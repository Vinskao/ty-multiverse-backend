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

// 人像服務
@Service
@Transactional(readOnly = true)
public class PeopleImageService {
    private final PeopleImageRepository peopleImageRepository;

    public PeopleImageService(PeopleImageRepository peopleImageRepository) {
        this.peopleImageRepository = peopleImageRepository;
    }

    /**
     * Get all people images
     * 
     * @return list of all people images
     */
    public List<PeopleImage> getAllPeopleImages() {
        return peopleImageRepository.findAll();
    }
    
    /**
     * Get people image by code name
     * 
     * @param codeName the code name of the person
     * @return the people image
     * @throws NoSuchElementException if the image is not found
     */
    public PeopleImage getPeopleImageByCodeName(String codeName) {
        PeopleImage image = peopleImageRepository.findByCodeName(codeName);
        if (image == null) {
            throw new NoSuchElementException("No image found for code name: " + codeName);
        }
        return image;
    }
    
    /**
     * Check if a people image exists by code name
     * 
     * @param codeName the code name of the person
     * @return true if the image exists, false otherwise
     */
    public boolean peopleImageExists(String codeName) {
        return peopleImageRepository.existsByCodeName(codeName);
    }

    /**
     * Save a people image
     * 
     * @param peopleImage the people image to save
     * @return the saved people image
     */
    @Transactional
    public PeopleImage savePeopleImage(PeopleImage peopleImage) {
        return peopleImageRepository.save(peopleImage);
    }
    
    /**
     * Delete a people image by code name
     * 
     * @param codeName the code name of the person
     * @throws NoSuchElementException if the image is not found
     */
    @Transactional
    public void deletePeopleImage(String codeName) {
        PeopleImage peopleImage = getPeopleImageByCodeName(codeName);
        peopleImageRepository.delete(peopleImage);
    }
    
    /**
     * Check if a people image exists
     * 
     * @param codeName the code name of the person
     * @return true if image exists, false otherwise
     */
    public boolean isCodeNameUnique(String codeName) {
        return !peopleImageRepository.findAll(
            (root, query, cb) -> cb.equal(root.get("codeName"), codeName)
        ).isEmpty();
    }

    /**
     * Find people images by code name containing (case insensitive)
     * 
     * @param codeNamePart part of the code name to search for
     * @return list of people images with code names containing the specified part
     */
    public List<PeopleImage> findBySpecification(Specification<PeopleImage> spec) {
        return peopleImageRepository.findAll(spec);
    }

    /**
     * Find all people images with pagination
     * 
     * @param pageable the pageable
     * @return the page of people images
     */
    public Page<PeopleImage> findAll(Pageable pageable) {
        return peopleImageRepository.findAll(pageable);
    }

    /**
     * Find people images by multiple criteria
     * 
     * @param specs list of specifications to apply
     * @return list of people images matching all criteria
     */
    public List<PeopleImage> findByMultipleSpecifications(List<Specification<PeopleImage>> specs) {
        Specification<PeopleImage> combinedSpec = specs.stream()
            .reduce(Specification.where(null), Specification::and);
        return peopleImageRepository.findAll(combinedSpec);
    }
}
