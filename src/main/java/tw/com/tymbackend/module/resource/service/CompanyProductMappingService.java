package tw.com.tymbackend.module.resource.service;

import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.core.config.oci.OciObjectStorageProperties;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Reads the research-zone company-product mapping JSON.
 *
 * <p>When OCI Object Storage is enabled the JSON is fetched from the private bucket; otherwise (or
 * on any read failure) it falls back to the JSON bundled on the classpath at
 * {@code data/company-product-mapping.json}. The result is cached for one hour (see
 * {@code RedisConfig}).</p>
 */
@Service
public class CompanyProductMappingService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyProductMappingService.class);
    private static final String CLASSPATH_FALLBACK = "data/company-product-mapping.json";
    public static final String CACHE_NAME = "company-product-mapping";

    private final OciObjectStorageProperties properties;
    private final ObjectStorage objectStorage;

    public CompanyProductMappingService(OciObjectStorageProperties properties,
                                        @Autowired(required = false) ObjectStorage objectStorage) {
        this.properties = properties;
        this.objectStorage = objectStorage;
    }

    /**
     * Returns the mapping JSON as a raw string. Cached so OCI is hit at most once per hour.
     */
    @Cacheable(value = CACHE_NAME, key = "'mapping'")
    public String getCompanyProductMappingJson() {
        if (properties.isEnabled() && objectStorage != null) {
            try {
                return readFromOci();
            } catch (Exception e) {
                logger.warn("Failed to read company-product mapping from OCI ({}/{}); using classpath fallback",
                        properties.getBucket(), properties.getObjectName(), e);
            }
        }
        return readFromClasspath();
    }

    /** Clears the cache so the next request re-reads from OCI (use after publishing new data). */
    @CacheEvict(value = CACHE_NAME, key = "'mapping'")
    public void evictCache() {
        logger.info("Evicted company-product-mapping cache");
    }

    private String readFromOci() throws Exception {
        GetObjectRequest request = GetObjectRequest.builder()
                .namespaceName(properties.getNamespace())
                .bucketName(properties.getBucket())
                .objectName(properties.getObjectName())
                .build();
        GetObjectResponse response = objectStorage.getObject(request);
        try (InputStream input = response.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String readFromClasspath() {
        try (InputStream input = new ClassPathResource(CLASSPATH_FALLBACK).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Missing classpath fallback " + CLASSPATH_FALLBACK, e);
        }
    }
}
