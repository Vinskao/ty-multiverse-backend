package tw.com.tymbackend.module.gallery.dao;

import org.springframework.stereotype.Repository;

import tw.com.tymbackend.core.repository.IntegerPkRepository;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

/**
 * Repository for Gallery entity
 */
@Repository
public interface GalleryRepository extends IntegerPkRepository<Gallery> {
    // Custom methods are now defined in GalleryRepositoryCustom
}
