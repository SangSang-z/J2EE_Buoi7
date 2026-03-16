package com.example.bai4.service;

import com.example.bai4.entity.Course;
import com.example.bai4.entity.Enrollment;
import com.example.bai4.entity.Student;
import com.example.bai4.repo.CourseRepository;
import com.example.bai4.repo.EnrollmentRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public void enrollCourse(Student student, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học phần"));

        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Bạn đã đăng ký học phần này rồi");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollDate(LocalDateTime.now());

        enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getMyCourses(Student student) {
        return enrollmentRepository.findByStudent(student);
    }
}