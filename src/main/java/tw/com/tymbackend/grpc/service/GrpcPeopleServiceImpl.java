package tw.com.tymbackend.grpc.service;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.grpc.people.*;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.service.PeopleService;
import tw.com.tymbackend.core.service.AsyncMessageService;
import tw.com.tymbackend.core.service.AsyncResultService;
import tw.com.tymbackend.core.message.AsyncResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Optional;

/**
 * gRPC People Service 实现
 * 
 * <p>提供 People 相关的 gRPC 服务</p>
 * <p>支持异步模式：通过 RabbitMQ + Consumer 处理请求</p>
 * 
 * @author TY Team
 * @version 2.0
 */
@Service
public class GrpcPeopleServiceImpl extends PeopleServiceGrpc.PeopleServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcPeopleServiceImpl.class);
    
    // 异步等待配置
    private static final int MAX_WAIT_TIME_MS = 30000;  // 最大等待 30 秒
    private static final int POLL_INTERVAL_MS = 200;     // 每 200ms 轮询一次

    @Autowired
    private PeopleService peopleService;
    
    @Autowired(required = false)
    private AsyncMessageService asyncMessageService;
    
    @Autowired(required = false)
    private AsyncResultService asyncResultService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void getAllPeople(GetAllPeopleRequest request, StreamObserver<GetAllPeopleResponse> responseObserver) {
        logger.info("📥 gRPC: getAllPeople 请求");
        
        try {
            List<People> peopleList;
            
            // 检查是否启用异步处理模式（RabbitMQ + Consumer）
            if (asyncMessageService != null && asyncResultService != null) {
                logger.info("🔄 gRPC: 使用异步模式 (RabbitMQ + Consumer)");
                peopleList = getAllPeopleAsync();
            } else {
                logger.info("⚡ gRPC: 使用同步模式 (直接查询数据库)");
                peopleList = peopleService.getAllPeople();
            }
            
            GetAllPeopleResponse.Builder responseBuilder = GetAllPeopleResponse.newBuilder();
            
            for (People person : peopleList) {
                PeopleData peopleData = convertToPeopleData(person);
                responseBuilder.addPeople(peopleData);
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
            logger.info("✅ gRPC: getAllPeople 成功返回 {} 条数据", peopleList.size());
        } catch (Exception e) {
            logger.error("❌ gRPC: getAllPeople 错误", e);
            responseObserver.onError(e);
        }
    }
    
    /**
     * 异步获取所有 People（通过 RabbitMQ + Consumer）
     * 
     * @return People 列表
     * @throws Exception 处理失败时抛出异常
     */
    private List<People> getAllPeopleAsync() throws Exception {
        // 1. 发送异步请求到 RabbitMQ
        String requestId = asyncMessageService.sendPeopleGetAllRequest();
        logger.info("📤 gRPC: 已发送异步请求到 RabbitMQ, requestId={}", requestId);
        
        // 2. 轮询等待 Consumer 处理完成
        long startTime = System.currentTimeMillis();
        AsyncResultDTO result = null;
        
        while (System.currentTimeMillis() - startTime < MAX_WAIT_TIME_MS) {
            result = asyncResultService.getResult(requestId);
            
            if (result != null) {
                logger.info("✅ gRPC: 收到异步处理结果, status={}, elapsed={}ms", 
                    result.getStatus(), System.currentTimeMillis() - startTime);
                break;
            }
            
            // 等待一段时间后再次轮询
            Thread.sleep(POLL_INTERVAL_MS);
        }
        
        // 3. 检查是否超时
        if (result == null) {
            logger.error("⏱️ gRPC: 异步请求超时, requestId={}, maxWait={}ms", requestId, MAX_WAIT_TIME_MS);
            throw new RuntimeException("异步请求超时，请稍后重试");
        }
        
        // 4. 检查处理状态
        if ("failed".equals(result.getStatus())) {
            logger.error("❌ gRPC: 异步处理失败, requestId={}, error={}", requestId, result.getError());
            throw new RuntimeException("异步处理失败: " + result.getError());
        }
        
        // 5. 解析结果数据
        try {
            List<People> peopleList = objectMapper.convertValue(
                result.getData(), 
                new TypeReference<List<People>>() {}
            );
            logger.info("📊 gRPC: 成功解析异步结果, count={}", peopleList.size());
            
            // 6. 清理 Redis 中的结果（可选）
            asyncResultService.deleteResult(requestId);
            
            return peopleList;
        } catch (Exception e) {
            logger.error("❌ gRPC: 解析异步结果失败, requestId={}", requestId, e);
            throw new RuntimeException("解析异步结果失败", e);
        }
    }

    @Override
    public void getPeopleByName(GetPeopleByNameRequest request, StreamObserver<PeopleResponse> responseObserver) {
        logger.info("📥 gRPC: getPeopleByName, name={}", request.getName());
        
        try {
            Optional<People> personOpt = peopleService.getPeopleByName(request.getName());
            
            PeopleResponse.Builder responseBuilder = PeopleResponse.newBuilder();
            
            if (personOpt.isPresent()) {
                responseBuilder.setPeople(convertToPeopleData(personOpt.get()))
                              .setSuccess(true)
                              .setMessage("Success");
            } else {
                responseBuilder.setSuccess(false)
                              .setMessage("People not found");
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
            logger.info("✅ gRPC: getPeopleByName 完成");
            
        } catch (Exception e) {
            logger.error("❌ gRPC: getPeopleByName 错误", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void insertPeople(PeopleData request, StreamObserver<PeopleResponse> responseObserver) {
        logger.info("📥 gRPC: insertPeople, name={}", request.getName());
        
        try {
            People people = convertToPeopleEntity(request);
            People savedPeople = peopleService.insertPerson(people);
            
            PeopleResponse response = PeopleResponse.newBuilder()
                    .setPeople(convertToPeopleData(savedPeople))
                    .setSuccess(true)
                    .setMessage("People inserted successfully")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("✅ gRPC: insertPeople 成功");
            
        } catch (Exception e) {
            logger.error("❌ gRPC: insertPeople 错误", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void updatePeople(UpdatePeopleRequest request, StreamObserver<PeopleResponse> responseObserver) {
        logger.info("📥 gRPC: updatePeople, name={}", request.getName());
        
        try {
            People people = convertToPeopleEntity(request.getPeople());
            people.setName(request.getName()); // 确保使用请求中的名称
            
            People updatedPeople = peopleService.updatePerson(people);
            
            PeopleResponse.Builder responseBuilder = PeopleResponse.newBuilder();
            
            if (updatedPeople != null) {
                responseBuilder.setPeople(convertToPeopleData(updatedPeople))
                              .setSuccess(true)
                              .setMessage("People updated successfully");
            } else {
                responseBuilder.setSuccess(false)
                              .setMessage("People not found");
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
            logger.info("✅ gRPC: updatePeople 完成");
            
        } catch (Exception e) {
            logger.error("❌ gRPC: updatePeople 错误", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void deletePeople(DeletePeopleRequest request, StreamObserver<DeletePeopleResponse> responseObserver) {
        logger.info("📥 gRPC: deletePeople, name={}", request.getName());
        
        try {
            // 注意：PeopleService 目前没有提供单个删除方法
            // 这里返回不支持的消息
            DeletePeopleResponse response = DeletePeopleResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Delete by name not supported in current service implementation")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("✅ gRPC: deletePeople 成功");
            
        } catch (Exception e) {
            logger.error("❌ gRPC: deletePeople 错误", e);
            responseObserver.onError(e);
        }
    }

    /**
     * 转换 People Entity 到 PeopleData Proto
     */
    private PeopleData convertToPeopleData(People people) {
        PeopleData.Builder builder = PeopleData.newBuilder();
        
        // 基本字段
        if (people.getName() != null) builder.setName(people.getName());
        if (people.getNameOriginal() != null) builder.setNameOriginal(people.getNameOriginal());
        if (people.getCodeName() != null) builder.setCodeName(people.getCodeName());
        
        // 力量属性
        if (people.getPhysicPower() != null) builder.setPhysicPower(people.getPhysicPower());
        if (people.getMagicPower() != null) builder.setMagicPower(people.getMagicPower());
        if (people.getUtilityPower() != null) builder.setUtilityPower(people.getUtilityPower());
        
        // 基本信息
        if (people.getDob() != null) builder.setDob(people.getDob());
        if (people.getRace() != null) builder.setRace(people.getRace());
        if (people.getAttributes() != null) builder.setAttributes(people.getAttributes());
        if (people.getGender() != null) builder.setGender(people.getGender());
        
        // 身体特征
        if (people.getAssSize() != null) builder.setAssSize(people.getAssSize());
        if (people.getBoobsSize() != null) builder.setBoobsSize(people.getBoobsSize());
        if (people.getHeightCm() != null) builder.setHeightCm(people.getHeightCm());
        if (people.getWeightKg() != null) builder.setWeightKg(people.getWeightKg());
        
        // 职业和技能
        if (people.getProfession() != null) builder.setProfession(people.getProfession());
        if (people.getCombat() != null) builder.setCombat(people.getCombat());
        if (people.getJob() != null) builder.setJob(people.getJob());
        if (people.getPhysics() != null) builder.setPhysics(people.getPhysics());
        
        // 个性特征
        if (people.getKnownAs() != null) builder.setKnownAs(people.getKnownAs());
        if (people.getPersonality() != null) builder.setPersonality(people.getPersonality());
        if (people.getInterest() != null) builder.setInterest(people.getInterest());
        if (people.getLikes() != null) builder.setLikes(people.getLikes());
        if (people.getDislikes() != null) builder.setDislikes(people.getDislikes());
        if (people.getFavoriteFoods() != null) builder.setFavoriteFoods(people.getFavoriteFoods());
        
        // 关系和组织
        if (people.getConcubine() != null) builder.setConcubine(people.getConcubine());
        if (people.getFaction() != null) builder.setFaction(people.getFaction());
        if (people.getArmyId() != null) builder.setArmyId(people.getArmyId());
        if (people.getArmyName() != null) builder.setArmyName(people.getArmyName());
        if (people.getDeptId() != null) builder.setDeptId(people.getDeptId());
        if (people.getDeptName() != null) builder.setDeptName(people.getDeptName());
        if (people.getOriginArmyId() != null) builder.setOriginArmyId(people.getOriginArmyId());
        if (people.getOriginArmyName() != null) builder.setOriginArmyName(people.getOriginArmyName());
        
        // 其他信息
        if (people.getGaveBirth() != null) builder.setGaveBirth(people.getGaveBirth());
        if (people.getEmail() != null) builder.setEmail(people.getEmail());
        if (people.getAge() != null) builder.setAge(people.getAge());
        if (people.getProxy() != null) builder.setProxy(people.getProxy());
        
        // JSON 属性
        if (people.getBaseAttributes() != null) builder.setBaseAttributes(people.getBaseAttributes());
        if (people.getBonusAttributes() != null) builder.setBonusAttributes(people.getBonusAttributes());
        if (people.getStateAttributes() != null) builder.setStateAttributes(people.getStateAttributes());
        
        // 元数据
        if (people.getCreatedAt() != null) builder.setCreatedAt(people.getCreatedAt().toString());
        if (people.getUpdatedAt() != null) builder.setUpdatedAt(people.getUpdatedAt().toString());
        if (people.getVersion() != null) builder.setVersion(people.getVersion());
        
        return builder.build();
    }

    /**
     * 转换 PeopleData Proto 到 People Entity
     */
    private People convertToPeopleEntity(PeopleData data) {
        People people = new People();
        
        // 基本字段
        if (!data.getName().isEmpty()) people.setName(data.getName());
        if (!data.getNameOriginal().isEmpty()) people.setNameOriginal(data.getNameOriginal());
        if (!data.getCodeName().isEmpty()) people.setCodeName(data.getCodeName());
        
        // 力量属性
        if (data.getPhysicPower() != 0) people.setPhysicPower(data.getPhysicPower());
        if (data.getMagicPower() != 0) people.setMagicPower(data.getMagicPower());
        if (data.getUtilityPower() != 0) people.setUtilityPower(data.getUtilityPower());
        
        // 基本信息
        if (!data.getDob().isEmpty()) people.setDob(data.getDob());
        if (!data.getRace().isEmpty()) people.setRace(data.getRace());
        if (!data.getAttributes().isEmpty()) people.setAttributes(data.getAttributes());
        if (!data.getGender().isEmpty()) people.setGender(data.getGender());
        
        // 身体特征
        if (!data.getAssSize().isEmpty()) people.setAssSize(data.getAssSize());
        if (!data.getBoobsSize().isEmpty()) people.setBoobsSize(data.getBoobsSize());
        if (data.getHeightCm() != 0) people.setHeightCm(data.getHeightCm());
        if (data.getWeightKg() != 0) people.setWeightKg(data.getWeightKg());
        
        // 职业和技能
        if (!data.getProfession().isEmpty()) people.setProfession(data.getProfession());
        if (!data.getCombat().isEmpty()) people.setCombat(data.getCombat());
        if (!data.getJob().isEmpty()) people.setJob(data.getJob());
        if (!data.getPhysics().isEmpty()) people.setPhysics(data.getPhysics());
        
        // 个性特征
        if (!data.getKnownAs().isEmpty()) people.setKnownAs(data.getKnownAs());
        if (!data.getPersonality().isEmpty()) people.setPersonality(data.getPersonality());
        if (!data.getInterest().isEmpty()) people.setInterest(data.getInterest());
        if (!data.getLikes().isEmpty()) people.setLikes(data.getLikes());
        if (!data.getDislikes().isEmpty()) people.setDislikes(data.getDislikes());
        if (!data.getFavoriteFoods().isEmpty()) people.setFavoriteFoods(data.getFavoriteFoods());
        
        // 关系和组织
        if (!data.getConcubine().isEmpty()) people.setConcubine(data.getConcubine());
        if (!data.getFaction().isEmpty()) people.setFaction(data.getFaction());
        if (data.getArmyId() != 0) people.setArmyId(data.getArmyId());
        if (!data.getArmyName().isEmpty()) people.setArmyName(data.getArmyName());
        if (data.getDeptId() != 0) people.setDeptId(data.getDeptId());
        if (!data.getDeptName().isEmpty()) people.setDeptName(data.getDeptName());
        if (data.getOriginArmyId() != 0) people.setOriginArmyId(data.getOriginArmyId());
        if (!data.getOriginArmyName().isEmpty()) people.setOriginArmyName(data.getOriginArmyName());
        
        // 其他信息
        people.setGaveBirth(data.getGaveBirth());
        if (!data.getEmail().isEmpty()) people.setEmail(data.getEmail());
        if (data.getAge() != 0) people.setAge(data.getAge());
        if (!data.getProxy().isEmpty()) people.setProxy(data.getProxy());
        
        // JSON 属性
        if (!data.getBaseAttributes().isEmpty()) people.setBaseAttributes(data.getBaseAttributes());
        if (!data.getBonusAttributes().isEmpty()) people.setBonusAttributes(data.getBonusAttributes());
        if (!data.getStateAttributes().isEmpty()) people.setStateAttributes(data.getStateAttributes());
        
        return people;
    }
}
