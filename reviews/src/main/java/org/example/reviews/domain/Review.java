package org.example.reviews.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    private UUID id;
    private UUID restaurantId;
    private String author;
    private String text;
    private int rating;
    private String status; //сага

    // Порожній конструктор обов'язково потрібен для бази даних (JPA/Hibernate)
    protected Review() {}

    // Конструктор з усіма параметрами
    public Review(UUID id, UUID restaurantId, String author, String text, int rating, String status) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.author = author;
        this.text = text;
        this.rating = rating;
        this.status = status;
    }

    // Геттери, які використовує ReviewService
    public UUID getId() { return id; }
    public UUID getRestaurantId() { return restaurantId; }
    public String getAuthor() { return author; }
    public String getText() { return text; }
    public int getRating() { return rating; }

    // Геттер та Сеттер для статусу
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}