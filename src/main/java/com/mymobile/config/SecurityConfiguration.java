package com.mymobile.config;

import com.mymobile.mapper.MapStructMapper;
import com.mymobile.model.enumerated.UserRoleEnum;
import com.mymobile.repository.UserRepository;
import com.mymobile.service.MobileleUserDetailsService;
import com.mymobile.service.oauth.OAuthSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableMethodSecurity // for @PreAuthorize to work
public class SecurityConfiguration {

    private final String rememberMeKey;

    public SecurityConfiguration(@Value("${my-mobile.remember-me-key}") String rememberMeKey) {

        this.rememberMeKey = rememberMeKey;
    }

    @Bean
    public PasswordEncoder encode() {

        // return NoOpPasswordEncoder.getInstance();
        // return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository, MapStructMapper mapper) {
        return new MobileleUserDetailsService(userRepository, mapper);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           OAuthSuccessHandler oAuthSuccessHandler) throws Exception {

        return httpSecurity // .csrf(AbstractHttpConfigurer::disable)
                // defines which pages will be authorized
                .authorizeHttpRequests((auth) -> {
                    auth
                            // allow access to all static locations defined in StaticResourceLocation enum class
                            // (images, css, js, webjars, etc.)
                            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                            // allow actuator endpoints
                            .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                            .requestMatchers("/test/**").permitAll()
                            // the URLs below are available for all users - logged in and anonymous
                            .requestMatchers(
                                    "/",
                                    "/index",
                                    "/users/login",
                                    "/users/register",
                                    "/users/registration-success",
                                    "/users/login-error",
                                    "/error"
                            ).permitAll()
                            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                            // the URLs below are available only for moderators or admins
                            .requestMatchers("/pages/moderators").hasAnyRole(UserRoleEnum.MODERATOR.name(),
                                    UserRoleEnum.ADMIN.name())
                            // the URLs below are available only for admins
                            .requestMatchers("/pages/admins").hasRole(UserRoleEnum.ADMIN.name())
                            .requestMatchers("/brands/**").permitAll()
                            .requestMatchers("/api/**").permitAll()
                            .requestMatchers("/offers/add").authenticated()
                            .requestMatchers(HttpMethod.GET, "/offers/**").permitAll()
                            // all other requests are authenticated
                            .anyRequest().authenticated();
                })
                .formLogin(form -> {
                    form
                            // redirect here when we access something which is not allowed
                            // also this is the page where we perform login
                            .loginPage("/users/login")
                            .loginProcessingUrl("/users/login")
                            .failureForwardUrl("/users/login-error")
                            // where do we go after login (use true argument if you want to go there,
                            // otherwise go to previous page)
                            .defaultSuccessUrl("/" /*,true*/) // arg alwaysUse: true
                            // the names of the "username" and "password" input fields in the custom login form
                            .usernameParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY)
                            .passwordParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY)
                            .permitAll();
                })
                .logout(logout -> {
                    logout
                            // the URL where we should POST in order to perform the logout
                            .logoutUrl("/users/logout")
                            // where to go when logged out
                            .logoutSuccessUrl("/")
                            .clearAuthentication(true)
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll();
                })
                .rememberMe(rememberMe -> {
                    rememberMe
                            .key(rememberMeKey)
                            .tokenValiditySeconds(604800)
                            .rememberMeParameter("remember-me-par")
                            .rememberMeCookieName("remember-me-cookie");
                    // https://docs.spring.io/spring-security/reference/servlet/authentication/rememberme.html
                })
                .securityContext(context -> {
                    context.securityContextRepository(securityContextRepository());
                })
                .oauth2Login(oauth -> {
                    oauth.successHandler(oAuthSuccessHandler);
                })
                .build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }
}
