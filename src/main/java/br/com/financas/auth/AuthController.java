package br.com.financas.auth;

import br.com.financas.auth.dto.CadastroRequest;
import br.com.financas.auth.dto.LoginRequest;
import br.com.financas.auth.dto.TokenResponse;
import br.com.financas.auth.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @Operation(summary = "Cadastra um novo usuário")
    public UsuarioResponse cadastrar(@RequestBody @Valid CadastroRequest request) {
        return service.cadastrar(request);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Faz login e devolve o token JWT (demo: demo@financas.local / demo1234)")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        return service.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário autenticado")
    public UsuarioResponse eu() {
        return service.eu();
    }
}
