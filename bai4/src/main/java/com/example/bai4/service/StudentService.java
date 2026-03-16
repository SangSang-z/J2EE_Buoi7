package com.example.bai4.service;

import com.example.bai4.dto.RegisterRequest;
import com.example.bai4.entity.Role;
import com.example.bai4.entity.Student;
import com.example.bai4.repo.RoleRepository;
import com.example.bai4.repo.StudentRepository;
import java.util.HashSet;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerStudent(RegisterRequest request) {
        if (studentRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role STUDENT"));

        Student student = new Student();
        student.setUsername(request.getUsername());
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student.setEmail(request.getEmail());

        HashSet<Role> roles = new HashSet<>();
        roles.add(studentRole);
        student.setRoles(roles);

        studentRepository.save(student);
    }

    public Student findByUsername(String username) {
        return studentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));
    }
    
    public Student findByUsernameOrEmail(String value) {
    return studentRepository.findByUsername(value)
            .or(() -> studentRepository.findByEmail(value))
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));
}
}