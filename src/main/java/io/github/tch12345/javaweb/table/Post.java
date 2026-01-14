package io.github.tch12345.javaweb.table;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "post")
public class Post {
    // ===== getters & setters =====
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String images;

    @Column(nullable = false)
    private Long post_by;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
