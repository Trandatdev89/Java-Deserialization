package com.project01.javadeserialization.controller;


import com.project01.javadeserialization.entity.Product;
import com.project01.javadeserialization.entity.User;
import com.project01.javadeserialization.repository.ProductRepository;
import com.project01.javadeserialization.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listProducts(Model model, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName());
        List<Product> products = productRepository.findByOwnerUsername(user.getUsername());
        model.addAttribute("products", products);
        model.addAttribute("username", user.getUsername());
        return "products";
    }

    @PostMapping("/add")
    public String addProduct(@RequestParam String title, @RequestParam String thumbnail, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName());
        Product p = new Product();
        p.setTitle(title);
        p.setThumbnail(thumbnail);
        p.setOwner(user);
        productRepository.save(p);
        return "redirect:/products";
    }
}