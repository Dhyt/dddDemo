package com.ddd.product.domain.repository;

import com.ddd.product.domain.model.Product;
import com.ddd.product.domain.model.ProductId;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(ProductId id);
    void save(Product product);
    List<Product> findAll();
}
