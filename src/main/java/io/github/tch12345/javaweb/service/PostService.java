package io.github.tch12345.javaweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tch12345.javaweb.dto.PostDetails;
import io.github.tch12345.javaweb.repository.UserRepository;
import io.github.tch12345.javaweb.table.Post;
import io.github.tch12345.javaweb.repository.PostRepository;
import io.github.tch12345.javaweb.table.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public List<PostDetails> getPosts(String title, int pageNumber, Long id) {
        int pageSize = 10;
        int pageIndex = Math.max(pageNumber, 1) - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by("createdAt").descending());
        List<Post> posts;
        if (title == null || title.trim().isEmpty()) {
            posts = postRepository.findAll(pageable).getContent();
        } else {
            posts = postRepository.findByTitleContaining(title, pageable).getContent();
        }
        List<Long> authorIds = posts.stream()
                .map(Post::getPost_by)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> authorMap = authorIds.isEmpty() ? new HashMap<>() :
                userRepository.findAllById(authorIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getName));

        List<PostDetails> detailsList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        String baseUrl = "http://localhost:8080";

        posts.forEach(post -> {
            PostDetails details = new PostDetails();
            details.setId(post.getId());
            details.setTitle(post.getTitle());
            details.setContent(post.getContent());
            details.setPost_by(post.getPost_by());
            details.setCreatedAt(post.getCreatedAt());
            details.setAuthorName(authorMap.getOrDefault(post.getPost_by(), "unknown user"));
            details.setIsMine(id != null && id.equals(post.getPost_by()));
            String jsonStr = post.getImages();
            if (jsonStr != null && !jsonStr.isEmpty()) {
                try {
                    List<String> imageArray =
                            mapper.readValue(jsonStr, new TypeReference<>() {});

                    List<String> fullUrlList = imageArray.stream()
                            .map(path -> {
                                if (path.startsWith("http")) {
                                    return path; // 已经是完整地址就不处理
                                }
                                return baseUrl + path;
                            })
                            .toList();
                    details.setImages(mapper.writeValueAsString(fullUrlList));
                } catch (Exception e) {
                    details.setImages("[]");
                }
            }else{
                details.setImages("[]");
            }
            detailsList.add(details);
        });

        return detailsList;
    }

    public Post getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        ObjectMapper mapper = new ObjectMapper();
        String baseUrl = "http://localhost:8080";
        String jsonStr = post.getImages();

        if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
                List<String> imageArray = mapper.readValue(jsonStr, new TypeReference<>() {});
                List<String> fullUrlList = imageArray.stream()
                        .map(path -> path.startsWith("http") ? path : baseUrl + path)
                        .toList();
                post.setImages(mapper.writeValueAsString(fullUrlList));
            } catch (Exception e) {
                post.setImages("[]");
            }
        } else {
            post.setImages("[]");
        }

        return post;
    }

}