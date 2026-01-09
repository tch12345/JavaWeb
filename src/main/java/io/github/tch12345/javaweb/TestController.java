package io.github.tch12345.javaweb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String hello() {
        return "Backend is running!";
    }

    @GetMapping("/members")
    public List<Map<String, String>> getMembers() {
        // 模拟数据库返回数据
        List<Map<String, String>> members = new ArrayList<>();
        Map<String, String> m1 = new HashMap<>();
        m1.put("name", "Alice");
        m1.put("email", "alice@example.com");
        members.add(m1);

        Map<String, String> m2 = new HashMap<>();
        m2.put("name", "Bob");
        m2.put("email", "bob@example.com");
        members.add(m2);

        return members;
    }
}
