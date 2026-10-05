package br.com.financas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Finanças API")
                .description("API REST de finanças pessoais: contas, categorias, transações e relatórios.")
                .version("0.0.1"));
    }
}
