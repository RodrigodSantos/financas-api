package br.com.financas.config;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Quem abre só o endereço base da API cai na documentação, e não num 401 que parece erro.
 */
@Hidden
@Controller
public class RaizController {

    @GetMapping("/")
    public RedirectView raiz() {
        return new RedirectView("/swagger-ui.html");
    }
}
