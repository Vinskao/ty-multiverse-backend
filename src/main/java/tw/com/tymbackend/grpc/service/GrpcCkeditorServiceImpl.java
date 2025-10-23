package tw.com.tymbackend.grpc.service;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.grpc.ckeditor.*;
import tw.com.tymbackend.module.ckeditor.service.EditContentService;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

import java.time.Duration;
import java.util.Optional;

/**
 * gRPC CKEditor Service 實現
 *
 * <p>提供 CKEditor 相關的 gRPC 服務</p>
 * <p>支持 JWT 認證和 Redis 存儲草稿</p>
 *
 * @author TY Team
 * @version 1.0
 */
@Service
public class GrpcCkeditorServiceImpl extends CkeditorServiceGrpc.CkeditorServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcCkeditorServiceImpl.class);

    @Autowired
    private EditContentService editContentService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveContent(SaveContentRequest request, StreamObserver<SaveContentResponse> responseObserver) {
        logger.info("📝 gRPC: saveContent 請求 - userId={}, editor={}", request.getUserId(), request.getEditor());

        try {
            // 清除草稿（因為已保存到數據庫）
            String draftKey = "ckeditor:draft:" + request.getUserId() + ":" + request.getEditor();
            redisTemplate.delete(draftKey);

            // 檢查內容是否有變化
            Optional<EditContentVO> stored = editContentService.getContent(request.getEditor()).get();
            if (stored.isPresent() && request.getContent().equals(stored.get().getContent())) {
                SaveContentResponse response = SaveContentResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("No changes detected")
                    .setEditor(request.getEditor())
                    .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                logger.info("✅ gRPC: saveContent 成功 - 無變化");
                return;
            }

            // 保存內容
            EditContentVO content = new EditContentVO(request.getEditor(), request.getContent());
            editContentService.saveContent(content).get(); // 等待完成

            // 記錄用戶活動
            String activityKey = "user:activity:" + request.getUserId();
            redisTemplate.opsForValue().set(activityKey, String.valueOf(System.currentTimeMillis()));

            SaveContentResponse response = SaveContentResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Content saved successfully")
                .setEditor(request.getEditor())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: saveContent 成功完成");
        } catch (Exception e) {
            logger.error("❌ gRPC: saveContent 錯誤", e);

            SaveContentResponse errorResponse = SaveContentResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to save content: " + e.getMessage())
                .setEditor(request.getEditor())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getContent(GetContentRequest request, StreamObserver<GetContentResponse> responseObserver) {
        logger.info("📖 gRPC: getContent 請求 - editor={}", request.getEditor());

        try {
            Optional<EditContentVO> content = editContentService.getContent(request.getEditor()).get();

            String contentText = content.map(EditContentVO::getContent).orElse("");

            GetContentResponse response = GetContentResponse.newBuilder()
                .setSuccess(true)
                .setContent(contentText)
                .setEditor(request.getEditor())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: getContent 成功完成");
        } catch (Exception e) {
            logger.error("❌ gRPC: getContent 錯誤", e);

            GetContentResponse errorResponse = GetContentResponse.newBuilder()
                .setSuccess(false)
                .setContent("")
                .setEditor(request.getEditor())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getDraft(GetDraftRequest request, StreamObserver<GetDraftResponse> responseObserver) {
        logger.info("📝 gRPC: getDraft 請求 - userId={}, editor={}", request.getUserId(), request.getEditor());

        try {
            String draftKey = "ckeditor:draft:" + request.getUserId() + ":" + request.getEditor();
            String content = redisTemplate.opsForValue().get(draftKey);

            if (content == null) {
                content = "";
            }

            GetDraftResponse response = GetDraftResponse.newBuilder()
                .setSuccess(true)
                .setContent(content)
                .setEditor(request.getEditor())
                .setLastModified("") // 可以從 Redis TTL 獲取
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: getDraft 成功完成");
        } catch (Exception e) {
            logger.error("❌ gRPC: getDraft 錯誤", e);

            GetDraftResponse errorResponse = GetDraftResponse.newBuilder()
                .setSuccess(false)
                .setContent("")
                .setEditor(request.getEditor())
                .setLastModified("")
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void saveDraft(SaveDraftRequest request, StreamObserver<SaveDraftResponse> responseObserver) {
        logger.info("💾 gRPC: saveDraft 請求 - userId={}, editor={}", request.getUserId(), request.getEditor());

        try {
            String draftKey = "ckeditor:draft:" + request.getUserId() + ":" + request.getEditor();

            // 保存草稿 24 小時
            redisTemplate.opsForValue().set(draftKey, request.getContent(),
                                          Duration.ofHours(24));

            SaveDraftResponse response = SaveDraftResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Draft saved successfully")
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: saveDraft 成功完成");
        } catch (Exception e) {
            logger.error("❌ gRPC: saveDraft 錯誤", e);

            SaveDraftResponse errorResponse = SaveDraftResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to save draft: " + e.getMessage())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
}
