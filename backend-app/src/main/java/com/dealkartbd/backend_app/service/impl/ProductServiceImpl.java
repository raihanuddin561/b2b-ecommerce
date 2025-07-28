package com.dealkartbd.backend_app.service.impl;

import com.dealkartbd.backend_app.dto.products.ProductRequest;
import com.dealkartbd.backend_app.dto.products.ProductResponse;
import com.dealkartbd.backend_app.entity.Company;
import com.dealkartbd.backend_app.entity.Product;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.exception.ResourceNotFoundException;
import com.dealkartbd.backend_app.repository.CompanyRepository;
import com.dealkartbd.backend_app.repository.ProductRepository;
import com.dealkartbd.backend_app.repository.UserRepository;
import com.dealkartbd.backend_app.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    public ProductResponse addProduct(ProductRequest request, Principal principal) {
        User user = getUserFromPrincipal(principal);
        Company company = user.getCompany();

        if (company == null) {
            throw new IllegalStateException("User is not associated with any company");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .minOrderQuantity(request.getMinOrderQuantity())
                .imageUrl(request.getImageUrl())
                .company(company)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product {} added successfully by company: {}", savedProduct.getName(), company.getName());

        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::mapToProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request, Principal principal) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        User user = getUserFromPrincipal(principal);
        validateProductOwnership(product, user);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setMinOrderQuantity(request.getMinOrderQuantity());
        product.setImageUrl(request.getImageUrl());

        Product updatedProduct = productRepository.save(product);
        log.info("Product {} updated successfully by user: {}", updatedProduct.getName(), user.getEmail());

        return mapToProductResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id, Principal principal) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        User user = getUserFromPrincipal(principal);
        validateProductOwnership(product, user);

        productRepository.delete(product);
        log.info("Product {} deleted successfully by user: {}", product.getName(), user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }

        List<Product> products = productRepository.searchProducts(query.trim());
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }

        List<Product> products = productRepository.findByCompanyId(companyId);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCompany(Long companyId, Pageable pageable) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }

        Page<Product> products = productRepository.findByCompanyId(companyId, pageable);
        return products.map(this::mapToProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getMyProducts(Principal principal) {
        User user = getUserFromPrincipal(principal);
        Company company = user.getCompany();

        if (company == null) {
            throw new IllegalStateException("User is not associated with any company");
        }

        List<Product> products = productRepository.findByCompanyId(company.getId());
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            throw new IllegalArgumentException("Invalid price range");
        }

        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateProductOwnership(Product product, User user) {
        if (user.getCompany() == null) {
            throw new IllegalStateException("User is not associated with any company");
        }

        if (!product.getCompany().getId().equals(user.getCompany().getId())) {
            throw new AccessDeniedException("You don't have permission to modify this product");
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .minOrderQuantity(product.getMinOrderQuantity())
                .imageUrl(product.getImageUrl())
                .companyId(product.getCompany().getId())
                .companyName(product.getCompany().getName())
                .build();
    }
}
