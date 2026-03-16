package com.example.bai4.config;

import java.util.Set;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication) {
        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        boolean isAdmin = false;
        boolean isStudent = false;
        String currentUsername = "";

        if (isAuthenticated) {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            isAdmin = roles.contains("ROLE_ADMIN");
            isStudent = roles.contains("ROLE_STUDENT");
            currentUsername = authentication.getName();

            if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
                Object email = oauth2User.getAttributes().get("email");
                if (email != null) {
                    currentUsername = email.toString();
                }
            }
        }

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStudent", isStudent);
        model.addAttribute("currentUsername", currentUsername);
    }
}