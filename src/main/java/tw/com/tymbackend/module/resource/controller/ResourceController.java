package tw.com.tymbackend.module.resource.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.com.ty.common.response.BackendApiResponse;
import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.MessageKey;
import tw.com.tymbackend.module.resource.service.CompanyProductMappingService;

/**
 * Public resource endpoints. Proxies static research-zone data so the frontend never touches the
 * OCI private bucket directly.
 */
@RestController
@RequestMapping("/resources")
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    private final CompanyProductMappingService mappingService;
    private final ObjectMapper objectMapper;

    public ResourceController(CompanyProductMappingService mappingService, ObjectMapper objectMapper) {
        this.mappingService = mappingService;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns the company-product mapping JSON consumed by the frontend Research Zone.
     */
    @GetMapping("/company-product-mapping")
    public ResponseEntity<?> getCompanyProductMapping() {
        try {
            JsonNode mapping = objectMapper.readTree(mappingService.getCompanyProductMappingJson());
            return ResponseEntity.ok(BackendApiResponse.success(MessageKey.GET_SUCCESS, mapping));
        } catch (Exception e) {
            logger.error("Failed to load company-product mapping", e);
            return ResponseEntity.status(500)
                    .body(BackendApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }
}
