error id: file://<WORKSPACE>/src/main/java/com/wp/skillswap/controller/OfferController.java:_empty_/OfferRepository#save#
file://<WORKSPACE>/src/main/java/com/wp/skillswap/controller/OfferController.java
empty definition using pc, found symbol in pc: _empty_/OfferRepository#save#
found definition using semanticdb; symbol com/wp/skillswap/controller/OfferController#offerRepository.
empty definition using fallback
non-local guesses:

offset: 4943
uri: file://<WORKSPACE>/src/main/java/com/wp/skillswap/controller/OfferController.java
text:
```scala
package com.wp.skillswap.controller;

import com.wp.skillswap.model.LessonRequest;
import com.wp.skillswap.model.Offer;
import com.wp.skillswap.model.RequestStatus;
import com.wp.skillswap.model.User;
import com.wp.skillswap.repository.LessonRequestRepository;
import com.wp.skillswap.repository.OfferRepository;
import com.wp.skillswap.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class OfferController {

    private final OfferRepository offerRepository;
    private final LessonRequestRepository lessonRequestRepository;
    private final UserService userService;

    public OfferController(OfferRepository offerRepository,
                           LessonRequestRepository lessonRequestRepository,
                           UserService userService) {
        this.offerRepository = offerRepository;
        this.lessonRequestRepository = lessonRequestRepository;
        this.userService = userService;
    }

    @GetMapping("/offers")
    public String showOffers(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String subject,
                             @RequestParam(required = false) Integer credits,
                             Model model) {

        List<Offer> offers;

        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasSubject = subject != null && !subject.trim().isEmpty() && !subject.equals("any");
        boolean hasCredits = credits != null;

        if (hasSubject && hasCredits) {
            offers = offerRepository.findByTitleContainingIgnoreCaseAndPriceCredits(subject, credits);
        } else if (hasSubject) {
            offers = offerRepository.findByTitleContainingIgnoreCase(subject);
        } else if (hasCredits) {
            offers = offerRepository.findByPriceCredits(credits);
        } else if (hasSearch) {
            offers = offerRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        } else {
            offers = offerRepository.findAll();
        }

        if (hasSearch && (hasSubject || hasCredits)) {
            offers = offers.stream()
                    .filter(offer ->
                            offer.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                            offer.getDescription().toLowerCase().contains(search.toLowerCase()))
                    .toList();
        }

        model.addAttribute("offers", offers);
        model.addAttribute("search", search);
        model.addAttribute("subject", subject);
        model.addAttribute("credits", credits);

        return "offers";
    }

    @GetMapping("/offers/{id}")
public String showOfferDetails(@PathVariable Long id, Model model, Principal principal) {
    Offer offer = offerRepository.findById(id).orElse(null);

    if (offer == null) {
        return "redirect:/offers?error=notfound";
    }

    boolean isOwner = false;

    if (principal != null) {
        User currentUser = userService.getAuthenticatedUser(principal);
        isOwner = offer.getOwner().getId().equals(currentUser.getId());
    }

    model.addAttribute("offer", offer);
    model.addAttribute("isOwner", isOwner);

    return "offer-details";
}

    @GetMapping("/offers/create")
    public String showCreateOfferPage(Model model) {
        model.addAttribute("offer", new Offer());
        return "create-offer";
    }

    @PostMapping("/offers/create")
    public String createOffer(@ModelAttribute Offer offer,
                            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
                            Principal principal) throws IOException {

        User user = userService.getAuthenticatedUser(principal);

        offer.setOwner(user);
        offer.setCreatedAt(LocalDateTime.now());

        if (attachment != null && !attachment.isEmpty()) {
            String uploadDir = "src/main/resources/static/uploads/";

            Files.createDirectories(Paths.get(uploadDir));

            String originalName = attachment.getOriginalFilename();

            if (originalName == null) {
                originalName = "attachment";
            }

            String safeName = originalName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

            String fileName = System.currentTimeMillis() + "_" + safeName;
            Path filePath = Paths.get(uploadDir + fileName);

            Files.write(filePath, attachment.getBytes());

            offer.setAttachmentPath("/uploads/" + fileName);
        }

        offerRepository.sa@@ve(offer);

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

        lessonRequestRepository.deleteByOffer(offer);
        offerRepository.delete(offer);

        return "redirect:/offers?success=deleted";
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

        LessonRequest lessonRequest = new LessonRequest();
        lessonRequest.setOffer(offer);
        lessonRequest.setRequester(requester);
        lessonRequest.setCreatedAt(LocalDateTime.now());
        lessonRequest.setStatus(RequestStatus.PENDING);

        lessonRequestRepository.save(lessonRequest);

        return "redirect:/lesson-requests?success=requested";
    }

    @GetMapping("/offers/edit/{id}")
public String showEditOfferPage(@PathVariable Long id, Model model, Principal principal) {
    User user = userService.getAuthenticatedUser(principal);
    Offer offer = offerRepository.findById(id).orElse(null);

    if (offer == null) {
        return "redirect:/profile?error=notfound";
    }

    if (!offer.getOwner().getId().equals(user.getId())) {
        return "redirect:/profile?error=forbidden";
    }

    model.addAttribute("offer", offer);
    return "edit-offer";
}

@PostMapping("/offers/edit/{id}")
public String editOffer(@PathVariable Long id,
                        @RequestParam String title,
                        @RequestParam Integer priceCredits,
                        @RequestParam String description,
                        Principal principal) {
    User user = userService.getAuthenticatedUser(principal);
    Offer offer = offerRepository.findById(id).orElse(null);

    if (offer == null) {
        return "redirect:/profile?error=notfound";
    }

    if (!offer.getOwner().getId().equals(user.getId())) {
        return "redirect:/profile?error=forbidden";
    }

    offer.setTitle(title);
    offer.setPriceCredits(priceCredits);
    offer.setDescription(description);

    offerRepository.save(offer);

    return "redirect:/profile?success=offerUpdated";
}

@GetMapping("/offers/{id}/request")
public String requestLessonPage(@PathVariable Long id, Model model) {

    Offer offer = offerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offer not found"));

    model.addAttribute("offer", offer);

    return "request-lesson";
}
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/OfferRepository#save#