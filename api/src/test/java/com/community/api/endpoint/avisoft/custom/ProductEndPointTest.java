package com.community.api.endpoint.avisoft.custom;

import com.community.api.services.exception.ExceptionHandlingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.catalog.domain.*;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductEndPointTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ProductEndPoint productEndPoint; // The class containing the retrieveProducts method

    @Mock
    private CatalogService catalogService; // Mock the CatalogService

    @Mock
    private EntityManager entityManager; // Mock the EntityManager

    private List<Product> products;

    @Mock
    private ExceptionHandlingService exceptionHandlingService;

    @Mock
    ExtProductService extProductService;

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productEndPoint).build();
        products = new ArrayList<>();
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Utility method to parse JSON string
    private List<Map<String, Object>> parseJson(String json) throws IOException {
        return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
    }
    private Map<String, Object> parseJson2(String json) throws IOException {
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    @Test
    void addProduct_Success() throws Exception {
        // Initialize mock data
        ProductImpl productImpl = new ProductImpl();
        productImpl.setId(1001L);

        Category category = new CategoryImpl();
        category.setId(1L);
        category.setName("Electronics");

        Sku sku = new SkuImpl();
        sku.setId(2001L);
        sku.setName("Sample SKU");

        // Mock behaviors
        when(catalogService.findCategoryById(1L)).thenReturn(category);
        when(catalogService.findSkuById(2001L)).thenReturn(null); // SKU does not exist, will be created
        when(catalogService.createSku()).thenReturn(sku);
        when(catalogService.saveProduct(any(ProductImpl.class))).thenReturn(productImpl);
        when(catalogService.saveSku(any(Sku.class))).thenReturn(sku);
        doNothing().when(extProductService).saveExtProduct(any(Date.class), anyInt(), anyLong());

        // Perform request
        MvcResult mvcResult = mockMvc.perform(post("/productCustom/add")
                        .param("expirationDate", "2024-12-31 23:59:59")
                        .param("goLiveDate", "2024-01-01 00:00:00")
                        .param("priorityLevel", "5")
                        .param("categoryId", "1")
                        .param("skuId", "0")
                        .param("name", "New Product")
                        .param("description", "New Product Description")
                        .param("quantity", "10")
                        .param("cost", "150.00")
                        .contentType("application/json")
                        .content("{\"id\":1001}")
                ).andExpect(status().isOk())
                .andReturn();

        // Verify response
        assertEquals("Product added successfully", mvcResult.getResponse().getContentAsString());

        // Verify interactions
        verify(catalogService).findCategoryById(1L);
        verify(catalogService).createSku();
        verify(catalogService).saveProduct(any(ProductImpl.class));
        verify(catalogService).saveSku(any(Sku.class));
        verify(extProductService).saveExtProduct(any(Date.class), anyInt(), anyLong());
    }

    @Test
    void addProduct_CategoryNotFound() throws Exception {
        // Initialize mock data
        ProductImpl productImpl = new ProductImpl();
        productImpl.setId(1001L);

        // Mock behaviors
        when(catalogService.findCategoryById(1L)).thenReturn(null); // Simulate category not found

        // Perform request
        MvcResult mvcResult = mockMvc.perform(post("/productCustom/add")
                        .param("expirationDate", "2024-12-31 23:59:59")
                        .param("goLiveDate", "2024-10-01 00:00:00")
                        .param("priorityLevel", "5")
                        .param("categoryId", "1") // Category ID that will not be found
                        .param("skuId", "0")
                        .param("name", "New Product")
                        .param("description", "New Product Description")
                        .param("quantity", "10")
                        .param("cost", "150.00")
                        .contentType("application/json")
                        .content("{}")
                ).andExpect(status().isNotFound()) // Expect 404 Not Found
                .andReturn();

        // Verify interactions
        verify(catalogService).findCategoryById(1L);
        verifyNoInteractions(extProductService); // Ensure extProductService is not called
    }


    @Test
    void retrieveProducts_Success() throws Exception {
        // Initialize mock data
        Product product = new ProductImpl();
        product.setId(1001L);

        Sku sku = new SkuImpl();
        sku.setId(1001L);

        Category category = new CategoryImpl();
        category.setId(1001L);

        CustomProduct customProduct = new CustomProduct(new Date(), 3);
        customProduct.setId(1001L);
        customProduct.setDefaultSku(sku);
        customProduct.getDefaultSku().setDefaultProduct(product);
        customProduct.getDefaultSku().setName("helloSku");
        customProduct.setDefaultCategory(category);
        customProduct.getDefaultCategory().getId();
        customProduct.getDefaultSku().setCost(new Money());

        products.add(product);

        // Mock behavior
        when(catalogService.findAllProducts()).thenReturn(products);
        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(customProduct);
        // Perform request and verify response
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/productCustom/getProducts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Ensure the status is OK
                .andReturn();

        // Extract and parse response
        String content = mvcResult.getResponse().getContentAsString();
        assertNotNull(content);
        System.out.println("This is the msg: " + content);

        List<Map<String, Object>> responseBody = parseJson(content);
        assertNotNull(responseBody);
        System.out.println(responseBody.size());
        assertEquals(1, responseBody.size());

        Map<String, Object> productResponse = responseBody.get(0);
        assertEquals(customProduct.getId().toString(), productResponse.get("productId").toString());
        assertEquals(customProduct.getDefaultSku().getName(), productResponse.get("productName"));
    }

    @Test
    void retrieveProducts_Failure_NoProductsFound() throws Exception {
        // Mock behavior
        when(catalogService.findAllProducts()).thenReturn(Collections.emptyList()); // Simulate no products found

        // Perform request and verify response
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/productCustom/getProducts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Expect 404 Not Found
                .andReturn();

        // Verify response
        String content = mvcResult.getResponse().getContentAsString();

        // Verify interactions
        verify(catalogService).findAllProducts();
        verifyNoInteractions(entityManager); // Ensure entityManager is not called
    }


    @Test
    void retrieveProductById_Success() throws Exception {
        // Initialize mock data
        Product product = new ProductImpl();
        product.setId(1001L);

        Sku sku = new SkuImpl();
        sku.setId(1001L);
        sku.setName("Sample SKU");
        sku.setDefaultProduct(product);
        sku.setCost(new Money(150.00)); // Assuming Money has a constructor for amount

        Category category = new CategoryImpl();
        category.setId(1001L);
        category.setName("Sample Category");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(new Date());
        Date goLiveDate = dateFormat.parse(formattedDate);

        CustomProduct customProduct = new CustomProduct(new Date(), 3);
        customProduct.setId(1001L);
        customProduct.setDefaultSku(sku);
        customProduct.setDefaultCategory(category);
        customProduct.setArchived('N');
        customProduct.setMetaTitle("Sample Meta Title");
        customProduct.setGoLiveDate(goLiveDate);
        customProduct.setPriorityLevel(1);

        // Mock behavior
        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(customProduct);
        when(catalogService.findProductById(1001L)).thenReturn(product);

        // Perform request and verify response
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/productCustom/getProducts/{productId}", 1001L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Ensure the status is OK
                .andReturn();

        // Extract and parse response
        String content = mvcResult.getResponse().getContentAsString();
        assertNotNull(content);
        System.out.println("This is the response: " + content);

        // Parse the JSON response into a Map
        Map<String, Object> responseBody = parseJson2(content);
        assertNotNull(responseBody);

        // Verify response content
        assertEquals(customProduct.getId().toString(), responseBody.get("productId").toString());
        assertEquals(customProduct.getDefaultSku().getName(), responseBody.get("productName"));
        assertEquals(customProduct.getArchived().toString(), responseBody.get("archived").toString());
        assertEquals(customProduct.getMetaTitle(), responseBody.get("metaTitle"));
        assertEquals(customProduct.getDefaultSku().getDescription(), responseBody.get("description"));
        assertEquals(customProduct.getDefaultSku().getCost().doubleValue(), responseBody.get("cost"));
        assertEquals(customProduct.getDefaultCategory().getId().toString(), responseBody.get("defaultCategoryId").toString());
        assertEquals(customProduct.getDefaultCategory().getName(), responseBody.get("categoryName"));
        assertEquals(customProduct.getDefaultSku().getActiveStartDate(), responseBody.get("ActiveCreatedDate"));
        assertEquals(customProduct.getDefaultSku().getActiveEndDate(), responseBody.get("ActiveExpirationDate"));

        // Extract and format dates for comparison
        Date expectedGoLiveDate = customProduct.getGoLiveDate();
        String expectedGoLiveDateStr = dateFormat.format(expectedGoLiveDate);

        // Format actual date for comparison
        String actualGoLiveDateStr = dateFormat.format(goLiveDate);

        // Compare dates
        assertEquals(expectedGoLiveDateStr, actualGoLiveDateStr);

        assertEquals(customProduct.getPriorityLevel(), responseBody.get("priorityLevel"));
    }

    @Test
    void retrieveProductById_Failure_ProductNotFound() throws Exception {
        // Mock behavior
        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(null); // Simulate product not found

        // Perform request and verify response
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/productCustom/getProducts/{productId}", 1001L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Expect 404 Not Found
                .andReturn();

        // Verify response
        String content = mvcResult.getResponse().getContentAsString();

        // Verify interactions
        verify(entityManager).find(CustomProduct.class, 1001L);
        verifyNoInteractions(catalogService); // Ensure catalogService is not called
    }


    @Test
    void deleteProduct_Success() throws Exception {
        // Initialize mock data
        Product product = new ProductImpl();
        product.setId(1001L);

        Sku sku = new SkuImpl();
        sku.setId(1001L);
        sku.setDefaultProduct(product);

        CustomProduct customProduct = new CustomProduct(new Date(), 3);
        customProduct.setId(1001L);
        customProduct.setDefaultSku(sku);

        // Mock behaviors
        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(customProduct);
        doNothing().when(catalogService).removeProduct(product);

        // Perform request and verify response
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/productCustom/delete/{productId}", 1001L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Product Archived Successfully"))
                .andReturn();

        // Verify interactions
        verify(entityManager).find(CustomProduct.class, 1001L);
        verify(catalogService).removeProduct(product);
        verifyNoInteractions(exceptionHandlingService); // Ensure exceptionHandlingService is not called
    }

    @Test
    void deleteProduct_ProductNotFound() throws Exception {
        // Mock behaviors
        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(null);

        // Perform request and verify response
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/productCustom/delete/{productId}", 1001L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Verify interactions
        verify(entityManager).find(CustomProduct.class, 1001L);
    }


    @Test
    void updateProduct_Success() throws Exception {
        // Initialize mock data
        ProductImpl productImpl = new ProductImpl();
        productImpl.setMetaTitle("Updated Meta Title");
        productImpl.setMetaDescription("Updated Meta Description");
        productImpl.setUrl("http://updated.url");

        Product product = new ProductImpl();
        product.setId(1001L);

        Sku sku = new SkuImpl();
        sku.setId(1001L);
        sku.setName("Name");
        sku.setDefaultProduct(product);

        CustomProduct customProduct = new CustomProduct(new Date(), 3);
        customProduct.setId(1001L);
        customProduct.setDefaultSku(sku);

        Category category = new CategoryImpl();
        category.setId(1002L);
        product.setDefaultCategory(category);

        // Mock behaviors
        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(customProduct);
        when(catalogService.findProductById(1001L)).thenReturn(product);
        when(catalogService.findCategoryById(1002L)).thenReturn(category);
        when(catalogService.saveProduct(any(Product.class))).thenReturn(product);
        when(entityManager.merge(any(CustomProduct.class))).thenReturn(customProduct);
        doNothing().when(extProductService).removeCategoryProductFromCategoryProductRefTable(anyLong(), anyLong());

        // Prepare request
        String requestBody = "{"
                + "\"metaTitle\": \"Updated Meta Title\","
                + "\"metaDescription\": \"Updated Meta Description\","
                + "\"url\": \"http://updated.url\""
                + "}";

        // Perform request and verify response
        mockMvc.perform(put("/productCustom/update/{productId}", 1001L)
                        .param("expirationDate", "2024-12-31 23:59:59")
                        .param("goLiveDate", "2024-10-01 00:00:00")
                        .param("priorityLevel", "5")
                        .param("categoryId", "1002")
                        .param("name", "Updated Name")
                        .param("description", "Updated Description")
                        .param("quantity", "20")
                        .param("cost", "200.00")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Product Updated Successfully"))
                .andReturn();

        // Verify interactions
        verify(entityManager).find(CustomProduct.class, 1001L);
        verify(catalogService).findProductById(1001L);
        verify(catalogService).findCategoryById(1002L);
        verify(catalogService).saveProduct(any(Product.class));
        verify(entityManager).merge(any(CustomProduct.class));
        verify(extProductService).removeCategoryProductFromCategoryProductRefTable(category.getId(), product.getId());
    }



    @Test
    void updateProduct_ProductNotFound() throws Exception {
        // Mock behaviors
        when(entityManager.find(CustomProduct.class, 1001L)).thenReturn(null);

        // Prepare request
        String requestBody = "{}";

        // Perform request and verify response
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/productCustom/update/{productId}", 1001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andReturn();

        // Verify interactions
        verify(entityManager).find(CustomProduct.class, 1001L);
        verifyNoInteractions(catalogService); // Ensure catalogService is not called
        verifyNoInteractions(extProductService); // Ensure extProductService is not called
    }
}