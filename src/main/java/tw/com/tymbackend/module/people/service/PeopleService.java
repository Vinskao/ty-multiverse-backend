package tw.com.tymbackend.module.people.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;

import tw.com.tymbackend.module.people.dao.PeopleRepository;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 角色服務類
 * 
 * 負責角色相關的業務邏輯處理，包括增刪改查等操作。
 */
@Service
@Transactional(readOnly = true, noRollbackFor = {IllegalArgumentException.class, EmptyResultDataAccessException.class})
public class PeopleService {

    private final PeopleRepository peopleRepository;

    /**
     * 建構函數
     * 
     * @param peopleRepository 角色資料庫操作介面
     */
    public PeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    /**
     * 查詢所有角色
     * 
     * @return 所有角色列表
     */
    public List<People> findAll() {
        return peopleRepository.findAll();
    }
    
    /**
     * 獲取所有角色
     * 
     * @return 所有角色列表
     */
    public List<People> getAllPeople() {
        return findAll();
    }

    /**
     * 根據名稱查詢角色
     * 
     * @param name 角色名稱
     * @return 角色資訊，如果不存在則返回空
     */
    public Optional<People> findByName(String name) {
        return peopleRepository.findByName(name);
    }
    
    /**
     * 根據名稱獲取角色
     * 
     * @param name 角色名稱
     * @return 角色資訊，如果不存在則返回空
     */
    public Optional<People> getPeopleByName(String name) {
        return peopleRepository.findByName(name);
    }

    /**
     * 保存角色
     * 
     * @param person 要保存的角色
     * @return 保存後的角色
     */
    @Transactional(transactionManager = "peopleTransactionManager", readOnly = false)
    public People save(People person) {
        return peopleRepository.save(person);
    }
    
    /**
     * 新增角色
     * 
     * @param person 要新增的角色
     * @return 新增後的角色
     */
    @Transactional(transactionManager = "peopleTransactionManager", readOnly = false)
    public People insertPerson(People person) {
        return save(person);
    }

    /**
     * 批量保存角色
     * 
     * @param peopleList 要保存的角色列表
     * @return 保存後的角色列表
     */
    @Transactional(transactionManager = "peopleTransactionManager", readOnly = false)
    public List<People> saveAll(List<People> peopleList) {
        return peopleRepository.saveAll(peopleList);
    }
    
    /**
     * 保存所有角色
     * 
     * @param peopleList 要保存的角色列表
     * @return 保存後的角色列表
     */
    @Transactional(transactionManager = "peopleTransactionManager", readOnly = false)
    public List<People> saveAllPeople(List<People> peopleList) {
        return saveAll(peopleList);
    }

    /**
     * 刪除所有角色
     */
    @Transactional(transactionManager = "peopleTransactionManager", readOnly = false)
    public void deleteAll() {
        peopleRepository.deleteAll();
    }
    
    /**
     * 刪除所有角色
     */
    @Transactional(transactionManager = "peopleTransactionManager", readOnly = false)
    public void deleteAllPeople() {
        deleteAll();
    }

    /**
     * 更新角色
     * 
     * @param name 角色名稱
     * @param person 要更新的角色資訊
     * @return 更新後的角色，如果不存在則返回 null
     */
    @Transactional(transactionManager = "peopleTransactionManager", readOnly = false)
    @SuppressWarnings("null")
    public People update(String name, People person) {
        if (peopleRepository.existsById(name)) {
            person.setName(name);
            return peopleRepository.save(person);
        }
        return null;
    }




    
    /**
     * 更新角色
     * 
     * @param person 要更新的角色
     * @return 更新後的角色
     */
    @Transactional(transactionManager = "peopleTransactionManager", readOnly = false)
    public People updatePerson(People person) {
        if (person.getName() != null) {
            // 先查詢現有實體，避免版本衝突
            Optional<People> existingOpt = peopleRepository.findById(person.getName());
            if (existingOpt.isPresent()) {
                People existing = existingOpt.get();
                // 更新所有非空字段
                updatePeopleFields(existing, person);
                // 若歷史資料 version 為 null，初始化為 0 以避免 Hibernate NPE
                if (existing.getVersion() == null) {
                    existing.setVersion(0L);
                }
                // 呼叫 save 可確保 flush 並使用正確的 TransactionManager
                return peopleRepository.save(existing);
            } else {
                // 如果不存在，拋出異常而不是嘗試插入
                throw new RuntimeException("Character not found with name: " + person.getName());
            }
        } else {
            throw new RuntimeException("Character name is required for update");
        }
    }

