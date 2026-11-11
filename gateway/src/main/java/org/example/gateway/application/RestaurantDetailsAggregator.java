package org.example.gateway.application;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RestaurantDetailsAggregator {

    private static final ParameterizedTypeReference<List<ReviewView>> REVIEWS_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    public RestaurantDetailsAggregator(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<RestaurantDetailsResponse> aggregateRestaurantDetails(UUID restaurantId) {
        Mono<ServiceResult<RestaurantView>> restaurantMono = fetchRestaurant(restaurantId)
                .map(data -> ServiceResult.available("catalog", data))
                .onErrorResume(error -> {
                    if (isNotFound(error)) {
                        return Mono.error(error);
                    }
                    return Mono.just(ServiceResult.unavailable("catalog", "Catalog service is unavailable"));
                });

        Mono<ServiceResult<List<ReviewView>>> reviewsMono = fetchReviews(restaurantId)
                .map(data -> ServiceResult.available("reviews", data))
                .onErrorResume(error -> Mono.just(
                        ServiceResult.unavailable("reviews", "Reviews service is unavailable")
                ));

        return Mono.zip(restaurantMono, reviewsMono)
                .map(tuple -> buildResponse(restaurantId, tuple.getT1(), tuple.getT2()));
    }

    private Mono<RestaurantView> fetchRestaurant(UUID restaurantId) {
        return webClient.get()
                .uri("http://localhost:8081/api/v1/catalog/restaurants/{id}", restaurantId)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Restaurant " + restaurantId + " not found"
                        )))
                .bodyToMono(RestaurantView.class);
    }

    private Mono<List<ReviewView>> fetchReviews(UUID restaurantId) {
        return webClient.get()
                .uri("http://localhost:8082/api/v1/reviews/restaurant/{id}", restaurantId)
                .retrieve()
                .bodyToMono(REVIEWS_TYPE);
    }

    private RestaurantDetailsResponse buildResponse(
            UUID restaurantId,
            ServiceResult<RestaurantView> restaurant,
            ServiceResult<List<ReviewView>> reviews
    ) {
        List<String> unavailableServices = new ArrayList<>();

        Optional<RestaurantView> restaurantData = restaurant.data();
        if (restaurantData.isEmpty()) {
            unavailableServices.add(restaurant.serviceName());
        }

        Optional<List<ReviewView>> reviewsData = reviews.data();
        if (reviewsData.isEmpty()) {
            unavailableServices.add(reviews.serviceName());
        }

        return new RestaurantDetailsResponse(
                restaurantId,
                restaurantData.orElse(null),
                reviewsData.orElse(Collections.emptyList()),
                !unavailableServices.isEmpty(),
                unavailableServices
        );
    }

    private boolean isNotFound(Throwable error) {
        return error instanceof ResponseStatusException responseStatusException
                && responseStatusException.getStatusCode().value() == 404;
    }
}
