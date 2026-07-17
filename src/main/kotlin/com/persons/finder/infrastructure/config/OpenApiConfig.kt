package com.persons.finder.infrastructure.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Persons Finder API")
                .version("1.0.0")
                .description(
                    """
                    REST API for a mobile app that helps users find people nearby.
                    
                    Features:
                    - Create persons with AI-generated bios (prompt-injection protected)
                    - Update person locations
                    - Find nearby persons using Haversine great-circle distance
                    """.trimIndent()
                )
                .contact(
                    Contact()
                        .name("Persons Finder")
                        .url("https://github.com/persons-finder")
                )
                .license(
                    License()
                        .name("MIT")
                )
        )
}
