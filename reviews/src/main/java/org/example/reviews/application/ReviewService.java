package org.example.reviews.application;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.example.reviews.domain.OutboxRecord;
import org.example.reviews.domain.OutboxRepository;
import org.example.reviews.domain.Review;
import org.example.reviews.domain.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OutboxRepository outboxRepository;
    private final RestTemplate restTemplate;

    public ReviewService(ReviewRepository reviewRepository, OutboxRepository outboxRepository, RestTemplate restTemplate) {
        this.reviewRepository = reviewRepository;
        this.outboxRepository = outboxRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
//    @CircuitBreaker(name = "catalogService", fallbackMethod = "catalogFallback")
    @Retry(name = "catalogService")
    public Review createReview(CreateReviewDto dto) {
        String catalogUrl = "http://localhost:8081/api/v1/catalog/restaurants/" + dto.restaurantId();

//        restTemplate.getForObject(catalogUrl, Object.class);

        Review review = new Review(
                UUID.randomUUID(),
                dto.restaurantId(),
                dto.authorName(),
                dto.comment(),
                dto.rating(),
                "PENDING"
        );
        reviewRepository.save(review);


        String payload = String.format("{\"reviewId\":\"%s\", \"restaurantId\":\"%s\", \"rating\":%d}",
                review.getId(), review.getRestaurantId(), review.getRating());
        outboxRepository.save(new OutboxRecord("REVIEW_CREATED", payload));

        return review;
    }

    public Review catalogFallback(CreateReviewDto dto, Throwable t) {
        System.out.println("Fallback спрацював! Причина: " + t.getMessage());
        throw new RuntimeException("Каталог не відповідає або ресторан не знайдено.");
    }

    public List<ReviewDto> getReviewsForRestaurant(UUID restaurantId) {
        return reviewRepository.findAllByRestaurantId(restaurantId).stream()
                .map(r -> new ReviewDto(
                        r.getId(),
                        r.getRestaurantId(),
                        r.getAuthor(),
                        r.getRating(),
                        r.getText(),
                        java.time.LocalDateTime.now()
                ))
                .toList();
    }
}