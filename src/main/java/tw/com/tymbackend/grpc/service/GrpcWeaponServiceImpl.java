package tw.com.tymbackend.grpc.service;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.grpc.weapons.WeaponServiceGrpc;
import tw.com.tymbackend.grpc.weapons.*;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.weapon.service.WeaponService;
import tw.com.tymbackend.core.service.AsyncMessageService;
import tw.com.tymbackend.core.service.AsyncResultService;
import tw.com.tymbackend.core.message.AsyncResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * gRPC Weapon Service 实现
 *
 * <p>提供 Weapon 相关的 gRPC 服务</p>
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
public class GrpcWeaponServiceImpl extends WeaponServiceGrpc.WeaponServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcWeaponServiceImpl.class);

    // 异步等待配置
    private static final int MAX_WAIT_TIME_MS = 30000;  // 最大等待 30 秒
    private static final int POLL_INTERVAL_MS = 200;     // 每 200ms 轮询一次

    @Autowired
    private WeaponService weaponService;

    @Autowired(required = false)
    private AsyncMessageService asyncMessageService;

    @Autowired
    private AsyncResultService asyncResultService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void getAllWeapons(GetAllWeaponsRequest request, StreamObserver<GetAllWeaponsResponse> responseObserver) {
        logger.info("📥 gRPC: getAllWeapons 请求");

        try {
            // 同步获取所有武器
            List<Weapon> weaponList = weaponService.getAllWeapons();

            // 转换为 gRPC 消息
            GetAllWeaponsResponse.Builder responseBuilder = GetAllWeaponsResponse.newBuilder();

            for (Weapon weapon : weaponList) {
                WeaponData weaponData = convertToWeaponData(weapon);
                responseBuilder.addWeapons(weaponData);
            }

            GetAllWeaponsResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: getAllWeapons 成功返回 {} 个武器", weaponList.size());

        } catch (Exception e) {
            logger.error("❌ gRPC: getAllWeapons 错误", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getWeaponById(GetWeaponByIdRequest request, StreamObserver<WeaponResponse> responseObserver) {
        logger.info("📥 gRPC: getWeaponById, name={}", request.getName());

        try {
            Optional<Weapon> weaponOpt = weaponService.getWeaponById(request.getName());

            WeaponResponse.Builder responseBuilder = WeaponResponse.newBuilder()
                    .setSuccess(weaponOpt.isPresent());

            if (weaponOpt.isPresent()) {
                WeaponData weaponData = convertToWeaponData(weaponOpt.get());
                responseBuilder.setWeapon(weaponData)
                              .setMessage("武器获取成功");
            } else {
                responseBuilder.setMessage("武器不存在: " + request.getName());
            }

            WeaponResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: getWeaponById {} 完成", weaponOpt.isPresent() ? "成功" : "失败");

        } catch (Exception e) {
            logger.error("❌ gRPC: getWeaponById 错误", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getWeaponsByOwner(GetWeaponsByOwnerRequest request, StreamObserver<GetWeaponsByOwnerResponse> responseObserver) {
        logger.info("📥 gRPC: getWeaponsByOwner, owner={}", request.getOwner());

        try {
            List<Weapon> weaponList = weaponService.getWeaponsByOwner(request.getOwner());

            GetWeaponsByOwnerResponse.Builder responseBuilder = GetWeaponsByOwnerResponse.newBuilder();

            for (Weapon weapon : weaponList) {
                WeaponData weaponData = convertToWeaponData(weapon);
                responseBuilder.addWeapons(weaponData);
            }

            GetWeaponsByOwnerResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: getWeaponsByOwner 成功返回 {} 个武器", weaponList.size());

        } catch (Exception e) {
            logger.error("❌ gRPC: getWeaponsByOwner 错误", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void createWeapon(WeaponData request, StreamObserver<WeaponResponse> responseObserver) {
        logger.info("📥 gRPC: createWeapon, name={}", request.getName());

        try {
            Weapon weapon = convertFromWeaponData(request);
            Weapon savedWeapon = weaponService.saveWeaponSmart(weapon);

            WeaponData weaponData = convertToWeaponData(savedWeapon);
            WeaponResponse response = WeaponResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("武器创建成功")
                    .setWeapon(weaponData)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: createWeapon 成功");

        } catch (Exception e) {
            logger.error("❌ gRPC: createWeapon 错误", e);
            WeaponResponse response = WeaponResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("武器创建失败: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateWeapon(UpdateWeaponRequest request, StreamObserver<WeaponResponse> responseObserver) {
        logger.info("📥 gRPC: updateWeapon, name={}", request.getName());

        try {
            Weapon weapon = convertFromWeaponData(request.getWeapon());
            weapon.setName(request.getName());

            Weapon updatedWeapon = weaponService.saveWeaponSmart(weapon);

            WeaponData weaponData = convertToWeaponData(updatedWeapon);
            WeaponResponse response = WeaponResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("武器更新成功")
                    .setWeapon(weaponData)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: updateWeapon 成功");

        } catch (Exception e) {
            logger.error("❌ gRPC: updateWeapon 错误", e);
            WeaponResponse response = WeaponResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("武器更新失败: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteWeapon(DeleteWeaponRequest request, StreamObserver<DeleteWeaponResponse> responseObserver) {
        logger.info("📥 gRPC: deleteWeapon, name={}", request.getName());

        try {
            weaponService.deleteWeapon(request.getName());

            DeleteWeaponResponse response = DeleteWeaponResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("武器删除成功")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: deleteWeapon 成功");

        } catch (Exception e) {
            logger.error("❌ gRPC: deleteWeapon 错误", e);
            DeleteWeaponResponse response = DeleteWeaponResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("武器删除失败: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void checkWeaponExists(CheckWeaponExistsRequest request, StreamObserver<CheckWeaponExistsResponse> responseObserver) {
        logger.info("📥 gRPC: checkWeaponExists, name={}", request.getName());

        try {
            boolean exists = weaponService.weaponExists(request.getName());

            CheckWeaponExistsResponse response = CheckWeaponExistsResponse.newBuilder()
                    .setExists(exists)
                    .setMessage(exists ? "武器存在" : "武器不存在")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: checkWeaponExists 完成, exists={}", exists);

        } catch (Exception e) {
            logger.error("❌ gRPC: checkWeaponExists 错误", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateWeaponAttributes(UpdateWeaponAttributesRequest request, StreamObserver<WeaponResponse> responseObserver) {
        logger.info("📥 gRPC: updateWeaponAttributes, name={}", request.getName());

        try {
            Weapon weapon = weaponService.getWeaponById(request.getName())
                    .orElseThrow(() -> new RuntimeException("武器不存在: " + request.getName()));

            weapon.setAttributes(request.getAttributes());
            Weapon updatedWeapon = weaponService.updateWeaponAttributes(request.getName(), weapon);

            WeaponData weaponData = convertToWeaponData(updatedWeapon);
            WeaponResponse response = WeaponResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("武器属性更新成功")
                    .setWeapon(weaponData)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: updateWeaponAttributes 成功");

        } catch (Exception e) {
            logger.error("❌ gRPC: updateWeaponAttributes 错误", e);
            WeaponResponse response = WeaponResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("武器属性更新失败: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void findByDamageRange(FindByDamageRangeRequest request, StreamObserver<FindByDamageRangeResponse> responseObserver) {
        logger.info("📥 gRPC: findByDamageRange, min={}, max={}", request.getMinDamage(), request.getMaxDamage());

        try {
            List<Weapon> weaponList = weaponService.findByBaseDamageRange(request.getMinDamage(), request.getMaxDamage());

            FindByDamageRangeResponse.Builder responseBuilder = FindByDamageRangeResponse.newBuilder();

            for (Weapon weapon : weaponList) {
                WeaponData weaponData = convertToWeaponData(weapon);
                responseBuilder.addWeapons(weaponData);
            }

            FindByDamageRangeResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: findByDamageRange 成功返回 {} 个武器", weaponList.size());

        } catch (Exception e) {
            logger.error("❌ gRPC: findByDamageRange 错误", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void findByAttribute(FindByAttributeRequest request, StreamObserver<FindByAttributeResponse> responseObserver) {
        logger.info("📥 gRPC: findByAttribute, attribute={}", request.getAttribute());

        try {
            List<Weapon> weaponList = weaponService.findByAttribute(request.getAttribute());

            FindByAttributeResponse.Builder responseBuilder = FindByAttributeResponse.newBuilder();

            for (Weapon weapon : weaponList) {
                WeaponData weaponData = convertToWeaponData(weapon);
                responseBuilder.addWeapons(weaponData);
            }

            FindByAttributeResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("✅ gRPC: findByAttribute 成功返回 {} 个武器", weaponList.size());

        } catch (Exception e) {
            logger.error("❌ gRPC: findByAttribute 错误", e);
            responseObserver.onError(e);
        }
    }

    /**
     * 将 Weapon 实体转换为 gRPC WeaponData 消息
     */
    private WeaponData convertToWeaponData(Weapon weapon) {
        return WeaponData.newBuilder()
                .setName(weapon.getName())
                .setOwner(weapon.getOwner() != null ? weapon.getOwner() : "")
                .setAttributes(weapon.getAttributes() != null ? weapon.getAttributes() : "")
                .setBaseDamage(weapon.getBaseDamage() != null ? weapon.getBaseDamage() : 0)
                .setBonusDamage(weapon.getBonusDamage() != null ? weapon.getBonusDamage() : 0)
                .addAllBonusAttributes(weapon.getBonusAttributes() != null ? weapon.getBonusAttributes() : List.of())
                .addAllStateAttributes(weapon.getStateAttributes() != null ? weapon.getStateAttributes() : List.of())
                .setCreatedAt(weapon.getCreatedAt() != null ? weapon.getCreatedAt().format(formatter) : "")
                .setUpdatedAt(weapon.getUpdatedAt() != null ? weapon.getUpdatedAt().format(formatter) : "")
                .setVersion(weapon.getVersion() != null ? weapon.getVersion() : 0)
                .build();
    }

    /**
     * 将 gRPC WeaponData 消息转换为 Weapon 实体
     */
    private Weapon convertFromWeaponData(WeaponData weaponData) {
        Weapon weapon = new Weapon();
        weapon.setName(weaponData.getName());
        weapon.setOwner(weaponData.getOwner());
        weapon.setAttributes(weaponData.getAttributes());
        weapon.setBaseDamage(weaponData.getBaseDamage());
        weapon.setBonusDamage(weaponData.getBonusDamage());
        weapon.setBonusAttributes(weaponData.getBonusAttributesList());
        weapon.setStateAttributes(weaponData.getStateAttributesList());

        if (!weaponData.getCreatedAt().isEmpty()) {
            weapon.setCreatedAt(LocalDateTime.parse(weaponData.getCreatedAt(), formatter));
        }
        if (!weaponData.getUpdatedAt().isEmpty()) {
            weapon.setUpdatedAt(LocalDateTime.parse(weaponData.getUpdatedAt(), formatter));
        }
        weapon.setVersion(weaponData.getVersion());

        return weapon;
    }
}

