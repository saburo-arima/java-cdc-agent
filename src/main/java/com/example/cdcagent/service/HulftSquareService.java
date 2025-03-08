package com.example.cdcagent.service;

import com.example.cdcagent.config.HulftSquareProperties;
import com.example.cdcagent.model.ChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * HULFT Squareとの通信を担当するサービス
 */
@Service
public class HulftSquareService {
    private static final Logger logger = LoggerFactory.getLogger(HulftSquareService.class);

    private final WebClient webClient;
    private final HulftSquareProperties hulftSquareProperties;
    private final AgentStateManager stateManager;

    public HulftSquareService(WebClient webClient, HulftSquareProperties hulftSquareProperties, AgentStateManager stateManager) {
        this.webClient = webClient;
        this.hulftSquareProperties = hulftSquareProperties;
        this.stateManager = stateManager;
    }

    /**
     * 変更イベントをHULFT SquareのREST APIに送信
     * @param event 送信する変更イベント
     * @return 処理結果
     */
    public Mono<Boolean> sendEvent(ChangeEvent event) {
        if (!stateManager.isRunning()) {
            logger.debug("エージェントは実行中ではないため、イベント送信をスキップします: {}", event.getId());
            return Mono.just(false);
        }

        logger.debug("HULFT Squareにイベントを送信: {}", event.getId());
        
        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(event)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    logger.debug("HULFT Squareからの応答: {}", response);
                    return true;
                })
                .onErrorResume(e -> {
                    logger.error("HULFT Squareへのイベント送信中にエラーが発生しました: {}", e.getMessage());
                    return Mono.just(false);
                })
                .retryWhen(Retry.backoff(
                        hulftSquareProperties.getApi().getRetryCount(),
                        Duration.ofMillis(hulftSquareProperties.getApi().getRetryBackoffMs()))
                        .filter(throwable -> stateManager.isRunning())
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            logger.error("再試行回数を超過しました: {}", event.getId());
                            return retrySignal.failure();
                        }))
                .subscribeOn(Schedulers.boundedElastic());
    }
} 