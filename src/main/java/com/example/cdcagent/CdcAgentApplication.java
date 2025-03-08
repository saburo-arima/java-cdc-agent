package com.example.cdcagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CDCエージェントのメインアプリケーションクラス
 * Debeziumを使用してMySQLデータベースの変更を監視し、HULFT SquareにREST APIで送信します
 */
@SpringBootApplication
@EnableScheduling
public class CdcAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdcAgentApplication.class, args);
    }
} 