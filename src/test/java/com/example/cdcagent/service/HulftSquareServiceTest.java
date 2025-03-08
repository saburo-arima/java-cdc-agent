package com.example.cdcagent.service;

import com.example.cdcagent.config.HulftSquareProperties;
import com.example.cdcagent.model.ChangeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HulftSquareServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private HulftSquareProperties hulftSquareProperties;

    @Mock
    private HulftSquareProperties.Api api;

    @Mock
    private AgentStateManager stateManager;

    private HulftSquareService hulftSquareService;
    private ChangeEvent testEvent;

    @BeforeEach
    void setUp() {
        lenient().when(hulftSquareProperties.getApi()).thenReturn(api);
        lenient().when(api.getRetryCount()).thenReturn(3);
        lenient().when(api.getRetryBackoffMs()).thenReturn(100);
        
        // WebClientのモックチェーンをセットアップ
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        hulftSquareService = new HulftSquareService(webClient, hulftSquareProperties, stateManager);
        
        // テスト用のChangeEventを作成
        testEvent = createTestEvent();
    }

    @Test
    void sendEvent_shouldReturnTrue_whenAgentIsRunningAndHulftSquareResponseIsSuccessful() {
        // モックの設定
        when(stateManager.isRunning()).thenReturn(true);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("Success"));
        
        // 実行と検証
        StepVerifier.create(hulftSquareService.sendEvent(testEvent))
                .expectNext(true)
                .expectComplete()
                .verify();
        
        // WebClientのメソッドが呼ばれたことを検証
        verify(webClient).post();
        verify(requestBodyUriSpec).contentType(any());
        verify(requestBodySpec).bodyValue(any());
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(String.class);
    }

    @Test
    void sendEvent_shouldReturnFalse_whenAgentIsNotRunning() {
        // モックの設定
        when(stateManager.isRunning()).thenReturn(false);
        
        // 実行と検証
        StepVerifier.create(hulftSquareService.sendEvent(testEvent))
                .expectNext(false)
                .expectComplete()
                .verify();
        
        // WebClientのメソッドが呼ばれないことを検証
        verify(webClient, never()).post();
    }

    @Test
    void sendEvent_shouldReturnFalse_whenHulftSquareResponseFails() {
        // モックの設定
        when(stateManager.isRunning()).thenReturn(true);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("API Error")));
        
        // 実行と検証
        StepVerifier.create(hulftSquareService.sendEvent(testEvent))
                .expectNext(false)
                .expectComplete()
                .verify();
        
        // WebClientのメソッドが呼ばれたことを検証
        verify(webClient).post();
        verify(requestBodyUriSpec).contentType(any());
        verify(requestBodySpec).bodyValue(any());
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(String.class);
    }

    private ChangeEvent createTestEvent() {
        ChangeEvent event = new ChangeEvent();
        event.setId(UUID.randomUUID().toString());
        event.setType("mysql");
        event.setDatabase("testdb");
        event.setTable("testtable");
        event.setOperation(ChangeEvent.OperationType.INSERT);
        event.setTimestamp(Instant.now());
        
        Map<String, Object> after = new HashMap<>();
        after.put("id", 1);
        after.put("name", "Test Name");
        after.put("created_at", Instant.now().toString());
        
        event.setAfter(after);
        
        return event;
    }
} 