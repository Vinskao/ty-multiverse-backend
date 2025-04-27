package tw.com.tymbackend.module.gallery.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

/**
 * Repository for Gallery entity
 */
@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Integer>, JpaSpecificationExecutor<Gallery>, GalleryRepositoryCustom {
    // Custom methods are now defined in GalleryRepositoryCustom
}
