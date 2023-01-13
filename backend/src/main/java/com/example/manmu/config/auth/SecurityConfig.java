package com.example.manmu.config.auth;



import com.example.manmu.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 인가 정책 설정
        http
                .csrf().disable()
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests() //요청에 대한 보안 검사 실행
                .antMatchers("/", "/css/**", "/images/**",
                        "/js/**", "/h2-console/**", "/**").permitAll() //해당 URL은 모두 허용
                .antMatchers("/api/v1/**").hasRole(Role.USER.name()) //권한 제한
                .anyRequest().authenticated() //어떠한 요청에도 인증을 받도록 설정
                .and()
                //로그아웃 처리
                .logout()
                .logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
                .deleteCookies("SESSION") // 로그아웃 후 쿠키 삭제
                .and()
                //로그인 성공 후 처리
                .oauth2Login()
                .userInfoEndpoint()
                .userService(customOAuth2UserService);

    }

}