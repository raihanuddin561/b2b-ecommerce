package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.products.ProductRequest;
import com.dealkartbd.backend_app.dto.products.ProductResponse;

import java.security.Principal;
import java.util.List;

public interface ProductService {
    public void addProduct(ProductRequest request, Principal principal);
    public List<ProductResponse> getAllProducts();
    public List<ProductResponse> search(String query);
}
