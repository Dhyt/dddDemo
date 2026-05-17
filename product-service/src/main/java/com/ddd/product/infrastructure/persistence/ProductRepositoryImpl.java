package com.ddd.product.infrastructure.persistence;

import com.ddd.product.domain.model.Product;
import com.ddd.product.domain.model.ProductId;
import com.ddd.product.domain.model.ProductStatus;
import com.ddd.product.domain.model.Stock;
import com.ddd.product.domain.repository.ProductRepository;
import com.ddd.product.infrastructure.persistence.jpa.ProductJpaEntity;
import com.ddd.product.infrastructure.persistence.jpa.ProductJpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryImpl(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public void save(Product product) {
        ProductJpaEntity entity = toJpa(product);
        ProductJpaEntity saved = jpaRepository.save(entity);
        if (product.getId() == null && saved.getId() != null) {
            product.setId(saved.getId());
        }
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private Product toDomain(ProductJpaEntity entity) {
        try {
            Product product = new Product(
                    entity.getName(),
                    entity.getPrice(),
                    entity.getAvailableQuantity()
            );

            product.setId(entity.getId());

            Field descriptionField = Product.class.getDeclaredField("description");
            descriptionField.setAccessible(true);
            descriptionField.set(product, entity.getDescription());

            Field stockField = Product.class.getDeclaredField("stock");
            stockField.setAccessible(true);
            stockField.set(product, new Stock(entity.getAvailableQuantity(), entity.getReservedQuantity()));

            Field statusField = Product.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(product, ProductStatus.valueOf(entity.getStatus()));

            Field createdAtField = Product.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(product, entity.getCreatedAt());

            return product;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct Product from JPA entity", e);
        }
    }

    private ProductJpaEntity toJpa(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        if (product.getId() != null) {
            entity.setId(product.getId().value());
        }
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPrice(product.getPrice());
        entity.setAvailableQuantity(product.getStock().availableQuantity());
        entity.setReservedQuantity(product.getStock().reservedQuantity());
        entity.setStatus(product.getStatus().name());
        entity.setCreatedAt(product.getCreatedAt());
        return entity;
    }
}
