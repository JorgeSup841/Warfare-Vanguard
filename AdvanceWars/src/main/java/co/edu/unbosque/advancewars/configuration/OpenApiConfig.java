package co.edu.unbosque.advancewars.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI(
            @Value("${app.openapi.title}") String title,
            @Value("${app.openapi.version}") String version) {
        SecurityScheme scheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        return new OpenAPI()
                .info(new Info().title(title).version(version)
                        .description("Autenticación mediante correo, OTP y JWT"))
                .components(new Components().addSecuritySchemes("bearerAuth", scheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
