package com.mashup.music.search.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Config for the Swagger configuration
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * Create bean to expose swagger as part of the service.
     *
     * @return {@link Docket}
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.mashup.music.search"))
                .paths(PathSelectors.any())
                .build();
    }
}
