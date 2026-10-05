package br.com.financas.auth;

import br.com.financas.auth.dto.TokenResponse;
import br.com.financas.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final Duration expiracao;

    public TokenService(JwtEncoder encoder, @Value("${financas.jwt.expiracao}") Duration expiracao) {
        this.encoder = encoder;
        this.expiracao = expiracao;
    }

    /**
     * O "subject" do token é o id do usuário: é o que o UsuarioLogado lê em cada requisição.
     */
    public TokenResponse gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(expiracao);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("financas-api")
                .subject(usuario.getId().toString())
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .claim("email", usuario.getEmail())
                .claim("nome", usuario.getNome())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiraEm);
    }
}
