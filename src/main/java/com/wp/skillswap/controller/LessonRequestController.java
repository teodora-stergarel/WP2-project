package com.wp.skillswap.controller;

import com.wp.skillswap.model.LessonRequest;
import com.wp.skillswap.model.RequestStatus;
import com.wp.skillswap.model.User;
import com.wp.skillswap.repository.LessonRequestRepository;
import com.wp.skillswap.repository.UserRepository;
import com.wp.skillswap.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class LessonRequestController {

    private final LessonRequestRepository lessonRequestRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public LessonRequestController(LessonRequestRepository lessonRequestRepository,
                                   UserRepository userRepository,
                                   UserService userService) {
        this.lessonRequestRepository = lessonRequestRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/lesson-requests")
    public String showLessonRequests(Model model, Principal principal) {
        User currentUser = userService.getAuthenticatedUser(principal);

        model.addAttribute("sentRequests", lessonRequestRepository.findByRequester(currentUser));
        model.addAttribute("receivedRequests", lessonRequestRepository.findByOfferOwner(currentUser));

        return "lesson-requests";
    }

    @PostMapping("/lesson-requests/accept/{id}")
    public String acceptRequest(@PathVariable Long id, Principal principal) {
        User currentUser = userService.getAuthenticatedUser(principal);
        LessonRequest request = lessonRequestRepository.findById(id).orElse(null);

        if (request == null) {
            return "redirect:/lesson-requests?error=notfound";
        }

        if (!request.getOffer().getOwner().getId().equals(currentUser.getId())) {
            return "redirect:/lesson-requests?error=forbidden";
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            return "redirect:/lesson-requests?error=processed";
        }

        User requester = request.getRequester();
        User owner = request.getOffer().getOwner();
        Integer price = request.getOffer().getPriceCredits();

        if (requester.getCreditsBalance() < price) {
            return "redirect:/lesson-requests?error=notenoughcredits";
        }

        requester.setCreditsBalance(requester.getCreditsBalance() - price);
        owner.setCreditsBalance(owner.getCreditsBalance() + price);

        userRepository.save(requester);
        userRepository.save(owner);

        request.setStatus(RequestStatus.ACCEPTED);
        lessonRequestRepository.save(request);

        return "redirect:/lesson-requests?success=accepted";
    }

    @PostMapping("/lesson-requests/reject/{id}")
    public String rejectRequest(@PathVariable Long id, Principal principal) {
        User currentUser = userService.getAuthenticatedUser(principal);
        LessonRequest request = lessonRequestRepository.findById(id).orElse(null);

        if (request == null) {
            return "redirect:/lesson-requests?error=notfound";
        }

        if (!request.getOffer().getOwner().getId().equals(currentUser.getId())) {
            return "redirect:/lesson-requests?error=forbidden";
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            return "redirect:/lesson-requests?error=processed";
        }

        request.setStatus(RequestStatus.REJECTED);
        lessonRequestRepository.save(request);

        return "redirect:/lesson-requests?success=rejected";
    }
}