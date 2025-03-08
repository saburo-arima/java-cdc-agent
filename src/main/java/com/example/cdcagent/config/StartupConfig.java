package com.example.cdcagent.config;

import com.example.cdcagent.service.CdcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * アプリケーション起動時の初期化設定
 */
@Configuration
public class StartupConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupConfig.class);
    
    private final CdcService cdcService;
    
    @Autowired
    public StartupConfig(CdcService cdcService) {
        this.cdcService = cdcService;
    }
    
    /**
     * アプリケーション起動時にエージェントを開始する
     */
    @Bean
    public CommandLineRunner initializeAgent() {
        return args -> {
            logger.info("アプリケーション起動時にCDCエージェントを自動起動します");
            cdcService.start();
        };
    }
} 