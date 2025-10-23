package tw.com.tymbackend.grpc.service;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.grpc.gallery.GalleryServiceGrpc;
import tw.com.tymbackend.grpc.gallery.GalleryData;
import tw.com.tymbackend.grpc.gallery.GalleryResponse;
import tw.com.tymbackend.grpc.gallery.GetAllImagesRequest;
import tw.com.tymbackend.grpc.gallery.GetAllImagesResponse;
import tw.com.tymbackend.grpc.gallery.GetImageByIdRequest;
import tw.com.tymbackend.grpc.gallery.UpdateImageRequest;
import tw.com.tymbackend.grpc.gallery.DeleteImageRequest;
import tw.com.tymbackend.grpc.gallery.DeleteImageResponse;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;
import tw.com.tymbackend.module.gallery.service.GalleryService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * gRPC Gallery Service 实现
 *
 * <p>提供 Gallery 相关的 gRPC 服务</p>
 * <p>支持异步模式：通过 RabbitMQ + Consumer 处理请求</p>
 * <p>使用 gRPC ServerInterceptor 责任链：</p>
 * <ul>
 *   <li>RateLimitInterceptor - 限流控制</li>
 *   <li>AuthInterceptor - JWT认证</li>
 *   <li>LoggingInterceptor - 请求日志记录</li>
 *   <li>ErrorHandlingInterceptor - 异常转换</li>
 * </ul>
 *
 * @author TY Team
 * @version 1.0
 */
@Service
public class GrpcGalleryServiceImpl extends GalleryServiceGrpc.GalleryServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcGalleryServiceImpl.class);

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private GalleryService galleryService;

    @Override
    public void getAllImages(GetAllImagesRequest request, StreamObserver<GetAllImagesResponse> responseObserver) {
        logger.info("📥 gRPC: getAllImages 请求");

        try {
            List<Gallery> galleryList = galleryService.getAllImages();

            GetAllImagesResponse.Builder responseBuilder = GetAllImagesResponse.newBuilder();

            for (Gallery gallery : galleryList) {
                GalleryData galleryData = convertToGalleryData(gallery);
                responseBuilder.addGalleries(galleryData);
            }

            GetAllImagesResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: getAllImages 成功返回 {} 个图片", galleryList.size());

        } catch (Exception e) {
            logger.error("❌ gRPC: getAllImages 错误", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getImageById(GetImageByIdRequest request, StreamObserver<GalleryResponse> responseObserver) {
        logger.info("📥 gRPC: getImageById, id={}", request.getId());

        try {
            Optional<Gallery> galleryOpt = galleryService.getImageById(request.getId());

            GalleryResponse.Builder responseBuilder = GalleryResponse.newBuilder()
                    .setSuccess(galleryOpt.isPresent());

            if (galleryOpt.isPresent()) {
                GalleryData galleryData = convertToGalleryData(galleryOpt.get());
                responseBuilder.setGallery(galleryData)
                              .setMessage("图片获取成功");
            } else {
                responseBuilder.setMessage("图片不存在: " + request.getId());
            }

            GalleryResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: getImageById {} 完成", galleryOpt.isPresent() ? "成功" : "失败");

        } catch (Exception e) {
            logger.error("❌ gRPC: getImageById 错误", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void saveImage(GalleryData request, StreamObserver<GalleryResponse> responseObserver) {
        logger.info("📥 gRPC: saveImage, id={}", request.getId());

        try {
            Gallery gallery = convertFromGalleryData(request);
            Gallery savedGallery = galleryService.saveImage(gallery);

            GalleryData galleryData = convertToGalleryData(savedGallery);
            GalleryResponse response = GalleryResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("图片保存成功")
                    .setGallery(galleryData)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: saveImage 成功");

        } catch (Exception e) {
            logger.error("❌ gRPC: saveImage 错误", e);
            GalleryResponse response = GalleryResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("图片保存失败: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateImage(UpdateImageRequest request, StreamObserver<GalleryResponse> responseObserver) {
        logger.info("📥 gRPC: updateImage, id={}", request.getId());

        try {
            Gallery updatedGallery = galleryService.updateImage(request.getId(), request.getImageBase64());

            GalleryData galleryData = convertToGalleryData(updatedGallery);
            GalleryResponse response = GalleryResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("图片更新成功")
                    .setGallery(galleryData)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: updateImage 成功");

        } catch (Exception e) {
            logger.error("❌ gRPC: updateImage 错误", e);
            GalleryResponse response = GalleryResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("图片更新失败: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteImage(DeleteImageRequest request, StreamObserver<DeleteImageResponse> responseObserver) {
        logger.info("📥 gRPC: deleteImage, id={}", request.getId());

        try {
            galleryService.deleteImage(request.getId());

            DeleteImageResponse response = DeleteImageResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("图片删除成功")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: deleteImage 成功");

        } catch (Exception e) {
            logger.error("❌ gRPC: deleteImage 错误", e);
            DeleteImageResponse response = DeleteImageResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("图片删除失败: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * 将 Gallery 实体转换为 gRPC GalleryData 消息
     */
    private GalleryData convertToGalleryData(Gallery gallery) {
        return GalleryData.newBuilder()
                .setId(gallery.getId())
                .setImageBase64(gallery.getImageBase64() != null ? gallery.getImageBase64() : "")
                .setUploadTime(gallery.getUploadTime() != null ? gallery.getUploadTime().format(formatter) : "")
                .setVersion(gallery.getVersion() != null ? gallery.getVersion() : 0)
                .build();
    }

    /**
     * 将 gRPC GalleryData 消息转换为 Gallery 实体
     */
    private Gallery convertFromGalleryData(GalleryData galleryData) {
        Gallery gallery = new Gallery();
        gallery.setId(galleryData.getId());
        gallery.setImageBase64(galleryData.getImageBase64());

        if (!galleryData.getUploadTime().isEmpty()) {
            gallery.setUploadTime(LocalDateTime.parse(galleryData.getUploadTime(), formatter));
        } else {
            gallery.setUploadTime(LocalDateTime.now());
        }

        gallery.setVersion(galleryData.getVersion());

        return gallery;
    }
}
