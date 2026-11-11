package org.example.gateway.api;

import org.example.gateway.application.RestaurantDetailsAggregator;
import org.example.gateway.application.RestaurantDetailsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/composition")
public class CompositionController {

    private final RestaurantDetailsAggregator aggregator;

    public CompositionController(RestaurantDetailsAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @GetMapping("/restaurants/{restaurantId}/details")
    public Mono<RestaurantDetailsResponse> getRestaurantDetails(@PathVariable("restaurantId") UUID restaurantId) {
        return aggregator.aggregateRestaurantDetails(restaurantId);
    }
}
