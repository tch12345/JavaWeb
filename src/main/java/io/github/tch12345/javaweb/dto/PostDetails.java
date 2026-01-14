package io.github.tch12345.javaweb.dto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostDetails {
    private Long id;
    private String title;
    private String content;
    private String images;
    private Long post_by;
    private String authorName;
    private Boolean isMine;
    private LocalDateTime createdAt;
}
