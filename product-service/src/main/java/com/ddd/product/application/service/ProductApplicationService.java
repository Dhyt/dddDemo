package com.ddd.product.application.service;

import com.ddd.product.application.dto.ProductResponse;
import com.ddd.product.domain.model.Product;
import com.ddd.product.domain.model.ProductId;
import com.ddd.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductApplicationService {

    private final ProductRepository productRepository;

    public ProductApplicationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(new ProductId(id))
                .orElseThrow(() -> new IllegalArgumentException("商品不存在: " + id));
        return toResponse(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId().value(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock().availableQuantity(),
                product.getStock().reservedQuantity(),
                product.getStatus().name(),
                product.getCreatedAt()
        );
    }
}
