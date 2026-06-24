package com.project01.javadeserialization.dto;


import com.project01.javadeserialization.entity.User;
import com.project01.javadeserialization.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class CustomRememberMeServices extends AbstractRememberMeServices {

    private final UserRepository userRepository;

    public CustomRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
        this.userRepository = ((CustomUserDetailsService) userDetailsService).getUserRepository();
        setCookieName("user");
        setParameter("remember-me");
    }

    @Override
    protected String[] decodeCookie(String cookieValue) throws InvalidCookieException {
        return new String[]{cookieValue};
    }

    @Override
    protected String extractRememberMeCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(getCookieName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
                                  Authentication successfulAuthentication) {

        String username = successfulAuthentication.getName();
        User user = userRepository.findByUsername(username);

        VulnerableRememberMeToken token = new VulnerableRememberMeToken();
        token.setUsername(user.getUsername());
        token.setPassword(user.getPassword());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(token);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            Cookie cookie = new Cookie("user", base64);
            cookie.setMaxAge(604800);
            cookie.setPath("/");
            response.addCookie(cookie);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected org.springframework.security.core.userdetails.UserDetails processAutoLoginCookie(
            String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) {

        try {
            byte[] decoded = Base64.getDecoder().decode(cookieTokens[0]);
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decoded))) {
                Object obj = ois.readObject();
                if (obj instanceof VulnerableRememberMeToken token) {
                    return getUserDetailsService().loadUserByUsername(token.getUsername());
                }
            }
        } catch (Exception e) {
            throw new InvalidCookieException("Invalid remember-me cookie");
        }
        return null;
    }
}