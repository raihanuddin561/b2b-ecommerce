package com.dealkartbd.backend_app.dto.products;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int minOrderQuantity;
    private String imageUrl;
    private String companyName;
}
