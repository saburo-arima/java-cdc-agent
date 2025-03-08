package com.example.cdcagent.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient設定クラス
 */
@Configuration
public class WebClientConfig {

    private final HulftSquareProperties hulftSquareProperties;

    public WebClientConfig(HulftSquareProperties hulftSquareProperties) {
        this.hulftSquareProperties = hulftSquareProperties;
    }

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, hulftSquareProperties.getApi().getConnectTimeout())
                .responseTimeout(Duration.ofMillis(hulftSquareProperties.getApi().getReadTimeout()))
                .doOnConnected(conn -> 
                        conn.addHandlerLast(new ReadTimeoutHandler(hulftSquareProperties.getApi().getReadTimeout(), TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(hulftSquareProperties.getApi().getWriteTimeout(), TimeUnit.MILLISECONDS)));

        // メモリバッファサイズを増やす
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(hulftSquareProperties.getApi().getUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
} 