package tw.com.tymbackend.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import tw.com.tymbackend.core.repository.DataAccessor;
import tw.com.tymbackend.core.repository.RepositoryDataAccessor;

// People module
import tw.com.tymbackend.module.people.dao.PeopleRepository;
import tw.com.tymbackend.module.people.dao.PeopleImageRepository;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;

// Weapon module
import tw.com.tymbackend.module.weapon.dao.WeaponRepository;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

// Gallery module
import tw.com.tymbackend.module.gallery.dao.GalleryRepository;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

// Livestock module
import tw.com.tymbackend.module.livestock.dao.LivestockRepository;
import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

// CKEditor module
import tw.com.tymbackend.module.ckeditor.dao.EditContentRepository;
import tw.com.tymbackend.module.ckeditor.dao.EditContentVORepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

import java.util.List;
import java.util.Optional;

@Configuration
public class DataAccessorConfig {

    // People module
    @Bean
    public DataAccessor<People, Long> peopleDataAccessor(PeopleRepository repository) {
        return RepositoryDataAccessor.create(repository);
    }

    @Bean
    public DataAccessor<PeopleImage, String> peopleImageDataAccessor(PeopleImageRepository repository) {
        return RepositoryDataAccessor.create(repository);
    }

    // Weapon module
    @Bean
    public DataAccessor<Weapon, String> weaponDataAccessor(WeaponRepository repository) {
        return RepositoryDataAccessor.create(repository);
    }

    // Gallery module - using a dummy implementation to break the circular dependency
    @Bean
    public DataAccessor<Gallery, Integer> galleryDataAccessor() {
        return new DummyGalleryDataAccessor();
    }

    // Livestock module
    @Bean
    public DataAccessor<Livestock, Long> livestockDataAccessor(LivestockRepository repository) {
        return RepositoryDataAccessor.create(repository);
    }

    // CKEditor module
    @Bean
    public DataAccessor<EditContentVO, String> editContentVODataAccessor(EditContentVORepository repository) {
        return RepositoryDataAccessor.create(repository);
    }
    
    // Dummy implementation to break circular dependency
    private static class DummyGalleryDataAccessor implements DataAccessor<Gallery, Integer> {
        @Override
        public List<Gallery> findAll() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Optional<Gallery> findById(Integer id) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Gallery save(Gallery entity) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public List<Gallery> saveAll(List<Gallery> entities) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void deleteById(Integer id) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public List<Gallery> findAll(Specification<Gallery> spec) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Page<Gallery> findAll(Pageable pageable) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Page<Gallery> findAll(Specification<Gallery> spec, Pageable pageable) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
} 