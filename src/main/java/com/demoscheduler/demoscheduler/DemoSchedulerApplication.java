package com.demoscheduler.demoscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.fasterxml.jackson.databind.MapperFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

@SpringBootApplication
public class DemoSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoSchedulerApplication.class, args);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer caseInsensitiveJson() {
        return builder -> builder.featuresToEnable(
                MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES
        );
    }
}
