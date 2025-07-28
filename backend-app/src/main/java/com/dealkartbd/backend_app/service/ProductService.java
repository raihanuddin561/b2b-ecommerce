package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.products.ProductRequest;
import com.dealkartbd.backend_app.dto.products.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

public interface ProductService {
    ProductResponse addProduct(ProductRequest request, Principal principal);
    List<ProductResponse> getAllProducts();
    Page<ProductResponse> getAllProducts(Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(Long id, ProductRequest request, Principal principal);
    void deleteProduct(Long id, Principal principal);
    List<ProductResponse> search(String query);
    List<ProductResponse> getProductsByCompany(Long companyId);
    Page<ProductResponse> getProductsByCompany(Long companyId, Pageable pageable);
    List<ProductResponse> getMyProducts(Principal principal);
    List<ProductResponse> getProductsByPriceRange(Double minPrice, Double maxPrice);
}
