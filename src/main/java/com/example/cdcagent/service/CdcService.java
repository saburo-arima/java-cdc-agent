package com.example.cdcagent.service;

import com.example.cdcagent.config.HulftSquareProperties;
import com.example.cdcagent.model.ChangeEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CDCエージェントのメインサービス
 */
@Service
public class CdcService {
    private static final Logger logger = LoggerFactory.getLogger(CdcService.class);

    private final DebeziumEngineService debeziumEngineService;
    private final HulftSquareService hulftSquareService;
    private final AgentStateManager stateManager;
    private final HulftSquareProperties hulftSquareProperties;
    private final MeterRegistry meterRegistry;

    // 進行中の処理数を追跡
    private final AtomicInteger inFlightRequests = new AtomicInteger(0);
    
    // メトリクス
    private Counter eventsProcessedCounter;
    private Counter eventsSuccessCounter;
    private Counter eventsFailedCounter;

    @Autowired
    public CdcService(
            DebeziumEngineService debeziumEngineService,
            HulftSquareService hulftSquareService,
            AgentStateManager stateManager,
            HulftSquareProperties hulftSquareProperties,
            MeterRegistry meterRegistry) {
        this.debeziumEngineService = debeziumEngineService;
        this.hulftSquareService = hulftSquareService;
        this.stateManager = stateManager;
        this.hulftSquareProperties = hulftSquareProperties;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // メトリクスの初期化
        eventsProcessedCounter = Counter.builder("cdc.events.processed")
                .description("処理されたイベントの総数")
                .register(meterRegistry);
        
        eventsSuccessCounter = Counter.builder("cdc.events.success")
                .description("正常に処理されたイベントの数")
                .register(meterRegistry);
        
        eventsFailedCounter = Counter.builder("cdc.events.failed")
                .description("処理に失敗したイベントの数")
                .register(meterRegistry);
        
        // inFlightリクエストのゲージ登録
        meterRegistry.gauge("cdc.requests.inflight", inFlightRequests);
        
        // DebeziumからのイベントストリームのSubscribe
        setupEventProcessor();
    }

    /**
     * CDCエージェントを起動
     */
    public void start() {
        if (stateManager.setState(AgentStateManager.AgentState.STARTING)) {
            logger.info("CDCエージェントを起動中...");
            debeziumEngineService.start();
            stateManager.setState(AgentStateManager.AgentState.RUNNING);
            logger.info("CDCエージェントが起動完了しました");
        } else {
            logger.warn("CDCエージェントはすでに起動しているか、起動中です");
        }
    }

    /**
     * CDCエージェントを一時停止
     */
    public void pause() {
        if (stateManager.setState(AgentStateManager.AgentState.PAUSED)) {
            logger.info("CDCエージェントを一時停止しました");
        } else {
            logger.warn("CDCエージェントの一時停止に失敗しました");
        }
    }

    /**
     * CDCエージェントを再開
     */
    public void resume() {
        if (stateManager.setState(AgentStateManager.AgentState.RUNNING)) {
            logger.info("CDCエージェントを再開しました");
        } else {
            logger.warn("CDCエージェントの再開に失敗しました");
        }
    }

    /**
     * CDCエージェントを停止
     */
    public void stop() {
        if (stateManager.setState(AgentStateManager.AgentState.STOPPING)) {
            logger.info("CDCエージェントを停止中...");
            debeziumEngineService.stop();
            stateManager.setState(AgentStateManager.AgentState.STOPPED);
            logger.info("CDCエージェントが停止しました");
        } else {
            logger.warn("CDCエージェントの停止に失敗しました");
        }
    }

    /**
     * イベント処理パイプラインのセットアップ
     */
    private void setupEventProcessor() {
        // 並列処理でイベントを処理
        int maxParallelism = Runtime.getRuntime().availableProcessors();
        
        debeziumEngineService.getChangeEventFlux()
                .parallel(maxParallelism)
                .runOn(Schedulers.boundedElastic())
                .doOnNext(event -> {
                    eventsProcessedCounter.increment();
                    logger.debug("イベントの処理を開始: {}", event.getId());
                })
                .filter(this::canProcessEvent)
                .flatMap(this::processAndSendEvent)
                .sequential()
                .subscribe(
                    success -> {
                        if (success) {
                            eventsSuccessCounter.increment();
                        } else {
                            eventsFailedCounter.increment();
                        }
                    },
                    error -> logger.error("イベント処理中にエラーが発生しました", error),
                    () -> logger.info("イベント処理ストリームが終了しました")
                );
    }

    /**
     * イベントを処理できるか確認（バックプレッシャー制御）
     */
    private boolean canProcessEvent(ChangeEvent event) {
        if (!stateManager.isRunning()) {
            logger.debug("エージェントが実行中ではないため、イベントをスキップします: {}", event.getId());
            return false;
        }

        // 進行中のリクエスト数を制限（バックプレッシャー）
        int currentInFlight = inFlightRequests.get();
        int maxInFlight = hulftSquareProperties.getApi().getMaxInFlight();
        
        if (currentInFlight >= maxInFlight) {
            logger.warn("バックプレッシャー制御: 進行中リクエスト数の上限に達したためイベントをスキップします (現在: {}, 上限: {})",
                    currentInFlight, maxInFlight);
            return false;
        }
        
        return true;
    }

    /**
     * イベントを処理してHULFT Squareに送信
     */
    private ParallelFlux<Boolean> processAndSendEvent(ChangeEvent event) {
        inFlightRequests.incrementAndGet();
        
        return ParallelFlux.from(hulftSquareService.sendEvent(event)
                .doFinally(signal -> inFlightRequests.decrementAndGet()));
    }
} 