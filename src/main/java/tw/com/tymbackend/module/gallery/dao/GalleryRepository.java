package tw.com.tymbackend.module.gallery.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Integer> {
}
