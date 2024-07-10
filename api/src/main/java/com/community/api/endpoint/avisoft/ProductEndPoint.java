package com.community.api.endpoint.avisoft;
import com.broadleafcommerce.rest.api.endpoint.BaseEndpoint;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.broadleafcommerce.core.catalog.domain.Product;
import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/productcustom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)

public class ProductEndPoint  {

    private static final Logger logger = LoggerFactory.getLogger(ProductEndPoint.class);

    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;
    @RequestMapping(value = "getProducts/{productId}", method = RequestMethod.GET)
    public ResponseEntity<String> retrieveProductById(@PathVariable("productId") Long productId) {
        logger.debug("Retrieving product by ID: {}", productId);
        if (productId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            final Product product = catalogService.findProductById(productId);

            if (product != null) {
                return new ResponseEntity<>(product.getName(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            logger.error("Error retrieving product: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}