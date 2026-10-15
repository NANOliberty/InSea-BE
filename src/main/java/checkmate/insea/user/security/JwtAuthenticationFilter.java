package checkmate.insea.user.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 추가
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import checkmate.insea.user.model.User;
import checkmate.insea.user.repository.UserRepository;
import checkmate.insea.user.service.CustomUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailService userDetailService;
    private final UserRepository userRepository; // 추가

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/login/oauth2/")
                || path.startsWith("/oauth2/authorization/")
                || path.startsWith("/api/auth/kakao/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt) && jwtUtil.isAccessToken(jwt)) {
                String email = jwtUtil.getEmailFromToken(jwt);
                
                // UserDetails 조회 (권한 정보를 위해)
                UserDetails userDetails = userDetailService.loadUserByUsername(email);
                
                // 실제 User 엔티티 조회
                Optional<User> userOptional = userRepository.findByEmailAndIsActiveTrue(email);
                
                if (userDetails != null && userOptional.isPresent()) {
                    User user = userOptional.get();
                    
                    // User 엔티티를 principal로 설정
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("User {} authenticated successfully", user.getEmail());
                } else {
                    log.warn("User not found for email: {}", email);
                }
            }
        } catch (UsernameNotFoundException ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}