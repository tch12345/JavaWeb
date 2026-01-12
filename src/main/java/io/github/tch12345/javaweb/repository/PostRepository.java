package io.github.tch12345.javaweb.repository;
import io.github.tch12345.javaweb.table.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

}
