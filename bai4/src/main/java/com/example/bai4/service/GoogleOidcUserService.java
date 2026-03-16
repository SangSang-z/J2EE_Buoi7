package com.example.bai4.service;

import com.example.bai4.entity.Role;
import com.example.bai4.entity.Student;
import com.example.bai4.repo.RoleRepository;
import com.example.bai4.repo.StudentRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class GoogleOidcUserService extends OidcUserService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public GoogleOidcUserService(StudentRepository studentRepository,
                                 RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

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

        return new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "email"
        );
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
            base = name.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
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