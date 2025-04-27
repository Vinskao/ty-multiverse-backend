package tw.com.tymbackend.module.people.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.core.repository.DataAccessor;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;

// 人像服務
@Service
public class PeopleImageService {
    
    private final DataAccessor<PeopleImage, String> peopleImageDataAccessor;
    
    public PeopleImageService(DataAccessor<PeopleImage, String> peopleImageDataAccessor) {
        this.peopleImageDataAccessor = peopleImageDataAccessor;
    }
    
    /**
     * Get all people images
     * 
     * @return list of all people images
     */
    public List<PeopleImage> getAllPeopleImages() {
        return peopleImageDataAccessor.findAll();
    }
    
    /**
     * Get people image by code name
     * 
     * @param codeName the code name of the person
     * @return the people image
     * @throws NoSuchElementException if no image is found with the given code name
     */
    public PeopleImage getPeopleImageByCodeName(String codeName) {
        return peopleImageDataAccessor.findAll(
            (root, query, cb) -> cb.equal(root.get("codeName"), codeName)
        ).stream()
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("No image found for code name: " + codeName));
    }
    
    /**
     * Save or update a people image
     * 
     * @param peopleImage the people image to save or update
     * @return the saved people image
     */
    @Transactional
    public PeopleImage savePeopleImage(PeopleImage peopleImage) {
        return peopleImageDataAccessor.save(peopleImage);
    }
    
    /**
     * Delete a people image by code name
     * 
     * @param codeName the code name of the person whose image is to be deleted
     * @throws NoSuchElementException if no image is found with the given code name
     */
    @Transactional
    public void deletePeopleImage(String codeName) {
        PeopleImage image = getPeopleImageByCodeName(codeName);
        peopleImageDataAccessor.deleteById(image.getCodeName());
    }
    
    /**
     * Check if a people image exists
     * 
     * @param codeName the code name of the person
     * @return true if image exists, false otherwise
     */
    public boolean peopleImageExists(String codeName) {
        return !peopleImageDataAccessor.findAll(
            (root, query, cb) -> cb.equal(root.get("codeName"), codeName)
        ).isEmpty();
    }

    /**
     * Find people images by code name containing (case insensitive)
     * 
     * @param codeNamePart part of the code name to search for
     * @return list of people images with code names containing the specified part
     */
    public List<PeopleImage> findByCodeNameContaining(String codeNamePart) {
        Specification<PeopleImage> spec = (root, query, cb) -> 
            cb.like(cb.lower(root.get("codeName")), "%" + codeNamePart.toLowerCase() + "%");
        return peopleImageDataAccessor.findAll(spec);
    }

    /**
     * Find all people images with pagination
     * 
     * @param pageable the pageable
     * @return the page of people images
     */
    public Page<PeopleImage> findAll(Pageable pageable) {
        return peopleImageDataAccessor.findAll(pageable);
    }

    /**
     * Find people images by multiple criteria
     * 
     * @param codeNamePart part of the code name to search for
     * @param hasImage whether the image should exist
     * @return list of people images matching all criteria
     */
    public List<PeopleImage> findByMultipleCriteria(String codeNamePart, boolean hasImage) {
        List<Specification<PeopleImage>> specs = new ArrayList<>();
        
        if (codeNamePart != null) {
            specs.add((root, query, cb) -> 
                cb.like(cb.lower(root.get("codeName")), "%" + codeNamePart.toLowerCase() + "%")
            );
        }
        
        specs.add((root, query, cb) -> 
            hasImage ? cb.isNotNull(root.get("image")) : cb.isNull(root.get("image"))
        );
        
        Specification<PeopleImage> combinedSpec = specs.stream()
            .reduce(Specification.where(null), Specification::and);
            
        return peopleImageDataAccessor.findAll(combinedSpec);
    }
}
