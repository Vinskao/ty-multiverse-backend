package tw.com.tymbackend.module.people.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.core.factory.QueryConditionFactory;
import tw.com.tymbackend.core.factory.RepositoryFactory;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;

// 人像服務
@Service
public class PeopleImageService {
    
    private final RepositoryFactory repositoryFactory;
    private final QueryConditionFactory queryConditionFactory;
    
    public PeopleImageService(RepositoryFactory repositoryFactory,
                            QueryConditionFactory queryConditionFactory) {
        this.repositoryFactory = repositoryFactory;
        this.queryConditionFactory = queryConditionFactory;
    }
    
    /**
     * Get all people images
     * 
     * @return list of all people images
     */
    public List<PeopleImage> getAllPeopleImages() {
        return repositoryFactory.findAll(PeopleImage.class);
    }
    
    /**
     * Get people image by code name
     * 
     * @param codeName the code name of the person
     * @return the people image
     * @throws NoSuchElementException if no image is found with the given code name
     */
    public PeopleImage getPeopleImageByCodeName(String codeName) {
        Optional<PeopleImage> peopleImage = repositoryFactory.findById(PeopleImage.class, codeName);
        return peopleImage.orElseThrow(() -> new NoSuchElementException("No image found for code name: " + codeName));
    }
    
    /**
     * Save or update a people image
     * 
     * @param peopleImage the people image to save or update
     * @return the saved people image
     */
    @Transactional
    public PeopleImage savePeopleImage(PeopleImage peopleImage) {
        return repositoryFactory.save(peopleImage);
    }
    
    /**
     * Delete a people image by code name
     * 
     * @param codeName the code name of the person whose image is to be deleted
     * @throws NoSuchElementException if no image is found with the given code name
     */
    @Transactional
    public void deletePeopleImage(String codeName) {
        if (!repositoryFactory.findById(PeopleImage.class, codeName).isPresent()) {
            throw new NoSuchElementException("No image found for code name: " + codeName);
        }
        repositoryFactory.deleteById(PeopleImage.class, codeName);
    }
    
    /**
     * Check if a people image exists
     * 
     * @param codeName the code name of the person
     * @return true if image exists, false otherwise
     */
    public boolean peopleImageExists(String codeName) {
        return repositoryFactory.findById(PeopleImage.class, codeName).isPresent();
    }

    /**
     * Find people images by code name containing (case insensitive)
     * 
     * @param codeNamePart part of the code name to search for
     * @return list of people images with code names containing the specified part
     */
    public List<PeopleImage> findByCodeNameContaining(String codeNamePart) {
        Specification<PeopleImage> spec = queryConditionFactory.createLikeCondition("codeName", codeNamePart);
        return repositoryFactory.getSpecificationRepository(PeopleImage.class, String.class).findAll(spec);
    }

    /**
     * Find all people images with pagination
     * 
     * @param pageable the pageable
     * @return the page of people images
     */
    public Page<PeopleImage> findAll(Pageable pageable) {
        return repositoryFactory.findAll(PeopleImage.class, pageable);
    }

    /**
     * Find people images by multiple criteria
     * 
     * @param codeNamePart part of the code name to search for
     * @param hasImage whether the image should exist
     * @return list of people images matching all criteria
     */
    public List<PeopleImage> findByMultipleCriteria(String codeNamePart, boolean hasImage) {
        Specification<PeopleImage> codeNameSpec = queryConditionFactory.createLikeCondition("codeName", codeNamePart);
        Specification<PeopleImage> imageSpec = queryConditionFactory.createIsNotNullCondition("image");
        Specification<PeopleImage> combinedSpec = queryConditionFactory.createCompositeCondition(
            codeNameSpec, 
            hasImage ? imageSpec : queryConditionFactory.createIsNullCondition("image")
        );
        return repositoryFactory.getSpecificationRepository(PeopleImage.class, String.class).findAll(combinedSpec);
    }
}
