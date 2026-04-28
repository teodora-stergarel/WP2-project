package com.wp.skillswap.controller;

import com.wp.skillswap.model.Offer;
import com.wp.skillswap.model.User;
import com.wp.skillswap.repository.LessonRequestRepository;
import com.wp.skillswap.repository.OfferRepository;
import com.wp.skillswap.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final LessonRequestRepository lessonRequestRepository;

    public AdminController(UserRepository userRepository,
                           OfferRepository offerRepository,
                           LessonRequestRepository lessonRequestRepository) {
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
        this.lessonRequestRepository = lessonRequestRepository;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model, Principal principal) {
        List<User> users = userRepository.findAll();
        List<Offer> offers = offerRepository.findAll();

        model.addAttribute("adminName", principal.getName());
        model.addAttribute("userCount", users.size());
        model.addAttribute("offerCount", offers.size());
        model.addAttribute("newUsers", users.size());
        model.addAttribute("newOffers", offers.size());
        model.addAttribute("creditsSwapped", 0);
        model.addAttribute("topTeacher", users.isEmpty() ? "N/A" : users.get(0).getName());

        return "admin";
    }

    @GetMapping("/admin/accounts")
    public String adminAccounts(Model model, Principal principal) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("currentAdminEmail", principal.getName());

        return "admin-accounts";
    }

    @PostMapping("/admin/accounts/delete/{id}")
    @Transactional
    public String deleteUser(@PathVariable Long id, Principal principal) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + id));

        if (user.getEmail().equals(principal.getName())) {
            return "redirect:/admin/accounts";
        }

        if (user.getRole().name().equals("ADMIN")) {
            return "redirect:/admin/accounts";
        }

        List<Offer> userOffers = offerRepository.findByOwner(user);

        lessonRequestRepository.deleteByRequester(user);

        for (Offer offer : userOffers) {
            lessonRequestRepository.deleteByOffer(offer);
        }

        offerRepository.deleteAll(userOffers);
        userRepository.delete(user);

        return "redirect:/admin/accounts";
    }

    @GetMapping("/admin/users/report/{id}")
    public String reportUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + id));

        if (user.getPenalties() == null) {
            user.setPenalties(0);
        }

        user.setPenalties(user.getPenalties() + 1);
        userRepository.save(user);

        return "redirect:/admin/accounts";
    }

    @GetMapping("/admin/offers")
    public String adminOffers(Model model) {
        model.addAttribute("offers", offerRepository.findAll());

        return "admin-offers";
    }

    @GetMapping("/admin/offers/{id}")
    public String adminOfferDetails(@PathVariable Long id, Model model) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid offer id: " + id));

        model.addAttribute("offer", offer);

        return "admin-offer-details";
    }

    @GetMapping("/admin/offers/delete/{id}")
    @Transactional
    public String deleteOffer(@PathVariable Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid offer id: " + id));

        lessonRequestRepository.deleteByOffer(offer);
        offerRepository.delete(offer);

        return "redirect:/admin/offers";
    }
}