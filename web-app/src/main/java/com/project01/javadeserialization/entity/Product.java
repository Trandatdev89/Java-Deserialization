package com.project01.javadeserialization.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product")
public class Product {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String title;

    private String thumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}