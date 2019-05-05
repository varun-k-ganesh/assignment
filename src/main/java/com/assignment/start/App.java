package com.assignment.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Starting point of the application
 */
@SpringBootApplication
public class App {

    //Start the spring boot app
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    //Method to handle CORS security issue which doesn't allow to communicate with the server
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:8000");
            }
        };
    }
}
