//package com.community.api.endpoint.avisoft.controller.Category;
//
//import com.community.api.dto.CategoryDto;
//import com.community.api.dto.CustomCategoryWrapper;
//import com.community.api.entity.CustomProduct;
//import com.community.api.services.CategoryService;
//import com.community.api.services.exception.ExceptionHandlingService;
//import org.broadleafcommerce.core.catalog.domain.Category;
//import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
//import org.broadleafcommerce.core.catalog.domain.SkuImpl;
//import org.broadleafcommerce.core.catalog.service.CatalogService;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.mock.web.MockHttpServletRequest;
//
//import javax.persistence.EntityManager;
//import java.math.BigInteger;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class CategoryControllerTest {
//
//    @Mock
//    private CatalogService catalogService;
//
//    @Mock
//    private ExceptionHandlingService exceptionHandlingService;
//
//    @Mock
//    CategoryService categoryService;
//
//    @Mock
//    EntityManager entityManager;
//
//    @InjectMocks
//    private CategoryController categoryController;
//
//    private MockHttpServletRequest request;
//    private CategoryImpl validCategoryImpl;
//
//    @Test
//    public void testAddCategory_Successful() {
//        // Given
//        Category mockCategory = new CategoryImpl();
//        when(catalogService.saveCategory(validCategoryImpl)).thenReturn(mockCategory);
//
//        CustomCategoryWrapper wrapper = new CustomCategoryWrapper();
//        doNothing().when(wrapper).wrapDetails(mockCategory, request);
//
//        // When
//        ResponseEntity<?> response = categoryController.addCategory(request, validCategoryImpl);
//
//        // Then
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertTrue(response.getBody() instanceof CustomCategoryWrapper);
//    }
//
//    @Test
//    public void testAddCategory_CategoryTitleEmpty() {
//        // Given
//        validCategoryImpl.setName("   ");
//
//        // When
//        ResponseEntity<?> response = categoryController.addCategory(request, validCategoryImpl);
//
//        // Then
//        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//        assertEquals("CategoryTitle cannot be empty or null", response.getBody());
//    }
//
//    @Test
//    public void testGetCategories_Successful() {
//        // Given
//        List<Category> mockCategories = Arrays.asList(
//                new Category(1L, "Category 1", new Date(), null, 'N'),
//                new Category(2L, "Category 2", new Date(), new Date(System.currentTimeMillis() + 3600 * 1000), 'N') // 1 hour later
//        );
//        when(catalogService.findAllCategories()).thenReturn(mockCategories);
//
//        // When
//        ResponseEntity<?> response = categoryController.getCategories(request, 20);
//
//        // Then
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        List<CustomCategoryWrapper> responseBody = (List<CustomCategoryWrapper>) response.getBody();
//        assertNotNull(responseBody);
//        assertEquals(2, responseBody.size()); // Ensure two active categories are returned
//    }
//
//    @Test
//    public void testGetCategories_CatalogServiceNull() {
//        // Given
//        when(catalogService).thenReturn(null);
//
//        // When
//        ResponseEntity<?> response = categoryController.getCategories(request, 20);
//
//        // Then
//        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//        assertEquals("catalogService is null", response.getBody());
//    }
//
//    @Test
//    public void testGetProductsByCategoryId_Successful() {
//        // Given
//        Long categoryId = 1L;
//        Category category = new CategoryImpl();
//        category.setId(categoryId);
//        category.setName("Test Category");
//
//        CustomProduct customProduct = new CustomProduct();
//        customProduct.setId(1L);
//        customProduct.setDefaultSku(new SkuImpl());  // Assume Sku is a related entity
//        customProduct.getDefaultSku().setActiveEndDate(new Date(System.currentTimeMillis() + 10000)); // future date
//
//        List<BigInteger> productIdList = Arrays.asList(BigInteger.valueOf(1));
//        when(catalogService.findCategoryById(categoryId)).thenReturn(category);
//        when(categoryService.getAllProductsByCategoryId(categoryId)).thenReturn(productIdList);
//        when(entityManager.find(CustomProduct.class, 1L)).thenReturn(customProduct);
//
//        // When
//        ResponseEntity<?> response = categoryController.getProductsByCategoryId(request, categoryId.toString());
//
//        // Then
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        CategoryDto categoryDto = (CategoryDto) response.getBody();
//        assertNotNull(categoryDto);
//        assertEquals(categoryId, categoryDto.getCategoryId());
//        assertEquals(1, categoryDto.getProducts().size());
//        verify(catalogService, times(1)).findCategoryById(categoryId);
//        verify(categoryService, times(1)).getAllProductsByCategoryId(categoryId);
//        verify(entityManager, times(1)).find(CustomProduct.class, 1L);
//    }
//
//    @Test
//    public void testGetProductsByCategoryId_CategoryNotFound() {
//        // Given
//        Long categoryId = 2L;
//        when(catalogService.findCategoryById(categoryId)).thenReturn(null);
//
//        // When
//        ResponseEntity<?> response = categoryController.getProductsByCategoryId(request, categoryId.toString());
//
//        // Then
//        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//        assertEquals("Category not Found", response.getBody());
//        verify(catalogService, times(1)).findCategoryById(categoryId);
//        verify(categoryService, never()).getAllProductsByCategoryId(anyLong());
//        verify(entityManager, never()).find(any(), any());
//    }
//
//    @Test
//    public void testRemoveCategoryById_Successful() {
//        // Given
//        Long categoryId = 1L;
//        Category mockCategory = new CategoryImpl();
//        when(catalogService.findCategoryById(categoryId)).thenReturn(mockCategory);
//        doNothing().when(catalogService).removeCategory(mockCategory);
//
//        // When
//        ResponseEntity<?> response = categoryController.removeCategoryById(request, categoryId.toString(), 20, 1, 20, 1);
//
//        // Then
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals("Category Deleted Successfully", response.getBody());
//        verify(catalogService, times(1)).removeCategory(mockCategory);
//    }
//
//    @Test
//    public void testRemoveCategoryById_CategoryNotFound() {
//        // Given
//        Long categoryId = 2L;
//        when(catalogService.findCategoryById(categoryId)).thenReturn(null);
//
//        // When
//        ResponseEntity<?> response = categoryController.removeCategoryById(request, categoryId.toString(), 20, 1, 20, 1);
//
//        // Then
//        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//        assertEquals("category not found", response.getBody());
//        verify(catalogService, never()).removeCategory(any(Category.class));
//    }
//
//    @Test
//    public void testUpdateCategoryById_Successful() {
//        // Given
//        Long categoryId = 1L;
//        CategoryImpl categoryImpl = new CategoryImpl();
//        categoryImpl.setName("Updated Name");
//        categoryImpl.setDescription("Updated Description");
//        categoryImpl.setActiveStartDate(new Date());
//        categoryImpl.setActiveEndDate(null);
//        categoryImpl.setDisplayTemplate("Updated Template");
//
//        Category existingCategory = new CategoryImpl();
//        existingCategory.setName("Old Name");
//
//        when(catalogService.findCategoryById(categoryId)).thenReturn(existingCategory);
//        when(catalogService.saveCategory(existingCategory)).thenReturn(existingCategory);
//
//        // When
//        ResponseEntity<?> response = categoryController.updateCategoryById(request, categoryImpl, categoryId.toString());
//
//        // Then
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        CustomCategoryWrapper wrapper = (CustomCategoryWrapper) response.getBody();
//        assertNotNull(wrapper);
//        assertEquals("Updated Name", wrapper.getName());
//        verify(catalogService, times(1)).saveCategory(existingCategory);
//    }
//
//    @Test
//    public void testUpdateCategoryById_CategoryNotFound() {
//        // Given
//        Long categoryId = 2L;
//        CategoryImpl categoryImpl = new CategoryImpl();
//        when(catalogService.findCategoryById(categoryId)).thenReturn(null);
//
//        // When
//        ResponseEntity<?> response = categoryController.updateCategoryById(request, categoryImpl, categoryId.toString());
//
//        // Then
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        assertEquals("Category not found.", response.getBody());
//        verify(catalogService, never()).saveCategory(any(Category.class));
//    }
//
//
//}