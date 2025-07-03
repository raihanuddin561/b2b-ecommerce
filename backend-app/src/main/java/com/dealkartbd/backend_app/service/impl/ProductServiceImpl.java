package com.dealkartbd.backend_app.service.impl;

import com.dealkartbd.backend_app.dto.products.ProductRequest;
import com.dealkartbd.backend_app.dto.products.ProductResponse;
import com.dealkartbd.backend_app.entity.Company;
import com.dealkartbd.backend_app.entity.Product;
import com.dealkartbd.backend_app.repository.CompanyRepository;
import com.dealkartbd.backend_app.repository.ProductRepository;
import com.dealkartbd.backend_app.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    @Override
    public void addProduct(ProductRequest request, Principal principal) {
        Company company = companyRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Company not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .minOrderQuantity(request.getMinOrderQuantity())
                .imageUrl(request.getImageUrl())
                .company(company)
                .build();

        productRepository.save(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> search(String query) {
        return productRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .minOrderQuantity(product.getMinOrderQuantity())
                .imageUrl(product.getImageUrl())
                .companyName(product.getCompany().getName())
                .build();
    }
}
