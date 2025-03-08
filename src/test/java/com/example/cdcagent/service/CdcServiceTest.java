package com.example.cdcagent.service;

import com.example.cdcagent.config.HulftSquareProperties;
import com.example.cdcagent.model.ChangeEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CdcServiceTest {

    @Mock
    private DebeziumEngineService debeziumEngineService;

    @Mock
    private HulftSquareService hulftSquareService;

    @Mock
    private AgentStateManager stateManager;

    @Mock
    private HulftSquareProperties hulftSquareProperties;

    @Mock
    private HulftSquareProperties.Api api;

    // 実際のメトリクスレジストリを使用
    @Spy
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    // テスト対象
    private CdcService cdcService;

    @BeforeEach
    void setUp() {
        // HulftSquarePropertiesの設定
        lenient().when(hulftSquareProperties.getApi()).thenReturn(api);
        lenient().when(api.getMaxInFlight()).thenReturn(100);
        
        // テスト用のFluxを作成
        Sinks.Many<ChangeEvent> testSink = Sinks.many().unicast().onBackpressureBuffer();
        lenient().when(debeziumEngineService.getChangeEventFlux()).thenReturn(testSink.asFlux());
        
        // テスト対象のインスタンスを作成
        cdcService = new CdcService(
                debeziumEngineService,
                hulftSquareService,
                stateManager,
                hulftSquareProperties,
                meterRegistry);
    }

    @Test
    void start_shouldChangeStateAndStartDebeziumEngine() {
        // モックの設定
        when(stateManager.setState(AgentStateManager.AgentState.STARTING)).thenReturn(true);
        when(stateManager.setState(AgentStateManager.AgentState.RUNNING)).thenReturn(true);
        
        // 実行
        cdcService.start();
        
        // 検証
        verify(stateManager).setState(AgentStateManager.AgentState.STARTING);
        verify(debeziumEngineService).start();
        verify(stateManager).setState(AgentStateManager.AgentState.RUNNING);
    }

    @Test
    void start_shouldNotChangeStateWhenSetStateFails() {
        // モックの設定
        when(stateManager.setState(AgentStateManager.AgentState.STARTING)).thenReturn(false);
        
        // 実行
        cdcService.start();
        
        // 検証
        verify(stateManager).setState(AgentStateManager.AgentState.STARTING);
        verify(debeziumEngineService, never()).start();
        verify(stateManager, never()).setState(AgentStateManager.AgentState.RUNNING);
    }

    @Test
    void pause_shouldChangeStateToPaused() {
        // モックの設定
        when(stateManager.setState(AgentStateManager.AgentState.PAUSED)).thenReturn(true);
        
        // 実行
        cdcService.pause();
        
        // 検証
        verify(stateManager).setState(AgentStateManager.AgentState.PAUSED);
    }

    @Test
    void resume_shouldChangeStateToRunning() {
        // モックの設定
        when(stateManager.setState(AgentStateManager.AgentState.RUNNING)).thenReturn(true);
        
        // 実行
        cdcService.resume();
        
        // 検証
        verify(stateManager).setState(AgentStateManager.AgentState.RUNNING);
    }

    @Test
    void stop_shouldChangeStateAndStopDebeziumEngine() {
        // モックの設定
        when(stateManager.setState(AgentStateManager.AgentState.STOPPING)).thenReturn(true);
        when(stateManager.setState(AgentStateManager.AgentState.STOPPED)).thenReturn(true);
        
        // 実行
        cdcService.stop();
        
        // 検証
        verify(stateManager).setState(AgentStateManager.AgentState.STOPPING);
        verify(debeziumEngineService).stop();
        verify(stateManager).setState(AgentStateManager.AgentState.STOPPED);
    }

    @Test
    void stop_shouldNotChangeStateWhenSetStateFails() {
        // モックの設定
        when(stateManager.setState(AgentStateManager.AgentState.STOPPING)).thenReturn(false);
        
        // 実行
        cdcService.stop();
        
        // 検証
        verify(stateManager).setState(AgentStateManager.AgentState.STOPPING);
        verify(debeziumEngineService, never()).stop();
        verify(stateManager, never()).setState(AgentStateManager.AgentState.STOPPED);
    }

    @Test
    void init_shouldSetupMetrics() {
        // 初期化（@PostConstructが自動で呼ばれないため手動で呼び出す）
        cdcService.init();
        
        // メトリクスが登録されていることを確認
        Counter processedCounter = meterRegistry.find("cdc.events.processed").counter();
        Counter successCounter = meterRegistry.find("cdc.events.success").counter();
        Counter failedCounter = meterRegistry.find("cdc.events.failed").counter();
        
        // カウンターが作成されていることを確認
        assertEquals(0, processedCounter.count());
        assertEquals(0, successCounter.count());
        assertEquals(0, failedCounter.count());
    }
} 