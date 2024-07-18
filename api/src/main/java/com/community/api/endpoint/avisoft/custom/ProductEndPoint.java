package com.community.api.endpoint.avisoft.custom;
import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.community.api.endpoint.avisoft.CustomCategoryEndpoint;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.broadleafcommerce.core.catalog.domain.Category;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.Date;

@RestController
@RequestMapping(value = "/productcustom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class ProductEndPoint extends CatalogEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomCategoryEndpoint.class);

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected ExtProductService extProductService;

    @Autowired
    protected CatalogService catalogService;

    /*



     */

    @Transactional
    @RequestMapping(value = "/add/{categoryName}", method = RequestMethod.POST)
    public ResponseEntity<String> addProduct(@RequestBody CustomProduct customProduct, @PathVariable("categoryName") String categoryName) throws ParseException {

        Product product = null;
        Category category = null;

        category = this.catalogService.createCategory();
//        category.setName("CategoryName"); // REQUIRED FOR CATEGORY TO BE CREATED
        category.setName(categoryName); // REQUIRED FOR CATEGORY TO BE CREATED
        category = catalogService.saveCategory(category);

        product = this.catalogService.createProduct(ProductType.PRODUCT);
        product.setDefaultCategory(category);

        product = catalogService.saveProduct(product);
        Date created = customProduct.getCreated_date();

        if (customProduct.getCreated_date() == null) {
            customProduct.setCreated_date(new Date());
        }
        extProductService.saveExtProduct(created, customProduct.getExpiration_date(), customProduct.getGo_live_date(), product.getId());

//        else{
//            product = catalogService.findProductById(customProduct.getId());
//            extProductService.saveExtProduct(customProduct.getCreated_date(), customProduct.getExpiration_date(), customProduct.getGo_live_date(), product.getId());
//        }

//        Category category = null;
//        if(customProduct.getDefaultCategory() == null){
//            category =  this.catalogService.findCategoryById(customProduct.getDefaultCategory().getId());
//            category.setName("New Grocery");
//            category = catalogService.saveCategory(category);
//        }else{
//            category = customProduct.getDefaultCategory();
//        }
//
//        Product product;
//        if(customProduct.getId() == null){
//            product = this.catalogService.createProduct(ProductType.PRODUCT);
//            product.setName("NewProduct");
//
//        }else{
//            product = catalogService.findProductById(customProduct.getId());
//
//        }
//        product.setDefaultCategory(category);
//        product = catalogService.saveProduct(product);
//
//        extProductService.saveExtProduct(customProduct.getCreated_date(), customProduct.getExpiration_date(), customProduct.getGo_live_date(), product.getId());

        return ResponseEntity.ok("Data Successfully Added");
    }

    @RequestMapping(value = "getProducts/{productId}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveProductById(HttpServletRequest request, @PathVariable("productId") Long productId) {
        CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
        return ResponseEntity.ok(customProduct.getGo_live_date());
    }



    /*@Transactional
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addProduct(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expirationDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime goLiveDate,
            @RequestParam Long productId
    ){

//        entityManager.merge(customProduct);

        extProductService.saveExtProduct(createdDate, expirationDate, goLiveDate, productId);

        return "added";
    }

    @RequestMapping(value = "getProducts/{productId}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveProductById(HttpServletRequest request, @PathVariable("productId") Long productId) {

        if (productId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {

            if (catalogService == null) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            final CustomProduct customProduct = entityManager.find(CustomProduct.class,productId);
            System.out.println(customProduct.getExpiration_date());
            if (customProduct == null) {
                logger.error("Error retrieving product as There is no product in DB with this Id");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound");
            }
            return ResponseEntity.ok(customProduct);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }*/

}
