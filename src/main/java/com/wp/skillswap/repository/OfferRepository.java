package com.wp.skillswap.repository;

import com.wp.skillswap.model.Offer;
import com.wp.skillswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByOwner(User owner);

    List<Offer> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String titleKeyword,
            String descriptionKeyword
    );

    List<Offer> findByTitleContainingIgnoreCase(String title);

    List<Offer> findByPriceCredits(Integer priceCredits);

    List<Offer> findByTitleContainingIgnoreCaseAndPriceCredits(String title, Integer priceCredits);
}