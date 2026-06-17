package com.wp.skillswap.controller;

import com.wp.skillswap.repository.OfferRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final OfferRepository offerRepository;

    public HomeController(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredOffers", offerRepository.findAll());
        return "home";
    }
}