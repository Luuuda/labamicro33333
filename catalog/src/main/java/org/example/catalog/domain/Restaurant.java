package org.example.catalog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    private UUID id;
    private String name;
    private String description;
    private String address;

    protected Restaurant() {}

    public Restaurant(UUID id, String name, String description, String address) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
}