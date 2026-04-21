package com.wp.skillswap.repository;

import com.wp.skillswap.model.LessonRequest;
import com.wp.skillswap.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRequestRepository extends JpaRepository<LessonRequest, Long> {

    void deleteByOffer(Offer offer);
}