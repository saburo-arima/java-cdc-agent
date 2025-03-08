package com.example.cdcagent.controller;

import com.example.cdcagent.service.AgentStateManager;
import com.example.cdcagent.service.CdcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * エージェント制御用のRESTコントローラー
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);
    
    private final CdcService cdcService;
    private final AgentStateManager stateManager;

    @Autowired
    public AgentController(CdcService cdcService, AgentStateManager stateManager) {
        this.cdcService = cdcService;
        this.stateManager = stateManager;
    }

    /**
     * エージェントを起動
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start() {
        logger.info("エージェント起動リクエストを受信");
        
        if (stateManager.isStopping()) {
            cdcService.start();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "エージェントを起動しました",
                    "state", stateManager.getState().toString()
            ));
        } else {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "status", "error",
                            "message", "エージェントはすでに起動しているか起動中です",
                            "state", stateManager.getState().toString()
                    ));
        }
    }

    /**
     * エージェントを一時停止
     */
    @PostMapping("/pause")
    public ResponseEntity<Map<String, Object>> pause() {
        logger.info("エージェント一時停止リクエストを受信");
        
        if (stateManager.isRunning()) {
            cdcService.pause();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "エージェントを一時停止しました",
                    "state", stateManager.getState().toString()
            ));
        } else {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "status", "error",
                            "message", "エージェントは実行中ではないため一時停止できません",
                            "state", stateManager.getState().toString()
                    ));
        }
    }

    /**
     * エージェントを再開
     */
    @PostMapping("/resume")
    public ResponseEntity<Map<String, Object>> resume() {
        logger.info("エージェント再開リクエストを受信");
        
        if (stateManager.isPaused()) {
            cdcService.resume();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "エージェントを再開しました",
                    "state", stateManager.getState().toString()
            ));
        } else {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "status", "error",
                            "message", "エージェントは一時停止中ではないため再開できません",
                            "state", stateManager.getState().toString()
                    ));
        }
    }

    /**
     * エージェントを停止
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stop() {
        logger.info("エージェント停止リクエストを受信");
        
        if (!stateManager.isStopping()) {
            cdcService.stop();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "エージェントを停止しました",
                    "state", stateManager.getState().toString()
            ));
        } else {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "status", "error",
                            "message", "エージェントはすでに停止しているか停止中です",
                            "state", stateManager.getState().toString()
                    ));
        }
    }

    /**
     * エージェントの状態を取得
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        logger.info("エージェント状態確認リクエストを受信");
        
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "state", stateManager.getState().toString()
        ));
    }
} 