    /**
     * 更新 People 實體的字段
     * 
     * @param existing 現有實體
     * @param updated 包含更新數據的實體
     */
    private void updatePeopleFields(People existing, People updated) {
        if (updated.getNameOriginal() != null) existing.setNameOriginal(updated.getNameOriginal());
        if (updated.getCodeName() != null) existing.setCodeName(updated.getCodeName());
        if (updated.getPhysicPower() != null) existing.setPhysicPower(updated.getPhysicPower());
        if (updated.getMagicPower() != null) existing.setMagicPower(updated.getMagicPower());
        if (updated.getUtilityPower() != null) existing.setUtilityPower(updated.getUtilityPower());
        if (updated.getDob() != null) existing.setDob(updated.getDob());
        if (updated.getRace() != null) existing.setRace(updated.getRace());
        if (updated.getAttributes() != null) existing.setAttributes(updated.getAttributes());
        if (updated.getGender() != null) existing.setGender(updated.getGender());
        if (updated.getAssSize() != null) existing.setAssSize(updated.getAssSize());
        if (updated.getBoobsSize() != null) existing.setBoobsSize(updated.getBoobsSize());
        if (updated.getHeightCm() != null) existing.setHeightCm(updated.getHeightCm());
        if (updated.getWeightKg() != null) existing.setWeightKg(updated.getWeightKg());
        if (updated.getProfession() != null) existing.setProfession(updated.getProfession());
        if (updated.getCombat() != null) existing.setCombat(updated.getCombat());
        if (updated.getFavoriteFoods() != null) existing.setFavoriteFoods(updated.getFavoriteFoods());
        if (updated.getJob() != null) existing.setJob(updated.getJob());
        if (updated.getPhysics() != null) existing.setPhysics(updated.getPhysics());
        if (updated.getKnownAs() != null) existing.setKnownAs(updated.getKnownAs());
        if (updated.getPersonality() != null) existing.setPersonality(updated.getPersonality());
        if (updated.getInterest() != null) existing.setInterest(updated.getInterest());
        if (updated.getLikes() != null) existing.setLikes(updated.getLikes());
        if (updated.getDislikes() != null) existing.setDislikes(updated.getDislikes());
        if (updated.getConcubine() != null) existing.setConcubine(updated.getConcubine());
        if (updated.getFaction() != null) existing.setFaction(updated.getFaction());
        if (updated.getArmyId() != null) existing.setArmyId(updated.getArmyId());
        if (updated.getArmyName() != null) existing.setArmyName(updated.getArmyName());
        if (updated.getDeptId() != null) existing.setDeptId(updated.getDeptId());
        if (updated.getDeptName() != null) existing.setDeptName(updated.getDeptName());
        if (updated.getOriginArmyId() != null) existing.setOriginArmyId(updated.getOriginArmyId());
        if (updated.getOriginArmyName() != null) existing.setOriginArmyName(updated.getOriginArmyName());
        if (updated.getGaveBirth() != null) existing.setGaveBirth(updated.getGaveBirth());
        if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
        if (updated.getAge() != null) existing.setAge(updated.getAge());
        if (updated.getProxy() != null) existing.setProxy(updated.getProxy());
        if (updated.getBaseAttributes() != null) existing.setBaseAttributes(updated.getBaseAttributes());
        if (updated.getBonusAttributes() != null) existing.setBonusAttributes(updated.getBonusAttributes());
        if (updated.getStateAttributes() != null) existing.setStateAttributes(updated.getStateAttributes());
        if (updated.getEmbedding() != null) existing.setEmbedding(updated.getEmbedding());
        
        // 更新時間戳
        existing.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * 更新角色屬性
     * 
     * @param name 角色名稱
     * @param person 包含新屬性的角色
     * @return 更新後的角色，如果不存在則返回 null
     */
    @Transactional(transactionManager = "peopleTransactionManager", readOnly = false)
    @SuppressWarnings("null")
    public People updateAttributes(String name, People person) {
        return peopleRepository.findByName(name)
            .map(existing -> {
                existing.setBaseAttributes(person.getBaseAttributes());
                existing.setBonusAttributes(person.getBonusAttributes());
                existing.setStateAttributes(person.getStateAttributes());
                return peopleRepository.save(existing);
            })
            .orElse(null);
    }



    /**
     * 根據規格查詢角色
     * 
     * @param spec 查詢規格
     * @return 符合條件的角色列表
     */
    public List<People> findBySpecification(Specification<People> spec) {
        return peopleRepository.findAll(spec);
    }

    /**
     * 根據規格和排序查詢角色
     * 
     * @param spec 查詢規格
     * @param sort 排序規則
     * @return 符合條件的角色列表
     */
    public List<People> findBySpecification(Specification<People> spec, Sort sort) {
        return peopleRepository.findAll(spec, sort);
    }

    /**
     * 根據規格和分頁查詢角色
     * 
     * @param spec 查詢規格
     * @param pageable 分頁參數
     * @return 分頁的角色列表
     */
    public Page<People> findBySpecification(Specification<People> spec, Pageable pageable) {
        return peopleRepository.findAll(spec, pageable);
    }

    /**
     * 根據多個規格查詢角色
     * 
     * @param specs 查詢規格列表
     * @return 符合所有條件的角色列表
     */
    public List<People> findByMultipleSpecifications(List<Specification<People>> specs) {
        Specification<People> combinedSpec = specs.stream()
            .reduce(Specification.where(null), Specification::and);
        return peopleRepository.findAll(combinedSpec);
    }

    /**
     * 分頁查詢所有角色
     * 
     * @param pageable 分頁參數
     * @return 分頁的角色列表
     */
    public Page<People> findAll(Pageable pageable) {
        return peopleRepository.findAll(pageable);
    }
    
    /**
     * 獲取所有角色名稱
     * 
     * @return 角色名稱列表
     */
    public List<String> getAllPeopleNames() {
        // ✅ 優化：直接查詢名稱，避免載入所有欄位
        return peopleRepository.findAllNames();
    }

    /**
     * 根據多個名稱查詢角色
     * 
     * @param names 要查詢的角色名稱列表
     * @return 符合條件的角色列表
     */
    public List<People> findByNames(List<String> names) {
        // ✅ 優化：使用批量查詢，避免N+1問題
        return peopleRepository.findByNamesIn(names);
    }

    /**
     * 根據屬性查詢角色
     * 
     * @param attributes 要查詢的屬性列表
     * @return 具有匹配屬性的角色列表
     */
    public List<People> findByAttributes(List<String> attributes) {
        // ✅ 優化：使用資料庫層級過濾，避免載入所有資料
        return attributes.stream()
            .flatMap(attr -> peopleRepository.findByAttributeContaining(attr).stream())
            .distinct() // 移除重複結果
            .collect(Collectors.toList());
    }
}