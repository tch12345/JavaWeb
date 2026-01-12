package io.github.tch12345.javaweb.controller;

import io.github.tch12345.javaweb.dto.ApiResponse;
import io.github.tch12345.javaweb.repository.PostRepository;
import io.github.tch12345.javaweb.service.AuthService;
import io.github.tch12345.javaweb.table.Post;
import io.github.tch12345.javaweb.table.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/create")
    public ResponseEntity<ApiResponse<Post>> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) MultipartFile[] files,
            @RequestHeader(value = "Authorization", required = false) String token) throws IOException {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authorization header is missing"));
        }
        String pureToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        User user = authService.getUserFromToken(pureToken);
        if (user == null) return ResponseEntity.status(401).body(ApiResponse.error("Session expired"));

        File uploadDir = new File("uploads");
        if (!uploadDir.exists()) {
            boolean success = uploadDir.mkdirs();
            if (!success) {
                return ResponseEntity.status(500).body(ApiResponse.error("cant create directory"));
            }
        }

        List<String> savedImagePaths = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    file.transferTo(new File(uploadDir.getAbsolutePath() + File.separator + fileName));
                    savedImagePaths.add("/uploads/" + fileName);
                }
            }
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setPost_by(user.getId());
        post.setImages(objectMapper.writeValueAsString(savedImagePaths));

        return ResponseEntity.ok(ApiResponse.success("Upload successful", postRepository.save(post)));
    }
}
