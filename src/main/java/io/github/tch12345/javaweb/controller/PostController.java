package io.github.tch12345.javaweb.controller;

import io.github.tch12345.javaweb.dto.ApiResponse;
import io.github.tch12345.javaweb.dto.PostDetails;
import io.github.tch12345.javaweb.repository.PostRepository;
import io.github.tch12345.javaweb.service.AuthService;
import io.github.tch12345.javaweb.service.PostService;
import io.github.tch12345.javaweb.table.Post;
import io.github.tch12345.javaweb.table.User;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private PostService postService;

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
        if (files != null) {
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


    @PutMapping("/update/{postId}")
    public ResponseEntity<ApiResponse<Post>> updatePost(
            @PathVariable Long postId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "images", required = false) MultipartFile[] files,
            @RequestHeader(value = "Authorization", required = false) String token
    ) throws IOException {

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authorization header is missing"));
        }

        String pureToken = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        User user = authService.getUserFromToken(pureToken);
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Session expired"));
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Post not found"));
        }

        if (!post.getPost_by().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("No permission to update this post"));
        }

        if (title != null) {
            post.setTitle(title);
        }

        if (content != null) {
            post.setContent(content);
        }

        if (files != null && files.length > 0) {
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) {
                boolean success = uploadDir.mkdirs();
                if (!success) {
                    return ResponseEntity.status(500).body(ApiResponse.error("cant create directory"));
                }
            }

            List<String> savedImagePaths = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    file.transferTo(new File(uploadDir.getAbsolutePath() + File.separator + fileName));
                    savedImagePaths.add("/uploads/" + fileName);
                }
                }

            System.out.println(savedImagePaths);
            post.setImages(objectMapper.writeValueAsString(savedImagePaths));
        }

        postRepository.save(post);

        return ResponseEntity.ok(
                ApiResponse.success("Post updated successfully")
        );
    }

    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authorization header is missing"));
        }

        String pureToken = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        User user = authService.getUserFromToken(pureToken);
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Session expired"));
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Post not found"));
        }

        if (!post.getPost_by().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("No permission to delete this post"));
        }

        postRepository.delete(post);

        return ResponseEntity.ok(
                ApiResponse.success("Post deleted successfully", null)
        );
    }

    @PostMapping("/list")
    public ResponseEntity<List<PostDetails>> listPosts(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") int page,
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        Long currentUserId = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (!token.isBlank() && !token.equals("null") && !token.equals("undefined")) {
                try {
                    User user = authService.getUserFromToken(token);
                    currentUserId = user.getId();
                } catch (Exception ignored) {
                }
            }
        }
        List<PostDetails> posts = postService.getPosts(title, page, currentUserId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostDetails(
            @PathVariable Long id
    ) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

}
