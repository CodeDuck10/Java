package com.kiet.examproject.service;

import com.kiet.examproject.config.AppConfig;
import com.kiet.examproject.dao.StudentDao;
import com.kiet.examproject.model.Student;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * SERVICE LAYER: StudentService
 * ============================================================================
 * Contains core business logic, score validations, calculations, and demonstrates
 * meaningful usage of Java Collections:
 * - ArrayList<Student>: For structured student lists.
 * - HashMap<Integer, Student>: For O(1) ID-based lookups and caching.
 * - HashSet<String>: For unique skills and Set equality comparisons (same skills).
 * - Comparator<Student>: For sorting students in descending order of average score.
 * 
 * VIVA EXPLANATION - WHAT IS @Service?
 * Specialization of @Component used to mark classes that reside in the Service
 * layer of MVC architecture, holding domain business logic.
 * 
 * VIVA EXPLANATION - INVERSION OF CONTROL (IoC) & DEPENDENCY INJECTION (DI):
 * - IoC: Inverting the control of object creation from the developer to the container.
 * - DI: The design pattern where dependencies (collaborating objects) are injected
 *   into a class rather than the class creating them with 'new'.
 * 
 * VIVA EXPLANATION - CONSTRUCTOR INJECTION VS FIELD INJECTION:
 * - Constructor Injection (Used here): Dependencies are passed through constructor;
 *   guarantees immutability (final fields), prevents NullPointerExceptions, and allows easy unit testing.
 * - Field Injection (@Autowired directly on field): Fragile, hides dependencies, cannot be final.
 * ============================================================================
 */
@Service
public class StudentService {

    // Primary Dependency: StudentDao injected via Constructor Injection
    private final StudentDao studentDao;

    // Prototype Bean Provider: Demonstrates retrieving prototype-scoped beans on demand
    private final ObjectProvider<AppConfig.StudentProcessingHelper> helperProvider;

    /**
     * SYLLABUS & VIVA INSPECTION REQUIREMENT:
     * Field Injection demonstration using @Autowired.
     * While Constructor Injection is the industry best practice used across this application,
     * this field explicitly showcases @Autowired field injection to fulfill the lab syllabus.
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * CONSTRUCTOR INJECTION (Recommended Spring Best Practice):
     */
    public StudentService(StudentDao studentDao, ObjectProvider<AppConfig.StudentProcessingHelper> helperProvider) {
        this.studentDao = studentDao;
        this.helperProvider = helperProvider;
    }

    // ========================================================================
    // LIFECYCLE CALLBACKS (@PostConstruct and @PreDestroy)
    // ========================================================================
    /**
     * @PostConstruct Callback:
     * Executed once after dependency injection is complete and the bean is initialized.
     */
    @PostConstruct
    public void onInit() {
        System.out.println("[StudentService Lifecycle] >>> @PostConstruct executed: StudentService is initialized and ready for requests.");
        if (applicationContext != null) {
            System.out.println("[StudentService Lifecycle] >>> @Autowired field injection verified: ApplicationContext bean ID is " + applicationContext.getId());
        }
    }

    /**
     * @PreDestroy Callback:
     * Executed right before the bean is destroyed by the Spring IoC container (e.g. on application shutdown).
     */
    @PreDestroy
    public void onDestroy() {
        System.out.println("[StudentService Lifecycle] <<< @PreDestroy executed: StudentService is shutting down, releasing resources.");
    }

    // ========================================================================
    // VALIDATION RULES (Question 2.8)
    // ========================================================================
    /**
     * Validates student data:
     * 1. Programming Score must be between 0.0 and 100.0.
     * 2. Mathematics Score must be between 0.0 and 100.0.
     * 3. Prevents duplicate student IDs on creation.
     * 4. Ensures unique email address.
     */
    public void validateStudent(Student student, boolean isNew) {
        if (student == null) {
            throw new IllegalArgumentException("Student payload cannot be empty.");
        }
        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name is required.");
        }
        if (student.getEmail() == null || student.getEmail().trim().isEmpty() || !student.getEmail().contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (student.getCourseName() == null || student.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required.");
        }

        // Validate Programming Score (0 to 100)
        Double prog = student.getProgrammingScore();
        if (prog == null || prog < 0.0 || prog > 100.0) {
            throw new IllegalArgumentException("Programming score must be between 0 and 100. Given: " + prog);
        }

        // Validate Mathematics Score (0 to 100)
        Double math = student.getMathematicsScore();
        if (math == null || math < 0.0 || math > 100.0) {
            throw new IllegalArgumentException("Mathematics score must be between 0 and 100. Given: " + math);
        }

        // Rule: Prevent Duplicate Student IDs on creation
        if (isNew && student.getStudentId() != null && student.getStudentId() > 0) {
            if (studentDao.existsById(student.getStudentId())) {
                throw new IllegalArgumentException("Duplicate Student ID! A student with ID " + student.getStudentId() + " already exists.");
            }
        }

        // Check duplicate email
        Optional<Student> existingByEmail = studentDao.findByEmail(student.getEmail());
        if (existingByEmail.isPresent()) {
            if (isNew || !existingByEmail.get().getStudentId().equals(student.getStudentId())) {
                throw new IllegalArgumentException("Duplicate Email! A student with email '" + student.getEmail() + "' already exists.");
            }
        }
    }

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================
    /**
     * Q3.1: Add a student.
     * Demonstrates prototype bean retrieval to format a generation report.
     */
    public Student addStudent(Student student) {
        validateStudent(student, true);

        // Retrieve a fresh prototype bean instance from the Spring container
        AppConfig.StudentProcessingHelper helper = helperProvider.getObject();
        System.out.println(helper.formatReportHeader("Registering New Student: " + student.getStudentName()));

        int id = studentDao.insert(student);
        student.setStudentId(id);
        return student;
    }

