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
    @Transactional
    public People save(People person) {
        return peopleRepository.save(person);
    }
    
    /**
     * 新增角色
     * 
     * @param person 要新增的角色
     * @return 新增後的角色
     */
    @Transactional
    public People insertPerson(People person) {
        return save(person);
    }

    /**
     * 批量保存角色
     * 
     * @param peopleList 要保存的角色列表
     * @return 保存後的角色列表
     */
    @Transactional
    public List<People> saveAll(List<People> peopleList) {
        return peopleRepository.saveAll(peopleList);
    }
    
    /**
     * 保存所有角色
     * 
     * @param peopleList 要保存的角色列表
     * @return 保存後的角色列表
     */
    @Transactional
    public List<People> saveAllPeople(List<People> peopleList) {
        return saveAll(peopleList);
    }

    /**
     * 刪除所有角色
     */
    @Transactional
    public void deleteAll() {
        peopleRepository.deleteAll();
    }
    
    /**
     * 刪除所有角色
     */
    @Transactional
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
    @Transactional
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
    @Transactional
    public People updatePerson(People person) {
        if (person.getName() != null && peopleRepository.existsById(person.getName())) {
            return peopleRepository.save(person);
        }
        return save(person);
    }

    /**
     * 更新角色屬性
     * 
     * @param name 角色名稱
     * @param person 包含新屬性的角色
     * @return 更新後的角色，如果不存在則返回 null
     */
    @Transactional
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
        return peopleRepository.findAll().stream()
            .map(People::getName)
            .collect(Collectors.toList());
    }

    /**
     * 根據多個名稱查詢角色
     * 
     * @param names 要查詢的角色名稱列表
     * @return 符合條件的角色列表
     */
    public List<People> findByNames(List<String> names) {
        return names.stream()
            .map(this::findByName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    /**
     * 根據屬性查詢角色
     * 
     * @param attributes 要查詢的屬性列表
     * @return 具有匹配屬性的角色列表
     */
    public List<People> findByAttributes(List<String> attributes) {
        return peopleRepository.findAll().stream()
            .filter(person -> person.getAttributes() != null && 
                   attributes.stream().anyMatch(attr -> person.getAttributes().contains(attr)))
            .collect(Collectors.toList());
    }
}