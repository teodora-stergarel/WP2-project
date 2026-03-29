package com.wp.skillswap.controller;

import com.wp.skillswap.model.LessonRequest;
import com.wp.skillswap.model.Offer;
import com.wp.skillswap.model.User;
import com.wp.skillswap.repository.LessonRequestRepository;
import com.wp.skillswap.repository.OfferRepository;
import com.wp.skillswap.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class OfferController {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final LessonRequestRepository lessonRequestRepository;

    public OfferController(OfferRepository offerRepository,
                           UserRepository userRepository,
                           LessonRequestRepository lessonRequestRepository) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.lessonRequestRepository = lessonRequestRepository;
    }

    @GetMapping("/offers")
    public String listOffers(Model model) {
        model.addAttribute("offers", offerRepository.findAll());
        return "offers";
    }

    @GetMapping("/offers/create")
    public String showCreateForm(Model model) {
        model.addAttribute("offer", new Offer());
        return "create-offer";
    }

    @PostMapping("/offers/create")
    public String createOffer(@ModelAttribute Offer offer) {
        User user = userRepository.findAll().stream().findFirst().orElse(null);

        if (user == null) {
            return "redirect:/register";
        }

        offer.setOwner(user);
        offer.setCreatedAt(LocalDateTime.now());

        offerRepository.save(offer);

        return "redirect:/offers";
    }

    @PostMapping("/offers/delete/{id}")
    public String deleteOffer(@PathVariable Long id) {
        offerRepository.deleteById(id);
        return "redirect:/offers";
    }

    @PostMapping("/offers/request/{id}")
    public String requestLesson(@PathVariable Long id) {
        Offer offer = offerRepository.findById(id).orElse(null);

        if (offer == null) {
            return "redirect:/offers";
        }

        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            return "redirect:/register";
        }

        User owner = offer.getOwner();
        User requester = null;

        for (User u : users) {
            if (!u.getId().equals(owner.getId())) {
                requester = u;
                break;
            }
        }

        if (requester == null) {
            return "redirect:/offers?error=noseconduser";
        }

        if (requester.getCreditsBalance() < offer.getPriceCredits()) {
            return "redirect:/offers?error=notenoughcredits";
        }

        requester.setCreditsBalance(
                requester.getCreditsBalance() - offer.getPriceCredits()
        );

        owner.setCreditsBalance(
                owner.getCreditsBalance() + offer.getPriceCredits()
        );

        userRepository.save(requester);
        userRepository.save(owner);

        LessonRequest request = new LessonRequest();
        request.setOffer(offer);
        request.setRequester(requester);
        request.setCreatedAt(LocalDateTime.now());

        lessonRequestRepository.save(request);

        return "redirect:/offers?success=requested";
    }
}