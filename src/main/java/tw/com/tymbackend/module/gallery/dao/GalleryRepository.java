package tw.com.tymbackend.module.gallery.dao;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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

/**
 * 專門配置 Gallery 模組的 repository
 * 使用 IntegerPkRepositoryImpl 作為基礎類別
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "tw.com.tymbackend.module.gallery.dao",
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager",
    repositoryImplementationPostfix = "Impl",
    repositoryBaseClass = tw.com.tymbackend.core.repository.IntegerPkRepositoryImpl.class
)
class GalleryRepositoryConfig {
    // Gallery repository configuration
}
