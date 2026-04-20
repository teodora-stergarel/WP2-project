package com.wp.skillswap.controller;

import com.wp.skillswap.model.Offer;
import com.wp.skillswap.model.User;
import com.wp.skillswap.repository.OfferRepository;
import com.wp.skillswap.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final OfferRepository offerRepository;

    public AdminController(UserRepository userRepository, OfferRepository offerRepository) {
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model, Principal principal) {

        List<User> users = userRepository.findAll();
        List<Offer> offers = offerRepository.findAll();

        int userCount = users.size();
        int offerCount = offers.size();

        int newUsers = userCount;
        int newOffers = offerCount;
        int creditsSwapped = 0;

        String topTeacher = users.isEmpty() ? "N/A" : users.get(0).getName();
        String adminName = principal.getName();

        model.addAttribute("adminName", adminName);
        model.addAttribute("userCount", userCount);
        model.addAttribute("offerCount", offerCount);
        model.addAttribute("newUsers", newUsers);
        model.addAttribute("newOffers", newOffers);
        model.addAttribute("creditsSwapped", creditsSwapped);
        model.addAttribute("topTeacher", topTeacher);

        return "admin";
    }

    @GetMapping("/admin/offers")
    public String adminOffers(Model model) {
        List<Offer> offers = offerRepository.findAll();
        model.addAttribute("offers", offers);
        return "admin-offers";
    }

    @GetMapping("/admin/offers/delete/{id}")
    public String deleteOffer(@PathVariable Long id) {
        offerRepository.deleteById(id);
        return "redirect:/admin/offers";
    }
}