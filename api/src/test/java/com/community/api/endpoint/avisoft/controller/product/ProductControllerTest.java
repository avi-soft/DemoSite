//package com.community.api.endpoint.avisoft.controller.product;
//
//import com.community.api.entity.CustomProduct;
//import com.community.api.services.ProductService;
//import com.community.api.services.exception.ExceptionHandlingService;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.broadleafcommerce.common.money.Money;
//import org.broadleafcommerce.core.catalog.domain.*;
//import org.broadleafcommerce.core.catalog.service.CatalogService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.validation.Errors;
//import org.springframework.validation.MapBindingResult;
//
//import javax.persistence.EntityManager;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//class ProductControllerTest {
//
//    private MockMvc mockMvc;
//
//    @InjectMocks
//    private ProductController productController; // The class containing the retrieveProducts method
//
//    @Mock
//    private CatalogService catalogService; // Mock the CatalogService
//
//    @Mock
//    private EntityManager entityManager; // Mock the EntityManager
//
//    @Mock
//    private ExceptionHandlingService exceptionHandlingService;
//
//    @Mock
//    ProductService productService;
//
//    List<Product> products = new ArrayList<>();
//
//    @BeforeEach
//    public void setUp() {
//
//        MockitoAnnotations.openMocks(this);
//        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
//    }
//
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    // Utility method to parse JSON string
//    private List<Map<String, Object>> parseJson(String json) throws IOException {
//        return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
//        });
//    }
//
//    private Map<String, Object> parseJson2(String json) throws IOException {
//        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
//        });
//    }
//
//    @Test
//    void addProduct_Success() throws Exception {
//        ProductImpl productImpl = new ProductImpl();
//        productImpl.setMetaTitle("New Product Title");
//        productImpl.setMetaDescription("New Product Description");
//        productImpl.setUrl("http://newproduct.url");
//
//        Category category = new CategoryImpl();
//        category.setId(1001L);
//
//        Sku sku = new SkuImpl();
//        sku.setId(15L);
//
//        // Mock service methods
//        when(catalogService.findCategoryById(1001L)).thenReturn(category);
//        when(catalogService.saveProduct(any(Product.class))).thenReturn(productImpl);
//        when(catalogService.findSkuById(15L)).thenReturn(sku); // Ensure this matches the SKU ID used in your test
//        when(catalogService.createSku()).thenReturn(sku); // Ensure this is set up if necessary
//        when(catalogService.saveSku(any(Sku.class))).thenReturn(sku);
//        doNothing().when(productService).saveCustomProduct(any(Date.class), anyInt(), anyLong());
//
//        String requestBody = "{"
//                + "\"metaTitle\": \"New Product Title\","
//                + "\"metaDescription\": \"New Product Description\","
//                + "\"url\": \"http://newproduct.url\""
//                + "}";
//
//        MvcResult mvcResult = mockMvc.perform(post("/productCustom/add")
//                        .param("expirationDate", "2024-12-31 23:59:59")
//                        .param("goLiveDate", "2024-10-01 00:00:00")
//                        .param("priorityLevel", "5")
//                        .param("categoryId", "1001")
//                        .param("skuId", "15")
//                        .param("quantity", "20")
//                        .param("cost", "200.00")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Product added successfully"))
//                .andReturn();
//
//        // Verify service methods were called with correct arguments
//        verify(catalogService).findCategoryById(1001L);
//        verify(catalogService).saveProduct(productImpl); // Ensure productImpl matches
//        verify(catalogService).findSkuById(15L);
//        verify(catalogService).saveSku(sku); // Ensure sku matches
//        verify(productService).saveCustomProduct(any(Date.class), eq(5), eq(productImpl.getId())); // Ensure priorityLevel and product ID are correctly used
//
//    }
//
//    @Test
//    void addProduct_InvalidCost() throws Exception {
//        String requestBody = "{"
//                + "\"metaTitle\": \"New Product Title\","
//                + "\"metaDescription\": \"New Product Description\","
//                + "\"url\": \"http://newproduct.url\""
//                + "}";
//
//        MvcResult mvcResult = mockMvc.perform(post("/productCustom/add")
//                        .param("expirationDate", "2024-12-31 23:59:59")
//                        .param("goLiveDate", "2024-10-01 00:00:00")
//                        .param("priorityLevel", "5")
//                        .param("categoryId", "1001")
//                        .param("skuId", "15")
//                        .param("quantity", "20")
//                        .param("cost", "0.00") // Invalid cost
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isInternalServerError())
//                .andExpect(content().string("Cost cannot be <= 0"))
//                .andReturn();
//    }
//
//    @Test
//    void addProduct_InvalidCategoryId() throws Exception {
//        when(catalogService.findCategoryById(1001L)).thenReturn(null); // Simulate category not found
//
//        String requestBody = "{"
//                + "\"metaTitle\": \"New Product Title\","
//                + "\"metaDescription\": \"New Product Description\","
//                + "\"url\": \"http://newproduct.url\""
//                + "}";
//
//        MvcResult mvcResult = mockMvc.perform(post("/productCustom/add")
//                        .param("expirationDate", "2024-12-31 23:59:59")
//                        .param("goLiveDate", "2024-10-01 00:00:00")
//                        .param("priorityLevel", "5")
//                        .param("categoryId", "1001") // Invalid category ID
//                        .param("skuId", "15")
//                        .param("quantity", "20")
//                        .param("cost", "200.00")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isInternalServerError())
//                .andReturn();
//    }
//
//
//    @Test
//    void retrieveProducts_Success() throws Exception {
//        // Initialize mock data
//        Product product = new ProductImpl();
//        product.setId(1001L);
//
//        Sku sku = new SkuImpl();
//        sku.setId(1001L);
//
//        Category category = new CategoryImpl();
//        category.setId(1001L);
//
//        CustomProduct customProduct = new CustomProduct(new Date(), 3);
//        customProduct.setId(1001L);
//        customProduct.setDefaultSku(sku);
//        customProduct.getDefaultSku().setDefaultProduct(product);
//        customProduct.setDefaultCategory(category);
//        customProduct.getDefaultCategory().getId();
//        customProduct.getDefaultSku().setCost(new Money());
//
//        products.add(product);
//
//        // Mock behavior
//        when(catalogService.findAllProducts()).thenReturn(products);
//        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(customProduct);
//        // Perform request and verify response
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/productCustom/getAllProducts")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk()) // Ensure the status is OK
//                .andReturn();
//
//        // Extract and parse response
//        String content = mvcResult.getResponse().getContentAsString();
//        assertNotNull(content);
//        System.out.println("This is the msg: " + content);
//
//        List<Map<String, Object>> responseBody = parseJson(content);
//        assertNotNull(responseBody);
//        System.out.println(responseBody.size());
//        assertEquals(1, responseBody.size());
//
//        Map<String, Object> productResponse = responseBody.get(0);
//        assertEquals(customProduct.getId().toString(), productResponse.get("productId").toString());
//    }
//
//    @Test
//    void retrieveProducts_Failure_NoProductsFound() throws Exception {
//        // Mock behavior
//        when(catalogService.findAllProducts()).thenReturn(Collections.emptyList()); // Simulate no products found
//
//        // Perform request and verify response
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/productCustom/getAllProducts")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError()) // Expect 404 Not Found
//                .andReturn();
//
//        // Verify response
//        String content = mvcResult.getResponse().getContentAsString();
//
//        // Verify interactions
//        verify(catalogService).findAllProducts();
//        verifyNoInteractions(entityManager); // Ensure entityManager is not called
//    }
//
//
//    @Test
//    void retrieveProductById_Success() throws Exception {
//        // Initialize mock data
//        Product product = new ProductImpl();
//        product.setId(1001L);
//
//        Sku sku = new SkuImpl();
//        sku.setId(1001L);
//        sku.setDefaultProduct(product);
//        sku.setCost(new Money(150.00)); // Assuming Money has a constructor for amount
//
//        Category category = new CategoryImpl();
//        category.setId(1001L);
//        category.setName("Sample Category");
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String formattedDate = dateFormat.format(new Date());
//        Date goLiveDate = dateFormat.parse(formattedDate);
//
//        CustomProduct customProduct = new CustomProduct(new Date(), 3);
//        customProduct.setId(1001L);
//        customProduct.setDefaultSku(sku);
//        customProduct.setDefaultCategory(category);
//        customProduct.setArchived('N');
//        customProduct.setMetaTitle("Sample Meta Title");
//        customProduct.setGoLiveDate(goLiveDate);
//        customProduct.setPriorityLevel(1);
//
//        // Mock behavior
//        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(customProduct);
//        when(catalogService.findProductById(1001L)).thenReturn(product);
//
//        // Perform request and verify response
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/productCustom/getProductById/{productId}", 1001L)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk()) // Ensure the status is OK
//                .andReturn();
//
//        // Extract and parse response
//        String content = mvcResult.getResponse().getContentAsString();
//        assertNotNull(content);
//        System.out.println("This is the response: " + content);
//
//        // Parse the JSON response into a Map
//        Map<String, Object> responseBody = parseJson2(content);
//        assertNotNull(responseBody);
//
//
//        // Verify response content
//        assertEquals(customProduct.getId().toString(), responseBody.get("productId").toString());
//        assertEquals(customProduct.getArchived().toString(), responseBody.get("archived").toString());
//        assertEquals(customProduct.getMetaTitle(), responseBody.get("metaTitle"));
//        assertEquals(customProduct.getDefaultSku().getCost().doubleValue(), responseBody.get("cost"));
//        assertEquals(customProduct.getDefaultCategory().getId().toString(), responseBody.get("defaultCategoryId").toString());
//        assertEquals(customProduct.getDefaultCategory().getName(), responseBody.get("categoryName"));
//        assertEquals(customProduct.getDefaultSku().getActiveStartDate(), responseBody.get("ActiveCreatedDate"));
//        assertEquals(customProduct.getDefaultSku().getActiveEndDate(), responseBody.get("ActiveExpirationDate"));
//
//        // Extract and format dates for comparison
//        Date expectedGoLiveDate = customProduct.getGoLiveDate();
//        String expectedGoLiveDateStr = dateFormat.format(expectedGoLiveDate);
//
//        // Format actual date for comparison
//        String actualGoLiveDateStr = dateFormat.format(goLiveDate);
//
//        // Compare dates
//        assertEquals(expectedGoLiveDateStr, actualGoLiveDateStr);
//
//        assertEquals(customProduct.getPriorityLevel(), responseBody.get("priorityLevel"));
//    }
//
//    @Test
//    void retrieveProductById_Failure_ProductNotFound() throws Exception {
//        // Mock behavior
//        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(null); // Simulate product not found
//
//        // Perform request and verify response
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/productCustom/getProductById/{productId}", 1001L)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError()) // Expect 500 Internal Server Error.
//                .andReturn();
//
//        // Verify response
//        String content = mvcResult.getResponse().getContentAsString();
//
//        // Verify interactions
//        verify(entityManager).find(CustomProduct.class, 1001L);
//        verifyNoInteractions(catalogService); // Ensure catalogService is not called
//    }
//
//
//    @Test
//    void deleteProduct_Success() throws Exception {
//        // Initialize mock data
//        Product product = new ProductImpl();
//        product.setId(1001L);
//
//        Sku sku = new SkuImpl();
//        sku.setId(1001L);
//        sku.setDefaultProduct(product);
//
//        CustomProduct customProduct = new CustomProduct(new Date(), 3);
//        customProduct.setId(1001L);
//        customProduct.setDefaultSku(sku);
//
//        // Mock behaviors
//        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(customProduct);
//        doNothing().when(catalogService).removeProduct(product);
//
//        // Perform request and verify response
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/productCustom/delete/{productId}", 1001L)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Product Deleted Successfully"))
//                .andReturn();
//
//        // Verify interactions
//        verify(entityManager).find(CustomProduct.class, 1001L);
//        verify(catalogService).removeProduct(product);
//        verifyNoInteractions(exceptionHandlingService); // Ensure exceptionHandlingService is not called
//    }
//
//    @Test
//    void deleteProduct_ProductNotFound() throws Exception {
//        // Mock behaviors
//        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(null);
//
//        // Perform request and verify response
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/productCustom/delete/{productId}", 1001L)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError())
//                .andReturn();
//
//        // Verify interactions
//        verify(entityManager).find(CustomProduct.class, 1001L);
//    }
//
//
//    @Test
//    void updateProduct_Success() throws Exception {
//        // Mock data
//        ProductImpl productImpl = new ProductImpl();
//        productImpl.setMetaTitle("Updated Product Title");
//        productImpl.setMetaDescription("Updated Product Description");
//        productImpl.setUrl("http://updatedproduct.url");
//
//        Category newCategory = new CategoryImpl();
//        newCategory.setId(1001L); // New category ID for testing
//
//        Category oldCategory = new CategoryImpl();
//        oldCategory.setId(999L); // Old category ID
//
//        Product product = new ProductImpl();
//        product.setDefaultCategory(oldCategory); // Initially set to old category
//        product.setMetaTitle("Old Title");
//        product.setMetaDescription("Old Description");
//        product.setUrl("http://oldurl.com");
//
//        CustomProduct customProduct = new CustomProduct();
//        Sku sku = new SkuImpl();
//        product.setDefaultSku(sku);
//        sku.setDefaultProduct(product);
//
//        Errors errors = new MapBindingResult(new HashMap<>(), "objectName");
//
//        // Mock methods
//        when(catalogService.findProductById(1L)).thenReturn(product);
//        when(catalogService.findCategoryById(1001L)).thenReturn(newCategory); // Return new category
//        when(catalogService.saveProduct(any(Product.class))).thenReturn(product);
//        doNothing().when(productService).removeCategoryProductFromCategoryProductRefTable(anyLong(), anyLong());
//        when(productService.validatePriorityLevel(anyInt())).thenReturn(errors);
//        when(entityManager.find(CustomProduct.class, 1L)).thenReturn(customProduct);
//
//        String requestBody = "{"
//                + "\"metaTitle\": \"Updated Product Title\","
//                + "\"metaDescription\": \"Updated Product Description\","
//                + "\"url\": \"http://updatedproduct.url\""
//                + "}";
//
//        MvcResult mvcResult = mockMvc.perform(put("/productCustom/update/1")
//                        .param("expirationDate", "2024-12-31 23:59:59")
//                        .param("goLiveDate", "2024-10-01 00:00:00")
//                        .param("priorityLevel", "3")
//                        .param("categoryId", "1001") // New category ID
//                        .param("quantity", "50")
//                        .param("cost", "150")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Product Updated Successfully"))
//                .andReturn();
//
//        // Verify interactions
//        verify(catalogService).findProductById(1L);
//        verify(catalogService).findCategoryById(1001L);
//        verify(catalogService).saveProduct(any(Product.class));
//        verify(productService).removeCategoryProductFromCategoryProductRefTable(anyLong(), anyLong()); // Verify call
//        verify(entityManager).find(CustomProduct.class, 1L);
//    }
//
//    @Test
//    void updateProduct_ProductNotFound() throws Exception {
//        // Mock behaviors
//        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(null);
//
//        // Prepare request
//        String requestBody = "{}";
//
//        // Perform request and verify response
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/productCustom/update/{productId}", 1001L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isInternalServerError())
//                .andExpect(content().string("Product not Found"))
//                .andReturn();
//
//        // Verify interactions
//        verify(entityManager).find(CustomProduct.class, 1001L);
//        verifyNoInteractions(catalogService); // Ensure catalogService is not called
//        verifyNoInteractions(productService); // Ensure productService is not called
//    }
//}