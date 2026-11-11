package org.example.catalog.api;

import org.example.catalog.application.RestaurantDto;
import org.example.catalog.application.RestaurantService;
import org.example.catalog.application.CreateRestaurantDto;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping
    public RestaurantDto createRestaurant(@Valid @RequestBody CreateRestaurantDto dto) {
        return restaurantService.createRestaurant(dto);
    }

    @GetMapping
    public List<RestaurantDto> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    @GetMapping("/{id}")
    public RestaurantDto getRestaurant(@PathVariable("id") UUID id) { // Додали ("id")
        return restaurantService.getRestaurantById(id);
    }}