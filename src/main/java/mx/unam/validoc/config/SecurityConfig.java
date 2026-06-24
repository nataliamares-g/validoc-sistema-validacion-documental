package mx.unam.validoc.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import mx.unam.validoc.repository.AppUserRepository;

@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService userDetailsService(AppUserRepository users){return email -> users.findByEmail(email).map(u -> User.withUsername(u.getEmail()).password(u.getPassword()).roles(u.getRole()).build()).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));}
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception { return http.authorizeHttpRequests(a -> a.requestMatchers("/login","/css/**","/validar/**").permitAll().anyRequest().authenticated()).formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/",true).permitAll()).logout(l -> l.logoutSuccessUrl("/login?logout")).build(); }
}