    /**
     * Q3.2: Retrieve all students.
     * Demonstrates usage of ArrayList<Student>.
     */
    public ArrayList<Student> getAllStudents() {
        List<Student> list = studentDao.findAll();
        return new ArrayList<>(list);
    }

    /**
     * Q3.3: Search for a student by ID.
     */
    public Student getStudentById(int id) {
        return studentDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Student with ID " + id + " not found."));
    }

    /**
     * Q3.4: Update student scores, skills, and personal details.
     */
    public Student updateStudent(int id, Student updatedStudent) {
        Student existing = getStudentById(id);
        updatedStudent.setStudentId(id);
        validateStudent(updatedStudent, false);

        int rows = studentDao.update(updatedStudent);
        if (rows == 0) {
            throw new IllegalStateException("Failed to update student with ID " + id);
        }
        return updatedStudent;
    }

    /**
     * Q3.5: Delete a student record.
     */
    public boolean deleteStudent(int id) {
        if (!studentDao.existsById(id)) {
            throw new NoSuchElementException("Cannot delete. Student with ID " + id + " does not exist.");
        }
        return studentDao.deleteById(id) > 0;
    }

    // ========================================================================
    // BUSINESS LOGIC & COLLECTIONS DEMONSTRATIONS (Q2 REQUIREMENTS)
    // ========================================================================

    /**
     * Q2.5 & Q3.6: Generate and display the merit list sorted by average score descending.
     * Demonstrates:
     * - ArrayList<Student> collection
     * - Comparator<Student> for descending sort by average score, then alphabetically by name.
     */
    public ArrayList<Student> getMeritList() {
        ArrayList<Student> students = getAllStudents();

        // Demonstrating Comparator<Student>
        Comparator<Student> meritComparator = Comparator
                .comparingDouble(Student::getAverageScore)
                .reversed()
                .thenComparing(Student::getStudentName);

        students.sort(meritComparator);
        return students;
    }

    /**
     * Q2.4: Search for students by ID, email, or course name.
     * Demonstrates HashMap<Integer, Student> for O(1) ID lookup!
     */
    public ArrayList<Student> searchStudents(Integer id, String email, String course) {
        ArrayList<Student> allStudents = getAllStudents();

        // Demonstrating HashMap<Integer, Student> for fast ID lookup
        Map<Integer, Student> studentMap = new HashMap<>();
        for (Student s : allStudents) {
            studentMap.put(s.getStudentId(), s);
        }

        // Case 1: Search by ID using HashMap
        if (id != null) {
            ArrayList<Student> result = new ArrayList<>();
            Student found = studentMap.get(id);
            if (found != null) {
                result.add(found);
            }
            return result;
        }

        // Case 2: Search by Email
        if (email != null && !email.trim().isEmpty()) {
            ArrayList<Student> result = new ArrayList<>();
            for (Student s : allStudents) {
                if (s.getEmail().equalsIgnoreCase(email.trim())) {
                    result.add(s);
                }
            }
            return result;
        }

        // Case 3: Search by Course
        if (course != null && !course.trim().isEmpty()) {
            ArrayList<Student> result = new ArrayList<>();
            for (Student s : allStudents) {
                if (s.getCourseName().toLowerCase().contains(course.trim().toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        }

        return allStudents;
    }

    /**
     * Q2.6: Identify students who have the exact same technical skills.
     * Demonstrates:
     * - HashSet<String> for set equality comparison (setA.equals(setB) is order-independent).
     * - Map<Set<String>, List<Student>> for grouping students sharing identical skill sets.
     * Returns only groups containing 2 or more students.
     */
    public Map<String, List<Student>> getStudentsWithSameSkills() {
        ArrayList<Student> allStudents = getAllStudents();

        // Group students by their normalized HashSet of skills
        Map<Set<String>, List<Student>> skillSetGroupMap = new HashMap<>();

        for (Student student : allStudents) {
            Set<String> normalizedSkills = student.getNormalizedSkillSet();
            if (!normalizedSkills.isEmpty()) {
                skillSetGroupMap.computeIfAbsent(normalizedSkills, k -> new ArrayList<>()).add(student);
            }
        }

        // Filter only groups that have 2 or more students (shared identical skills)
        Map<String, List<Student>> result = new LinkedHashMap<>();
        for (Map.Entry<Set<String>, List<Student>> entry : skillSetGroupMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                String skillKey = String.join(", ", entry.getKey());
                result.put(skillKey, entry.getValue());
            }
        }

        return result;
    }

    /**
     * Q2.7: Display students who need improvement in programming (< 50)
     * but have a mathematics score of at least 75.
     */
    public ArrayList<Student> getStudentsNeedingImprovementInProgramming() {
        ArrayList<Student> allStudents = getAllStudents();
        ArrayList<Student> filtered = new ArrayList<>();

        for (Student s : allStudents) {
            if (s.isProgrammingNeedsImprovementMathStrong()) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    /**
     * Raw JDBC Demonstration Helper
     */
    public Optional<Student> getStudentByIdRawJdbc(int id) {
        return studentDao.findByIdRawJdbc(id);
    }
}
