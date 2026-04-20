package com.wp.skillswap.controller;

import com.wp.skillswap.model.User;
import com.wp.skillswap.repository.UserRepository;
import com.wp.skillswap.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService;

    public ProfileController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        User user = userService.getAuthenticatedUser(principal);

        model.addAttribute("user", user);
        model.addAttribute("isAdmin", user.getRole().name().equals("ADMIN"));

        return "profile";
    }

    @PostMapping("/profile/update-bio")
    public String updateBio(@RequestParam("bio") String bio, Principal principal) {
        User user = userService.getAuthenticatedUser(principal);
        user.setBio(bio);
        userRepository.save(user);
        return "redirect:/profile?bioUpdated";
    }

    @PostMapping("/profile/upload-image")
    public String uploadImage(@RequestParam("image") MultipartFile image, Principal principal) throws IOException {
        User user = userService.getAuthenticatedUser(principal);

        if (!image.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
            File dir = new File(uploadDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = image.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;
            String filePath = uploadDir + File.separator + fileName;

            image.transferTo(new File(filePath));

            user.setProfileImagePath("/uploads/" + fileName);
            userRepository.save(user);
        }

        return "redirect:/profile?imageUpdated";
    }
}