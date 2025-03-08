package com.example.cdcagent.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * アプリケーション設定クラス
 */
@Configuration
@EnableConfigurationProperties({DebeziumProperties.class, HulftSquareProperties.class})
public class AppConfig {
    // 必要に応じて追加の設定を記述
} 