package com.example.cdcagent.service;

import com.example.cdcagent.config.DebeziumProperties;
import io.debezium.config.Configuration;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebeziumEngineServiceTest {

    @Mock
    private DebeziumProperties debeziumProperties;

    @Mock
    private DebeziumProperties.Connector connector;

    @Mock
    private DebeziumProperties.Source source;

    @Mock
    private DebeziumProperties.Source.Database database;

    @Mock
    private AgentStateManager stateManager;

    @Mock
    private DebeziumEngine<ChangeEvent<String, String>> debeziumEngine;

    @Mock
    private ExecutorService executorService;

    private DebeziumEngineService debeziumEngineService;

    @BeforeEach
    void setUp() throws Exception {
        // モックの設定
        lenient().when(debeziumProperties.getConnector()).thenReturn(connector);
        lenient().when(debeziumProperties.getSource()).thenReturn(source);
        lenient().when(source.getDatabase()).thenReturn(database);
        
        lenient().when(connector.getName()).thenReturn("mysql-connector");
        lenient().when(database.getHostname()).thenReturn("localhost");
        lenient().when(database.getPort()).thenReturn(3306);
        lenient().when(database.getUser()).thenReturn("debezium");
        lenient().when(database.getPassword()).thenReturn("dbz");
        lenient().when(database.getServerId()).thenReturn(1);
        lenient().when(database.getServerName()).thenReturn("mysql-server-1");
        lenient().when(database.isIncludeSchemaChanges()).thenReturn(true);
        
        Map<String, String> props = new HashMap<>();
        props.put("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        props.put("offset.storage.file.filename", "${user.home}/offsets.dat");
        props.put("offset.flush.interval.ms", "60000");
        lenient().when(connector.getProperties()).thenReturn(props);
        
        // テスト用のDebeziumEngineServiceインスタンスを作成
        debeziumEngineService = new DebeziumEngineService(debeziumProperties, stateManager);
        
        // テスト用のprivateフィールドを設定するため、リフレクションを使用
        java.lang.reflect.Field engineField = DebeziumEngineService.class.getDeclaredField("engine");
        engineField.setAccessible(true);
        engineField.set(debeziumEngineService, debeziumEngine);
        
        java.lang.reflect.Field executorServiceField = DebeziumEngineService.class.getDeclaredField("executorService");
        executorServiceField.setAccessible(true);
        executorServiceField.set(debeziumEngineService, executorService);
    }

    @Test
    void getChangeEventFlux_shouldReturnNonNullFlux() {
        // 実行
        var flux = debeziumEngineService.getChangeEventFlux();
        
        // 検証
        assertNotNull(flux);
    }

    @Test
    void start_shouldSubmitEngineRunToExecutorService() {
        // モックの設定
        lenient().when(stateManager.isRunning()).thenReturn(true);
        
        // 実行
        debeziumEngineService.start();
        
        // 検証
        verify(executorService).submit(any(Runnable.class));
    }

    @Test
    void stop_shouldCloseEngineAndShutdownExecutorService() throws IOException {
        // エンジンとexecutorServiceが利用可能であることを確認
        assertNotNull(debeziumEngine);
        assertNotNull(executorService);
        
        // エンジン停止前にエンジンが実行中であることを示す
        java.lang.reflect.Field engineRunningField;
        try {
            engineRunningField = DebeziumEngineService.class.getDeclaredField("engineRunning");
            engineRunningField.setAccessible(true);
            java.util.concurrent.atomic.AtomicBoolean engineRunning = 
                (java.util.concurrent.atomic.AtomicBoolean) engineRunningField.get(debeziumEngineService);
            engineRunning.set(true);
            
            // 実行
            debeziumEngineService.stop();
            
            // 検証
            verify(debeziumEngine).close();
            verify(executorService).shutdown();
        } catch (Exception e) {
            fail("リフレクションによるフィールドアクセスに失敗: " + e.getMessage());
        }
    }

    @Test
    void init_shouldCreateDebeziumEngineWithCorrectConfiguration() throws Exception {
        // モックの設定
        DebeziumEngineService spyService = spy(new DebeziumEngineService(debeziumProperties, stateManager));
        
        // テスト準備：createDebeziumConfigurationメソッドをモック化
        java.lang.reflect.Method createConfigMethod = DebeziumEngineService.class.getDeclaredMethod("createDebeziumConfiguration");
        createConfigMethod.setAccessible(true);
        Configuration config = (Configuration) createConfigMethod.invoke(spyService);
        
        // 検証
        Properties props = config.asProperties();
        assertEquals("mysql-connector", props.getProperty("name"));
        assertEquals("localhost", props.getProperty("database.hostname"));
        assertEquals("3306", props.getProperty("database.port"));
        assertEquals("debezium", props.getProperty("database.user"));
        assertEquals("dbz", props.getProperty("database.password"));
        assertEquals("1", props.getProperty("database.server.id"));
        assertEquals("mysql-server-1", props.getProperty("database.server.name"));
        assertEquals(".*", props.getProperty("database.include.list"));
        assertEquals(".*", props.getProperty("table.include.list"));
        assertEquals("true", props.getProperty("include.schema.changes"));
        assertEquals("org.apache.kafka.connect.storage.FileOffsetBackingStore", props.getProperty("offset.storage"));
    }
} 