package com.example.bai4.repo;

import com.example.bai4.entity.Course;
import com.example.bai4.entity.Enrollment;
import com.example.bai4.entity.Student;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentAndCourse(Student student, Course course);
    List<Enrollment> findByStudent(Student student);
}