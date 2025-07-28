package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.products.ProductRequest;
import com.dealkartbd.backend_app.dto.products.ProductResponse;
import com.dealkartbd.backend_app.entity.Company;
import com.dealkartbd.backend_app.entity.Product;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.exception.ResourceNotFoundException;
import com.dealkartbd.backend_app.repository.CompanyRepository;
import com.dealkartbd.backend_app.repository.ProductRepository;
import com.dealkartbd.backend_app.repository.UserRepository;
import com.dealkartbd.backend_app.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private Principal principal;

    @InjectMocks
    private ProductServiceImpl productService;

    private User testUser;
    private Company testCompany;
    private Product testProduct;
    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder().build();
        testCompany.setId(1L);
        testCompany.setName("Test Company");

        testUser = User.builder().build();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setFullName("John Doe");
        testUser.setCompany(testCompany);

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .minOrderQuantity(1)
                .imageUrl("http://test.com/image.jpg")
                .company(testCompany)
                .build();

        testProductRequest = ProductRequest.builder()
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .minOrderQuantity(1)
                .imageUrl("http://test.com/image.jpg")
                .build();
    }

    @Test
    void addProduct_ValidRequest_ShouldCreateProductSuccessfully() {
        // Arrange
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse response = productService.addProduct(testProductRequest, principal);

        // Assert
        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        assertEquals("Test Description", response.getDescription());
        assertEquals(99.99, response.getPrice());
        assertEquals(1, response.getMinOrderQuantity());
        assertEquals("Test Company", response.getCompanyName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void addProduct_UserWithoutCompany_ShouldThrowIllegalStateException() {
        // Arrange
        User userWithoutCompany = User.builder().build();
        userWithoutCompany.setId(1L);
        userWithoutCompany.setEmail("user@test.com");
        userWithoutCompany.setCompany(null);

        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userWithoutCompany));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            productService.addProduct(testProductRequest, principal);
        });
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<ProductResponse> responses = productService.getAllProducts();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Product", responses.get(0).getName());
    }

    @Test
    void getAllProductsPaginated_ShouldReturnPagedProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // Act
        Page<ProductResponse> responses = productService.getAllProducts(pageable);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.getContent().size());
        assertEquals("Test Product", responses.getContent().get(0).getName());
    }

    @Test
    void getProductById_ValidId_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        ProductResponse response = productService.getProductById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Product", response.getName());
    }

    @Test
    void getProductById_InvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(999L);
        });
    }

    @Test
    void updateProduct_ValidRequest_ShouldUpdateProductSuccessfully() {
        // Arrange
        ProductRequest updateRequest = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(149.99)
                .minOrderQuantity(2)
                .imageUrl("http://test.com/updated-image.jpg")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse response = productService.updateProduct(1L, updateRequest, principal);

        // Assert
        assertNotNull(response);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_UnauthorizedUser_ShouldThrowAccessDeniedException() {
        // Arrange
        Company otherCompany = Company.builder().build();
        otherCompany.setId(2L);
        otherCompany.setName("Other Company");

        User unauthorizedUser = User.builder().build();
        unauthorizedUser.setId(2L);
        unauthorizedUser.setEmail("other@test.com");
        unauthorizedUser.setCompany(otherCompany);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(principal.getName()).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(unauthorizedUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            productService.updateProduct(1L, testProductRequest, principal);
        });
    }

    @Test
    void deleteProduct_ValidRequest_ShouldDeleteProductSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));

        // Act
        productService.deleteProduct(1L, principal);

        // Assert
        verify(productRepository).delete(testProduct);
    }

    @Test
    void search_ValidQuery_ShouldReturnMatchingProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.searchProducts("Test")).thenReturn(products);

        // Act
        List<ProductResponse> responses = productService.search("Test");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Product", responses.get(0).getName());
    }

    @Test
    void search_EmptyQuery_ShouldReturnAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<ProductResponse> responses = productService.search("");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(productRepository).findAll();
        verify(productRepository, never()).searchProducts(anyString());
    }

    @Test
    void getProductsByCompany_ValidCompanyId_ShouldReturnCompanyProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(companyRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findByCompanyId(1L)).thenReturn(products);

        // Act
        List<ProductResponse> responses = productService.getProductsByCompany(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Product", responses.get(0).getName());
    }

    @Test
    void getProductsByCompany_InvalidCompanyId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(companyRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductsByCompany(999L);
        });
    }

    @Test
    void getMyProducts_ValidUser_ShouldReturnUserCompanyProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findByCompanyId(1L)).thenReturn(products);

        // Act
        List<ProductResponse> responses = productService.getMyProducts(principal);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Product", responses.get(0).getName());
    }

    @Test
    void getProductsByPriceRange_ValidRange_ShouldReturnProductsInRange() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByPriceBetween(50.0, 150.0)).thenReturn(products);

        // Act
        List<ProductResponse> responses = productService.getProductsByPriceRange(50.0, 150.0);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Product", responses.get(0).getName());
    }

    @Test
    void getProductsByPriceRange_InvalidRange_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductsByPriceRange(150.0, 50.0); // Max < Min
        });

        assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductsByPriceRange(-10.0, 50.0); // Negative price
        });
    }
}
