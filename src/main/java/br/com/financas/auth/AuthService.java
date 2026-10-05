package br.com.financas.auth;

import br.com.financas.auth.dto.CadastroRequest;
import br.com.financas.auth.dto.LoginRequest;
import br.com.financas.auth.dto.TokenResponse;
import br.com.financas.auth.dto.UsuarioResponse;
import br.com.financas.shared.exception.CredenciaisInvalidasException;
import br.com.financas.shared.exception.RecursoNaoEncontradoException;
import br.com.financas.shared.exception.RegraNegocioException;
import br.com.financas.shared.usuario.UsuarioLogado;
import br.com.financas.usuario.Usuario;
import br.com.financas.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UsuarioLogado usuarioLogado;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService,
                       UsuarioLogado usuarioLogado) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.usuarioLogado = usuarioLogado;
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest request) {
        String email = normalizar(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Já existe um usuário com o e-mail '" + email + "'");
        }
        Usuario usuario = usuarioRepository.save(
                new Usuario(request.nome().trim(), email, passwordEncoder.encode(request.senha())));
        return UsuarioResponse.de(usuario);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizar(request.email()))
                .filter(u -> passwordEncoder.matches(request.senha(), u.getSenhaHash()))
                .orElseThrow(CredenciaisInvalidasException::new);
        return tokenService.gerar(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse eu() {
        Long id = usuarioLogado.getId();
        return usuarioRepository.findById(id)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
