package com.example.bai4.service;

import com.example.bai4.entity.Category;
import com.example.bai4.entity.Course;
import com.example.bai4.repo.CategoryRepository;
import com.example.bai4.repo.CourseRepository;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public CourseService(CourseRepository courseRepository,
                         CategoryRepository categoryRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<Course> getCourses(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (keyword != null && !keyword.trim().isEmpty()) {
            return courseRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        }

        return courseRepository.findAll(pageable);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học phần"));
    }

    @Transactional
    public void saveCourse(Course course, Long categoryId, MultipartFile imageFile) throws IOException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        course.setCategory(category);

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = storeFile(imageFile);
            course.setImage(fileName);
        }

        courseRepository.save(course);
    }

    @Transactional
    public void updateCourse(Long id, Course formCourse, Long categoryId, MultipartFile imageFile) throws IOException {
        Course course = getById(id);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        course.setName(formCourse.getName());
        course.setCredits(formCourse.getCredits());
        course.setLecturer(formCourse.getLecturer());
        course.setCategory(category);

        if (imageFile != null && !imageFile.isEmpty()) {
            if (course.getImage() != null && !course.getImage().isBlank()) {
                deleteFile(course.getImage());
            }
            String fileName = storeFile(imageFile);
            course.setImage(fileName);
        }

        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) throws IOException {
        Course course = getById(id);

        if (course.getImage() != null && !course.getImage().isBlank()) {
            deleteFile(course.getImage());
        }

        courseRepository.delete(course);
    }

    private String storeFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String originalName = file.getOriginalFilename();
        String ext = "";

        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        String newFileName = UUID.randomUUID() + ext;
        Path targetPath = uploadPath.resolve(newFileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return newFileName;
    }

    private void deleteFile(String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadPath.resolve(fileName);
        Files.deleteIfExists(filePath);
    }
}