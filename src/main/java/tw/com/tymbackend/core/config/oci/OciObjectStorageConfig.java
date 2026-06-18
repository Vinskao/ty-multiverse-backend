package tw.com.tymbackend.core.config.oci;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Builds the {@link ObjectStorage} client only when {@code oci.object-storage.enabled=true}.
 *
 * <p>In the OKE cluster the pod authenticates via Instance Principal (no stored keys). For local
 * testing against the real bucket, set {@code oci.object-storage.auth=config_file} to use
 * {@code ~/.oci/config}. When the feature is disabled no client bean is created and the service
 * falls back to the bundled classpath JSON.</p>
 */
@Configuration
public class OciObjectStorageConfig {

    private static final Logger logger = LoggerFactory.getLogger(OciObjectStorageConfig.class);

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "oci.object-storage", name = "enabled", havingValue = "true")
    public ObjectStorage objectStorageClient(OciObjectStorageProperties properties) {
        BasicAuthenticationDetailsProvider provider;
        if ("config_file".equalsIgnoreCase(properties.getAuth())) {
            logger.info("Initializing OCI ObjectStorage client with config-file auth");
            try {
                provider = new ConfigFileAuthenticationDetailsProvider("DEFAULT");
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load OCI config file (~/.oci/config)", e);
            }
        } else {
            logger.info("Initializing OCI ObjectStorage client with instance-principal auth");
            provider = InstancePrincipalsAuthenticationDetailsProvider.builder().build();
        }

        ObjectStorageClient client = ObjectStorageClient.builder().build(provider);
        client.setRegion(Region.fromRegionId(properties.getRegion()));
        return client;
    }
}
