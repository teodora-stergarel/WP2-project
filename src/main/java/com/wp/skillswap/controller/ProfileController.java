package com.wp.skillswap.controller;

import com.wp.skillswap.model.LessonRequest;
import com.wp.skillswap.model.Offer;
import com.wp.skillswap.model.RequestStatus;
import com.wp.skillswap.model.User;
import com.wp.skillswap.repository.LessonRequestRepository;
import com.wp.skillswap.repository.OfferRepository;
import com.wp.skillswap.repository.UserRepository;
import com.wp.skillswap.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final OfferRepository offerRepository;
    private final LessonRequestRepository lessonRequestRepository;

    public ProfileController(UserRepository userRepository,
                             UserService userService,
                             OfferRepository offerRepository,
                             LessonRequestRepository lessonRequestRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.offerRepository = offerRepository;
        this.lessonRequestRepository = lessonRequestRepository;
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal,
                              @RequestParam(defaultValue = "0") int offerIndex) {
        User user = userService.getAuthenticatedUser(principal);

        List<Offer> myOffers = offerRepository.findByOwner(user);
        List<LessonRequest> incomingRequests = lessonRequestRepository.findByOfferOwner(user);

        if (!myOffers.isEmpty() && offerIndex >= myOffers.size()) {
            offerIndex = 0;
        }

        boolean isAdmin = user.getRole().name().equals("ADMIN");

        model.addAttribute("user", user);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("myOffers", myOffers);
        model.addAttribute("incomingRequests", incomingRequests);
        model.addAttribute("currentOfferIndex", offerIndex);

        if (isAdmin) {
            model.addAttribute("userCount", userRepository.count());
            model.addAttribute("offerCount", offerRepository.count());
            return "admin-profile";
        }

        return "profile";
    }

    @GetMapping("/profile/{id}")
    public String viewUserProfile(@PathVariable Long id, Model model, Principal principal) {
        User profileUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id"));

        User currentUser = userService.getAuthenticatedUser(principal);

        if (profileUser.getId().equals(currentUser.getId())) {
            return "redirect:/profile";
        }

        List<Offer> userOffers = offerRepository.findByOwner(profileUser);

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("userOffers", userOffers);
        model.addAttribute("isAdmin", currentUser.getRole().name().equals("ADMIN"));

        return "user-profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, Principal principal) {
        User user = userService.getAuthenticatedUser(principal);
        List<Offer> myOffers = offerRepository.findByOwner(user);
        model.addAttribute("user", user);
        model.addAttribute("myOffers", myOffers);
        return "edit-profile";
    }

    @PostMapping("/profile/update-name")
    public String updateName(@RequestParam("name") String name,
                             @RequestParam("bio") String bio,
                             Principal principal) {
        User user = userService.getAuthenticatedUser(principal);
        user.setName(name);
        user.setBio(bio);
        userRepository.save(user);
        return "redirect:/profile";
    }

    @PostMapping("/profile/update-bio")
    public String updateBio(@RequestParam("bio") String bio, Principal principal) {
        User user = userService.getAuthenticatedUser(principal);
        user.setBio(bio);
        userRepository.save(user);
        return "redirect:/profile?bioUpdated";
    }

    @PostMapping("/profile/upload-image")
    public String uploadImage(@RequestParam("image") MultipartFile image,
                              Principal principal) throws IOException {
        User user = userService.getAuthenticatedUser(principal);
        if (!image.isEmpty()) {
            String savedPath = saveFile(image);
            user.setProfileImagePath("/uploads/" + savedPath);
            userRepository.save(user);
        }

        boolean isAdmin = user.getRole().name().equals("ADMIN");
        return isAdmin ? "redirect:/profile" : "redirect:/profile/edit?imageUpdated";
    }

    @PostMapping("/profile/upload-cover")
    public String uploadCover(@RequestParam("cover") MultipartFile cover,
                              Principal principal) throws IOException {
        User user = userService.getAuthenticatedUser(principal);
        if (!cover.isEmpty()) {
            String savedPath = saveFile(cover);
            user.setCoverImagePath("/uploads/" + savedPath);
            userRepository.save(user);
        }

        boolean isAdmin = user.getRole().name().equals("ADMIN");
        return isAdmin ? "redirect:/profile" : "redirect:/profile/edit?coverUpdated";
    }

    @PostMapping("/profile/requests/accept/{id}")
    public String acceptRequest(@PathVariable Long id) {
        LessonRequest request = lessonRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid request id"));
        request.setStatus(RequestStatus.ACCEPTED);
        lessonRequestRepository.save(request);
        return "redirect:/profile";
    }

    @PostMapping("/profile/requests/reject/{id}")
    public String rejectRequest(@PathVariable Long id) {
        LessonRequest request = lessonRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid request id"));
        request.setStatus(RequestStatus.REJECTED);
        lessonRequestRepository.save(request);
        return "redirect:/profile";
    }

    private String saveFile(MultipartFile file) throws IOException {
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID() + extension;
        file.transferTo(new File(uploadDir + File.separator + fileName));
        return fileName;
    }
}