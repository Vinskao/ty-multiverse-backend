package tw.com.tymbackend.grpc.service;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.grpc.deckofcards.*;
import tw.com.tymbackend.core.service.AsyncMessageService;
import tw.com.tymbackend.core.service.AsyncResultService;
import tw.com.tymbackend.core.message.AsyncResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * gRPC Deckofcards Service 實現
 *
 * <p>提供 Deckofcards 相關的 gRPC 服務</p>
 * <p>支持異步模式：通過 RabbitMQ + Consumer 處理請求</p>
 *
 * @author TY Team
 * @version 1.0
 */
@Service
public class GrpcDeckofcardsServiceImpl extends DeckofcardsServiceGrpc.DeckofcardsServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcDeckofcardsServiceImpl.class);

    // 異步等待配置
    private static final int MAX_WAIT_TIME_MS = 30000;  // 最大等待 30 秒
    private static final int POLL_INTERVAL_MS = 200;     // 每 200ms 輪詢一次

    @Autowired(required = false)
    private AsyncMessageService asyncMessageService;

    @Autowired(required = false)
    private AsyncResultService asyncResultService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void startGame(StartGameRequest request, StreamObserver<GameResponse> responseObserver) {
        logger.info("📥 gRPC: startGame 請求");

        try {
            GameState gameState = startGameAsync();

            GameResponse response = GameResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Game started successfully")
                .setGameState(gameState)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: startGame 成功完成");
        } catch (Exception e) {
            logger.error("❌ gRPC: startGame 錯誤", e);

            GameResponse errorResponse = GameResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to start game: " + e.getMessage())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void playerHit(PlayerHitRequest request, StreamObserver<GameResponse> responseObserver) {
        logger.info("📥 gRPC: playerHit 請求");

        try {
            GameState gameState = playerHitAsync();

            GameResponse response = GameResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Player hit successfully")
                .setGameState(gameState)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: playerHit 成功完成");
        } catch (Exception e) {
            logger.error("❌ gRPC: playerHit 錯誤", e);

            GameResponse errorResponse = GameResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to hit: " + e.getMessage())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void playerStand(PlayerStandRequest request, StreamObserver<GameResponse> responseObserver) {
        logger.info("📥 gRPC: playerStand 請求");

        try {
            GameState gameState = playerStandAsync();

            GameResponse response = GameResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Player stand successfully")
                .setGameState(gameState)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: playerStand 成功完成");
        } catch (Exception e) {
            logger.error("❌ gRPC: playerStand 錯誤", e);

            GameResponse errorResponse = GameResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to stand: " + e.getMessage())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getGameStatus(GetGameStatusRequest request, StreamObserver<GameResponse> responseObserver) {
        logger.info("📥 gRPC: getGameStatus 請求");

        try {
            GameState gameState = getGameStatusAsync();

            GameResponse response = GameResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Game status retrieved successfully")
                .setGameState(gameState)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: getGameStatus 成功完成");
        } catch (Exception e) {
            logger.error("❌ gRPC: getGameStatus 錯誤", e);

            GameResponse errorResponse = GameResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to get game status: " + e.getMessage())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void playerDouble(PlayerDoubleRequest request, StreamObserver<GameResponse> responseObserver) {
        logger.info("📥 gRPC: playerDouble 請求");

        try {
            GameState gameState = playerDoubleAsync();

            GameResponse response = GameResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Player double successfully")
                .setGameState(gameState)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: playerDouble 成功完成");
        } catch (Exception e) {
            logger.error("❌ gRPC: playerDouble 錯誤", e);

            GameResponse errorResponse = GameResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to double: " + e.getMessage())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void playerSplit(PlayerSplitRequest request, StreamObserver<GameResponse> responseObserver) {
        logger.info("📥 gRPC: playerSplit 請求");

        try {
            GameState gameState = playerSplitAsync();

            GameResponse response = GameResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Player split successfully")
                .setGameState(gameState)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: playerSplit 成功完成");
        } catch (Exception e) {
            logger.error("❌ gRPC: playerSplit 錯誤", e);

            GameResponse errorResponse = GameResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to split: " + e.getMessage())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    /**
     * 異步調用 Consumer 的 startGame
     */
    private GameState startGameAsync() throws Exception {
        if (asyncMessageService != null && asyncResultService != null) {
            // 發送異步請求到 Consumer
            String requestId = asyncMessageService.sendDeckofcardsRequest("start", null);
            logger.info("📤 gRPC: 已發送 startGame 異步請求到 Consumer, requestId={}", requestId);

            // 等待結果
            return waitForGameResult(requestId);
        } else {
            throw new RuntimeException("Async services not available");
        }
    }

    /**
     * 異步調用 Consumer 的 playerHit
     */
    private GameState playerHitAsync() throws Exception {
        if (asyncMessageService != null && asyncResultService != null) {
            String requestId = asyncMessageService.sendDeckofcardsRequest("hit", null);
            logger.info("📤 gRPC: 已發送 playerHit 異步請求到 Consumer, requestId={}", requestId);
            return waitForGameResult(requestId);
        } else {
            throw new RuntimeException("Async services not available");
        }
    }

    /**
     * 異步調用 Consumer 的 playerStand
     */
    private GameState playerStandAsync() throws Exception {
        if (asyncMessageService != null && asyncResultService != null) {
            String requestId = asyncMessageService.sendDeckofcardsRequest("stand", null);
            logger.info("📤 gRPC: 已發送 playerStand 異步請求到 Consumer, requestId={}", requestId);
            return waitForGameResult(requestId);
        } else {
            throw new RuntimeException("Async services not available");
        }
    }

    /**
     * 異步調用 Consumer 的 getGameStatus
     */
    private GameState getGameStatusAsync() throws Exception {
        if (asyncMessageService != null && asyncResultService != null) {
            String requestId = asyncMessageService.sendDeckofcardsRequest("status", null);
            logger.info("📤 gRPC: 已發送 getGameStatus 異步請求到 Consumer, requestId={}", requestId);
            return waitForGameResult(requestId);
        } else {
            throw new RuntimeException("Async services not available");
        }
    }

    /**
     * 異步調用 Consumer 的 playerDouble
     */
    private GameState playerDoubleAsync() throws Exception {
        if (asyncMessageService != null && asyncResultService != null) {
            String requestId = asyncMessageService.sendDeckofcardsRequest("double", null);
            logger.info("📤 gRPC: 已發送 playerDouble 異步請求到 Consumer, requestId={}", requestId);
            return waitForGameResult(requestId);
        } else {
            throw new RuntimeException("Async services not available");
        }
    }

    /**
     * 異步調用 Consumer 的 playerSplit
     */
    private GameState playerSplitAsync() throws Exception {
        if (asyncMessageService != null && asyncResultService != null) {
            String requestId = asyncMessageService.sendDeckofcardsRequest("split", null);
            logger.info("📤 gRPC: 已發送 playerSplit 異步請求到 Consumer, requestId={}", requestId);
            return waitForGameResult(requestId);
        } else {
            throw new RuntimeException("Async services not available");
        }
    }

    /**
     * 等待 Consumer 處理結果
     */
    private GameState waitForGameResult(String requestId) throws Exception {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < MAX_WAIT_TIME_MS) {
            AsyncResultDTO result = asyncResultService.getResult(requestId);

            if (result != null) {
                if ("completed".equals(result.getStatus())) {
                    // 解析遊戲狀態
                    String dataJson = result.getData().toString();
                    Map<String, Object> data = objectMapper.readValue(
                        dataJson, new TypeReference<Map<String, Object>>() {});

                    return convertToGameState(data);
                } else {
                    throw new RuntimeException(result.getError());
                }
            }

            Thread.sleep(POLL_INTERVAL_MS);
        }

        throw new RuntimeException("Timeout waiting for game result");
    }

    /**
     * 將 Consumer 返回的數據轉換為 gRPC GameState
     */
    private GameState convertToGameState(Map<String, Object> data) {
        GameState.Builder builder = GameState.newBuilder();

        if (data.containsKey("gameId")) {
            builder.setGameId(data.get("gameId").toString());
        }

        // 這裡需要根據 Consumer 返回的實際數據結構來實現轉換
        // 由於 Consumer 的 BJDTO 結構未知，這裡提供一個基本的實現

        builder.setGameStatus("playing");
        builder.setCanHit(true);
        builder.setCanStand(true);
        builder.setMessage("Game state retrieved");

        return builder.build();
    }
}
