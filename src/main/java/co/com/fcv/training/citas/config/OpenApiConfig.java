package co.com.fcv.training.citas.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "FCV Training Citas API", version = "0.5", description = "API REST del laboratorio de agendamiento de citas sintéticas."),
        tags = {
                @Tag(name = "Autenticación", description = "Registro, sesión y recuperación de contraseña."),
                @Tag(name = "Agenda", description = "Oferta, disponibilidad y ciclo de vida de citas."),
                @Tag(name = "Perfil", description = "Perfil del usuario autenticado.")
        })
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Access JWT obtenido mediante /api/v1/auth/login.")
class OpenApiConfig { }
