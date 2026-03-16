package com.example.bai4.service;

import com.example.bai4.entity.Role;
import com.example.bai4.entity.Student;
import com.example.bai4.repo.RoleRepository;
import com.example.bai4.repo.StudentRepository;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public GoogleOAuth2UserService(StudentRepository studentRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"),
                    "Google không trả về email"
            );
        }

        Student student = studentRepository.findByEmail(email)
                .orElseGet(() -> createStudentFromGoogle(email, name));

        Set<GrantedAuthority> authorities = new HashSet<>();
        student.getRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()))
        );

        return new DefaultOAuth2User(authorities, attributes, "email");
    }

    private Student createStudentFromGoogle(String email, String name) {
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role STUDENT"));

        Student student = new Student();
        student.setEmail(email);
        student.setUsername(generateUniqueUsername(email, name));
        student.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        HashSet<Role> roles = new HashSet<>();
        roles.add(studentRole);
        student.setRoles(roles);

        return studentRepository.save(student);
    }

    private String generateUniqueUsername(String email, String name) {
        String base = null;

        if (name != null && !name.isBlank()) {
            base = name.trim().toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
        }

        if (base == null || base.isBlank()) {
            base = email.substring(0, email.indexOf("@"))
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
        }

        if (base.isBlank()) {
            base = "googleuser";
        }

        String candidate = base;
        int i = 1;

        while (studentRepository.existsByUsername(candidate)) {
            candidate = base + i;
            i++;
        }

        return candidate;
    }
}