package org.example.catalog.application;

import org.example.catalog.domain.Restaurant;
import org.example.catalog.domain.RestaurantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class RestaurantService {

    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    public RestaurantDto createRestaurant(CreateRestaurantDto dto) {
        Restaurant restaurant = new Restaurant(UUID.randomUUID(), dto.name(), dto.description(), dto.address());
        return mapToDto(repository.save(restaurant));
    }

    public List<RestaurantDto> getAllRestaurants() {
        return repository.findAll().stream().map(this::mapToDto).toList();
    }

    // БЕЗПЕЧНИЙ МЕТОД: Якщо не знайдено - поверне 404, а не 500
    public RestaurantDto getRestaurantById(UUID id) {
        return repository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
    }

    private RestaurantDto mapToDto(Restaurant restaurant) {
        return new RestaurantDto(restaurant.getId(), restaurant.getName(), restaurant.getDescription(), restaurant.getAddress());
    }
}