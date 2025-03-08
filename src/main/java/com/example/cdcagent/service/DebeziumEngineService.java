package com.example.cdcagent.service;

import com.example.cdcagent.config.DebeziumProperties;
import com.example.cdcagent.model.ChangeEvent;
import io.debezium.config.Configuration;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Debeziumエンジンを管理するサービス
 */
@Service
public class DebeziumEngineService {
    private static final Logger logger = LoggerFactory.getLogger(DebeziumEngineService.class);

    private final DebeziumProperties debeziumProperties;
    private final AgentStateManager stateManager;
    private final ExecutorService executorService;
    private DebeziumEngine<io.debezium.engine.ChangeEvent<String, String>> engine;
    private final AtomicBoolean engineRunning = new AtomicBoolean(false);
    
    // 変更イベントを非同期に処理するためのSink
    private final Sinks.Many<ChangeEvent> changeEventSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Flux<ChangeEvent> changeEventFlux = changeEventSink.asFlux();

    @Autowired
    public DebeziumEngineService(DebeziumProperties debeziumProperties, AgentStateManager stateManager) {
        this.debeziumProperties = debeziumProperties;
        this.stateManager = stateManager;
        this.executorService = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "debezium-engine-thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Debeziumエンジンを初期化
     */
    @PostConstruct
    public void init() {
        logger.info("Debeziumエンジンを初期化中...");
        Configuration config = createDebeziumConfiguration();
        
        engine = DebeziumEngine.create(Json.class)
                .using(config.asProperties())
                .notifying(record -> {
                    if (stateManager.isRunning()) {
                        processRecord(record);
                    }
                })
                .using(this.getClass().getClassLoader())
                .build();
        
        logger.info("Debeziumエンジンが初期化されました");
    }

    /**
     * Debeziumエンジンを起動
     */
    public void start() {
        if (engineRunning.compareAndSet(false, true)) {
            logger.info("Debeziumエンジンを起動中...");
            executorService.submit(() -> {
                try {
                    engine.run();
                } catch (Exception e) {
                    logger.error("Debeziumエンジン実行中にエラーが発生しました", e);
                    engineRunning.set(false);
                    stateManager.setState(AgentStateManager.AgentState.STOPPING);
                }
            });
            logger.info("Debeziumエンジンが起動しました");
        } else {
            logger.warn("Debeziumエンジンは既に実行中です");
        }
    }

    /**
     * Debeziumエンジンを停止
     */
    @PreDestroy
    public void stop() {
        if (engineRunning.compareAndSet(true, false)) {
            logger.info("Debeziumエンジンを停止中...");
            try {
                engine.close();
                executorService.shutdown();
                logger.info("Debeziumエンジンが停止しました");
            } catch (IOException e) {
                logger.error("Debeziumエンジンの停止中にエラーが発生しました", e);
            }
        }
    }

    /**
     * 変更イベントのFluxを取得
     */
    public Flux<ChangeEvent> getChangeEventFlux() {
        return changeEventFlux;
    }

    /**
     * Debeziumの設定を作成
     */
    private Configuration createDebeziumConfiguration() {
        DebeziumProperties.Source.Database db = debeziumProperties.getSource().getDatabase();
        
        Properties props = new Properties();
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("name", debeziumProperties.getConnector().getName());
        props.setProperty("database.hostname", db.getHostname());
        props.setProperty("database.port", String.valueOf(db.getPort()));
        props.setProperty("database.user", db.getUser());
        props.setProperty("database.password", db.getPassword());
        props.setProperty("database.server.id", String.valueOf(db.getServerId()));
        props.setProperty("database.server.name", db.getServerName());
        props.setProperty("database.include.list", ".*");
        props.setProperty("table.include.list", ".*");
        props.setProperty("include.schema.changes", String.valueOf(db.isIncludeSchemaChanges()));
        props.setProperty("offset.storage", debeziumProperties.getConnector().getProperties().getOrDefault(
                "offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore"));
        props.setProperty("offset.storage.file.filename", debeziumProperties.getConnector().getProperties().getOrDefault(
                "offset.storage.file.filename", "${user.home}/offsets.dat"));
        props.setProperty("offset.flush.interval.ms", debeziumProperties.getConnector().getProperties().getOrDefault(
                "offset.flush.interval.ms", "60000"));
        
        return Configuration.from(props);
    }

    /**
     * Debeziumから受け取ったレコードを処理する
     */
    private void processRecord(io.debezium.engine.ChangeEvent<String, String> record) {
        try {
            if (record.value() != null) {
                // JSONをChangeEventに変換する処理を実装
                // 実際の実装ではJacksonなどを使ってJSON解析を行う
                logger.debug("変更イベントを受信: {}", record.value());
                
                // ここではサンプルとしてダミーのChangeEventを作成
                ChangeEvent changeEvent = new ChangeEvent();
                changeEvent.setId(java.util.UUID.randomUUID().toString());
                changeEvent.setType("mysql");
                changeEvent.setTimestamp(java.time.Instant.now());
                
                // 変更イベントをSinkに送信
                changeEventSink.tryEmitNext(changeEvent);
            }
        } catch (Exception e) {
            logger.error("レコード処理中にエラーが発生しました", e);
        }
    }
} 