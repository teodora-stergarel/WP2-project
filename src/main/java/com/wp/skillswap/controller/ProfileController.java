package com.wp.skillswap.controller;

import com.wp.skillswap.model.User;
import com.wp.skillswap.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = userRepository.findAll().stream().findFirst().orElse(null);

        if (user == null) {
            return "redirect:/register";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update-bio")
    public String updateBio(@RequestParam("bio") String bio) {
        User user = userRepository.findAll().stream().findFirst().orElse(null);

        if (user == null) {
            return "redirect:/register";
        }

        user.setBio(bio);
        userRepository.save(user);

        return "redirect:/profile?success";
    }

    @PostMapping("/profile/upload-image")
    public String uploadImage(@RequestParam("image") MultipartFile image) throws IOException {
        User user = userRepository.findAll().stream().findFirst().orElse(null);

        if (user == null) {
            return "redirect:/register";
        }

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

        return "redirect:/profile?imageSuccess";
    }
}