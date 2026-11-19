package com.kiet.examproject.controller;

import com.kiet.examproject.model.ApiResponse;
import com.kiet.examproject.model.Student;
import com.kiet.examproject.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * ============================================================================
 * REST CONTROLLER LAYER: StudentController
 * ============================================================================
 * Exposes RESTful HTTP endpoints for all CRUD and examination-specific operations.
 * Base Path: /api/students
 * 
 * VIVA EXPLANATION - WHAT IS @RestController?
 * @RestController is a convenience archetype annotation combining @Controller and @ResponseBody.
 * Every handler method automatically serializes the returned Java objects directly into
 * JSON HTTP response bodies using Jackson converter, without requiring a view template.
 * 
 * VIVA EXPLANATION - WHAT IS ResponseEntity?
 * ResponseEntity<T> represents the complete HTTP response including HTTP status codes
 * (200 OK, 201 Created, 400 Bad Request, 404 Not Found), custom response headers,
 * and the response body.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*") // Allows local development and cross-origin requests
public class StudentController {

    // Service dependency injected via Constructor Injection
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ========================================================================
    // 1. ADD A STUDENT (POST /api/students)
    // ========================================================================
    /**
     * Q3.1: Add a student.
     * Validates scores, prevents duplicate IDs, and persists student + skills.
     * Returns HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Student>> addStudent(@RequestBody Student student) {
        try {
            Student created = studentService.addStudent(student);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Student created successfully with ID: " + created.getStudentId(), created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal Server Error: " + e.getMessage()));
        }
    }

    // ========================================================================
    // 2. RETRIEVE ALL STUDENTS (GET /api/students)
    // ========================================================================
    /**
     * Q3.2: Retrieve all students.
     * Returns HTTP 200 OK with list of students.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
        ArrayList<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + students.size() + " students.", students));
    }

    // ========================================================================
    // 3. SEARCH FOR STUDENT BY ID (GET /api/students/{id})
    // ========================================================================
    /**
     * Q3.3: Search for a student by ID.
     * Returns HTTP 200 OK if found, or HTTP 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable("id") int id) {
        try {
            Student student = studentService.getStudentById(id);
            return ResponseEntity.ok(ApiResponse.success("Student found.", student));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Student with ID " + id + " was not found."));
        }
    }

    // ========================================================================
    // 4. UPDATE STUDENT SCORES AND SKILLS (PUT /api/students/{id})
    // ========================================================================
    /**
     * Q3.4: Update student scores and skills.
     * Returns HTTP 200 OK with updated record.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> updateStudent(@PathVariable("id") int id,
                                                              @RequestBody Student student) {
        try {
            Student updated = studentService.updateStudent(id, student);
            return ResponseEntity.ok(ApiResponse.success("Student updated successfully.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Update failed: " + e.getMessage()));
        }
    }

    // ========================================================================
    // 5. DELETE A STUDENT RECORD (DELETE /api/students/{id})
    // ========================================================================
    /**
     * Q3.5: Delete a student record.
     * Returns HTTP 200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable("id") int id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok(ApiResponse.success("Student with ID " + id + " deleted successfully.", null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete student: " + e.getMessage()));
        }
    }

    // ========================================================================
    // 6. DISPLAY THE MERIT LIST (GET /api/students/merit-list)
    // ========================================================================
    /**
     * Q3.6 & Q2.5: Display the merit list sorted by average score descending.
     * Uses Comparator<Student> in the service layer.
     */
    @GetMapping("/merit-list")
    public ResponseEntity<ApiResponse<List<Student>>> getMeritList() {
        ArrayList<Student> meritList = studentService.getMeritList();
        return ResponseEntity.ok(ApiResponse.success("Merit list generated successfully.", meritList));
    }

    // ========================================================================
    // 7. SEARCH BY ID, EMAIL OR COURSE (GET /api/students/search)
    // ========================================================================
    /**
     * Q2.4: Search for students by ID, email, or course name.
     * Supports queries like:
     * - /api/students/search?id=1
     * - /api/students/search?email=alice@kiet.edu
     * - /api/students/search?course=CSE
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Student>>> searchStudents(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "course", required = false) String course) {

        ArrayList<Student> results = studentService.searchStudents(id, email, course);
        return ResponseEntity.ok(ApiResponse.success("Found " + results.size() + " matching student(s).", results));
    }

    // ========================================================================
    // 8. IDENTIFY STUDENTS WITH SAME TECHNICAL SKILLS (GET /api/students/same-skills)
    // ========================================================================
    /**
     * Q2.6: Identify students who have the exact same technical skills.
     * Uses HashSet<String> for set equality.
     */
    @GetMapping("/same-skills")
    public ResponseEntity<ApiResponse<Map<String, List<Student>>>> getStudentsWithSameSkills() {
        Map<String, List<Student>> sameSkillsMap = studentService.getStudentsWithSameSkills();
        return ResponseEntity.ok(ApiResponse.success("Identified " + sameSkillsMap.size() + " group(s) with identical skills.", sameSkillsMap));
    }

    // ========================================================================
    // 9. NEEDS IMPROVEMENT IN PROGRAMMING (GET /api/students/needs-improvement)
    // ========================================================================
    /**
     * Q2.7: Display students who need improvement in programming (< 50)
     * but have a mathematics score of at least 75.
     */
    @GetMapping("/needs-improvement")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsNeedingImprovement() {
        ArrayList<Student> students = studentService.getStudentsNeedingImprovementInProgramming();
        return ResponseEntity.ok(ApiResponse.success("Found " + students.size() + " student(s) needing programming improvement with strong math.", students));
    }

    // ========================================================================
    // 10. RAW JDBC DEMONSTRATION ENDPOINT (GET /api/students/raw-jdbc/{id})
    // ========================================================================
    /**
     * Live viva endpoint demonstrating Raw JDBC (DriverManager, PreparedStatement, ResultSet)
     * executing alongside Spring JdbcTemplate.
     */
    @GetMapping("/raw-jdbc/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudentByIdRawJdbc(@PathVariable("id") int id) {
        Optional<Student> studentOpt = studentService.getStudentByIdRawJdbc(id);
        if (studentOpt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("[RAW JDBC] Record fetched successfully via DriverManager & PreparedStatement.", studentOpt.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("[RAW JDBC] Student with ID " + id + " not found."));
        }
    }
}
