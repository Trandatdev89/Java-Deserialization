package com.project01.javadeserialization.repository;

import com.project01.javadeserialization.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("SELECT p FROM Product p WHERE p.owner.username = :username")
    List<Product> findByOwnerUsername(@Param("username") String username);
}