package com.example.cdcagent.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebClientConfigTest {

    @Mock
    private HulftSquareProperties hulftSquareProperties;

    @Mock
    private HulftSquareProperties.Api api;

    @Test
    void webClient_shouldCreateWebClientWithCorrectConfiguration() {
        // モックの設定
        lenient().when(hulftSquareProperties.getApi()).thenReturn(api);
        lenient().when(api.getConnectTimeout()).thenReturn(5000);
        lenient().when(api.getReadTimeout()).thenReturn(5000);
        lenient().when(api.getWriteTimeout()).thenReturn(5000);
        lenient().when(api.getUrl()).thenReturn("http://localhost:9000/api/events");
        
        // テスト対象のインスタンスを作成
        WebClientConfig webClientConfig = new WebClientConfig(hulftSquareProperties);
        
        // 実行
        WebClient webClient = webClientConfig.webClient();
        
        // 検証
        assertNotNull(webClient);
    }
} 