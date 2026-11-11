package org.example.reviews.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    // Магія Spring: він сам напише SQL-запит для пошуку відгуків по ресторану!
    List<Review> findAllByRestaurantId(UUID restaurantId);
}