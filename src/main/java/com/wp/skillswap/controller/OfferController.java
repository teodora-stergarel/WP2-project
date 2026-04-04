package com.wp.skillswap.controller;

import com.wp.skillswap.model.LessonRequest;
import com.wp.skillswap.model.Offer;
import com.wp.skillswap.model.User;
import com.wp.skillswap.repository.LessonRequestRepository;
import com.wp.skillswap.repository.OfferRepository;
import com.wp.skillswap.repository.UserRepository;
import com.wp.skillswap.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class OfferController {

    private final OfferRepository offerRepository;
    private final LessonRequestRepository lessonRequestRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public OfferController(OfferRepository offerRepository,
                           LessonRequestRepository lessonRequestRepository,
                           UserRepository userRepository,
                           UserService userService) {
        this.offerRepository = offerRepository;
        this.lessonRequestRepository = lessonRequestRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/offers")
    public String showOffers(Model model) {
        model.addAttribute("offers", offerRepository.findAll());
        return "offers";
    }

    @GetMapping("/offers/create")
    public String showCreateOfferPage(Model model) {
        model.addAttribute("offer", new Offer());
        return "create-offer";
    }

    @PostMapping("/offers/create")
    public String createOffer(@ModelAttribute Offer offer, Principal principal) {
        User user = userService.getAuthenticatedUser(principal);

        offer.setOwner(user);
        offer.setCreatedAt(LocalDateTime.now());

        offerRepository.save(offer);
        return "redirect:/offers";
    }

    @PostMapping("/offers/delete/{id}")
    public String deleteOffer(@PathVariable Long id, Principal principal) {
        User user = userService.getAuthenticatedUser(principal);
        Offer offer = offerRepository.findById(id).orElse(null);

        if (offer == null) {
            return "redirect:/offers";
        }

        if (!offer.getOwner().getId().equals(user.getId())) {
            return "redirect:/offers?error=forbidden";
        }

        offerRepository.delete(offer);
        return "redirect:/offers?deleted";
    }

    @PostMapping("/offers/request/{id}")
    public String requestLesson(@PathVariable Long id, Principal principal) {
        Offer offer = offerRepository.findById(id).orElse(null);

        if (offer == null) {
            return "redirect:/offers?error=notfound";
        }

        User requester = userService.getAuthenticatedUser(principal);
        User owner = offer.getOwner();

        if (requester.getId().equals(owner.getId())) {
            return "redirect:/offers?error=ownoffer";
        }

        if (requester.getCreditsBalance() < offer.getPriceCredits()) {
            return "redirect:/offers?error=notenoughcredits";
        }

        requester.setCreditsBalance(requester.getCreditsBalance() - offer.getPriceCredits());
        owner.setCreditsBalance(owner.getCreditsBalance() + offer.getPriceCredits());

        userRepository.save(requester);
        userRepository.save(owner);

        LessonRequest lessonRequest = new LessonRequest();
        lessonRequest.setOffer(offer);
        lessonRequest.setRequester(requester);
        lessonRequest.setCreatedAt(LocalDateTime.now());

        lessonRequestRepository.save(lessonRequest);

        return "redirect:/offers?success=requested";
    }
}