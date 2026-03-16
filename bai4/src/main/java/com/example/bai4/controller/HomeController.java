package com.example.bai4.controller;

import com.example.bai4.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.bai4.entity.Course;

@Controller
public class HomeController {

    private final CourseService courseService;

    public HomeController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping({"/", "/home", "/courses"})
    public String home(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {

        Page<Course> coursePage = courseService.getCourses(keyword, page, 5);

        model.addAttribute("coursePage", coursePage);
        model.addAttribute("keyword", keyword);

        return "home";
    }
}