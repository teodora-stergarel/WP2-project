package com.wp.skillswap.repository;

import com.wp.skillswap.model.LessonRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRequestRepository extends JpaRepository<LessonRequest, Long> {
}
