package tw.com.tymbackend.core.config.oci;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the {@code oci.object-storage.*} settings used to read the research-zone
 * company-product mapping JSON from an OCI private bucket.
 */
@Configuration
@ConfigurationProperties(prefix = "oci.object-storage")
public class OciObjectStorageProperties {

    /** When false, the service reads the bundled classpath fallback instead of OCI. */
    private boolean enabled = false;

    /** OCI region identifier, e.g. {@code ap-singapore-2}. */
    private String region = "ap-singapore-2";

    /** Object Storage namespace. */
    private String namespace;

    /** Bucket name. */
    private String bucket;

    /** Object name to read. */
    private String objectName = "company-product-mapping.json";

    /** Authentication method: {@code instance_principal} or {@code config_file}. */
    private String auth = "instance_principal";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}
