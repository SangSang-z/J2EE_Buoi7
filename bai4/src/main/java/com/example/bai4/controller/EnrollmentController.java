package com.example.bai4.controller;

import com.example.bai4.entity.Student;
import com.example.bai4.service.EnrollmentService;
import com.example.bai4.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService;

    public EnrollmentController(EnrollmentService enrollmentService,
                                StudentService studentService) {
        this.enrollmentService = enrollmentService;
        this.studentService = studentService;
    }

    @PostMapping("/enroll/{courseId}")
    public String enrollCourse(@PathVariable Long courseId,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.findByUsernameOrEmail(authentication.getName());
            enrollmentService.enrollCourse(student, courseId);
            redirectAttributes.addFlashAttribute("success", "Đăng ký học phần thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/home";
    }

    @GetMapping("/my-courses")
    public String myCourses(Authentication authentication, Model model) {
        Student student = studentService.findByUsernameOrEmail(authentication.getName());
        model.addAttribute("enrollments", enrollmentService.getMyCourses(student));
        return "my-courses";
    }
}