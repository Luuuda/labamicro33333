package org.example.reviews.api;

import org.example.reviews.application.CreateReviewDto;
import org.example.reviews.application.ReviewDto;
import org.example.reviews.application.ReviewService;
import org.example.reviews.domain.Review;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody CreateReviewDto dto) {
        return reviewService.createReview(dto);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewDto>> getReviewsForRestaurant(@PathVariable("restaurantId") UUID restaurantId) {
        return ResponseEntity.ok(reviewService.getReviewsForRestaurant(restaurantId));
    }
}