package tw.com.tymbackend.module.people.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.tymbackend.module.people.dao.PeopleImageRepository;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 角色頭像服務類
 * 
 * 負責角色頭像相關的業務邏輯處理，包括增刪改查等操作。
 */
@Service
@Transactional(readOnly = true)
public class PeopleImageService {
    private final PeopleImageRepository peopleImageRepository;

    /**
     * 建構函數
     * 
     * @param peopleImageRepository 角色頭像資料庫操作介面
     */
    public PeopleImageService(PeopleImageRepository peopleImageRepository) {
        this.peopleImageRepository = peopleImageRepository;
    }

    /**
     * 獲取所有角色頭像
     * 
     * @return 所有角色頭像列表
     */
    public List<PeopleImage> getAllPeopleImages() {
        return peopleImageRepository.findAll();
    }
    
    /**
     * 根據代碼名稱獲取角色頭像
     * 
     * @param codeName 角色的代碼名稱
     * @return 角色頭像
     * @throws NoSuchElementException 如果找不到對應的頭像
     */
    public PeopleImage getPeopleImageByCodeName(String codeName) {
        PeopleImage image = peopleImageRepository.findByCodeName(codeName);
        if (image == null) {
            throw new NoSuchElementException("找不到代碼名稱為 " + codeName + " 的頭像");
        }
        return image;
    }
    
    /**
     * 檢查角色頭像是否存在
     * 
     * @param codeName 角色的代碼名稱
     * @return 如果頭像存在返回 true，否則返回 false
     */
    public boolean peopleImageExists(String codeName) {
        return peopleImageRepository.existsByCodeName(codeName);
    }

    /**
     * 保存角色頭像
     * 
     * @param peopleImage 要保存的角色頭像
     * @return 保存後的角色頭像
     */
    @Transactional
    public PeopleImage savePeopleImage(PeopleImage peopleImage) {
        return peopleImageRepository.save(peopleImage);
    }
    
    /**
     * 根據代碼名稱刪除角色頭像
     * 
     * @param codeName 角色的代碼名稱
     * @throws NoSuchElementException 如果找不到對應的頭像
     */
    @Transactional
    public void deletePeopleImage(String codeName) {
        PeopleImage peopleImage = getPeopleImageByCodeName(codeName);
        peopleImageRepository.delete(peopleImage);
    }
    
    /**
     * 檢查代碼名稱是否唯一
     * 
     * @param codeName 要檢查的代碼名稱
     * @return 如果代碼名稱唯一返回 true，否則返回 false
     */
    public boolean isCodeNameUnique(String codeName) {
        return !peopleImageRepository.findAll(
            (root, query, cb) -> cb.equal(root.get("codeName"), codeName)
        ).isEmpty();
    }

    /**
     * 根據規格查詢角色頭像
     * 
     * @param spec 查詢規格
     * @return 符合條件的角色頭像列表
     */
    public List<PeopleImage> findBySpecification(Specification<PeopleImage> spec) {
        return peopleImageRepository.findAll(spec);
    }

    /**
     * 分頁查詢所有角色頭像
     * 
     * @param pageable 分頁參數
     * @return 分頁的角色頭像列表
     */
    public Page<PeopleImage> findAll(Pageable pageable) {
        return peopleImageRepository.findAll(pageable);
    }

    /**
     * 根據多個規格查詢角色頭像
     * 
     * @param specs 查詢規格列表
     * @return 符合所有條件的角色頭像列表
     */
    public List<PeopleImage> findByMultipleSpecifications(List<Specification<PeopleImage>> specs) {
        Specification<PeopleImage> combinedSpec = specs.stream()
            .reduce(Specification.where(null), Specification::and);
        return peopleImageRepository.findAll(combinedSpec);
    }

    /**
     * 根據代碼名稱列表查詢角色頭像
     *
     * @param codeNames 代碼名稱列表
     * @return 符合條件的角色頭像列表
     */
    public List<PeopleImage> findByCodeNames(List<String> codeNames) {
        return codeNames.stream()
            .map(peopleImageRepository::findByCodeName)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toList());
    }
}
