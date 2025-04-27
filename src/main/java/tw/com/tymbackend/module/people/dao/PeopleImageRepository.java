package tw.com.tymbackend.module.people.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;

@Repository
@SuppressWarnings("DOMAIN_ID_FOR_REPOSITORY")
public interface PeopleImageRepository extends JpaRepository<PeopleImage, String> {
    
    /**
     * Find people image by code name
     * 
     * @param codeName the code name of the person
     * @return the people image entity
     */
    PeopleImage findByCodeName(String codeName);
    
    /**
     * Check if an image exists for the given code name
     * 
     * @param codeName the code name of the person
     * @return true if image exists, false otherwise
     */
    boolean existsByCodeName(String codeName);
}
