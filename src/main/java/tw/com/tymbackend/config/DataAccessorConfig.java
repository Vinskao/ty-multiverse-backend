package tw.com.tymbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

@Configuration
public class DataAccessorConfig {

    // People module
    @Bean
    public DataAccessor<People, Long> peopleDataAccessor(PeopleRepository repository) {
        return new RepositoryDataAccessor<People, Long>(repository);
    }

    @Bean
    public DataAccessor<PeopleImage, String> peopleImageDataAccessor(PeopleImageRepository repository) {
        return new RepositoryDataAccessor<PeopleImage, String>(repository);
    }

    // Weapon module
    @Bean
    public DataAccessor<Weapon, String> weaponDataAccessor(WeaponRepository repository) {
        return new RepositoryDataAccessor<Weapon, String>(repository);
    }

    // Gallery module
    @Bean
    public DataAccessor<Gallery, Integer> galleryDataAccessor(GalleryRepository repository) {
        return new RepositoryDataAccessor<Gallery, Integer>(repository);
    }

    // Livestock module
    @Bean
    public DataAccessor<Livestock, Long> livestockDataAccessor(LivestockRepository repository) {
        return new RepositoryDataAccessor<Livestock, Long>(repository);
    }

    // CKEditor module
    @Bean
    public DataAccessor<EditContentVO, String> editContentVODataAccessor(EditContentVORepository repository) {
        return new RepositoryDataAccessor<EditContentVO, String>(repository);
    }
} 