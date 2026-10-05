package br.com.financas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Relógio injetável: o código pergunta "que dia é hoje?" ao Clock, e não ao sistema.
 * Assim, quando um teste precisar, dá para fixar a data substituindo este bean (ex.: relatório do "mês atual").
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
