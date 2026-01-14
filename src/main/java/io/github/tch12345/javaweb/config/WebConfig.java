package io.github.tch12345.javaweb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost**", "https://localhost**")
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowCredentials(true);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射路径：前端通过 http://ip:port/uploads/文件名 访问
        registry.addResourceHandler("/uploads/**")
                // 物理路径：file: 后面接你磁盘的真实地址，注意末尾必须有斜杠 /
                .addResourceLocations("file:./uploads/");
    }
}