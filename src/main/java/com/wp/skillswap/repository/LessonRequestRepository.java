package com.wp.skillswap.repository;

import com.wp.skillswap.model.LessonRequest;
import com.wp.skillswap.model.Offer;
import com.wp.skillswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRequestRepository extends JpaRepository<LessonRequest, Long> {

    void deleteByOffer(Offer offer);

    void deleteByRequester(User requester);

    List<LessonRequest> findByRequester(User requester);

    List<LessonRequest> findByOfferOwner(User owner);
}