package com.kiet.examproject.dao;

import com.kiet.examproject.model.Student;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================================
 * DATA ACCESS OBJECT (DAO): StudentDao
 * ============================================================================
 * Implements persistent database operations using Spring JdbcTemplate
 * AND provides a separate Raw JDBC demonstration method.
 * 
 * VIVA EXPLANATION - WHAT IS A DAO?
 * A DAO (Data Access Object) encapsulates all database interactions, isolating
 * SQL queries, connection logic, and table mapping from the rest of the application.
 * 
 * VIVA EXPLANATION - WHAT IS JdbcTemplate?
 * JdbcTemplate is Spring's core JDBC abstraction class. It simplifies database operations by:
 * 1. Handling connection acquisition and release automatically.
 * 2. Managing transactions and statement lifecycle.
 * 3. Translating vendor-specific SQLExceptions into Spring's unified DataAccessException hierarchy.
 * 4. Eliminating boilerplate try-catch-finally code.
 * 
 * VIVA EXPLANATION - WHAT IS A RowMapper?
 * RowMapper<T> is a callback interface used by JdbcTemplate to map each row of a
 * SQL ResultSet to a Java domain object (e.g. mapping column "student_name" -> student.setStudentName()).
 * ============================================================================
 */
@Repository
public class StudentDao {

    // Spring JdbcTemplate injected via Constructor Dependency Injection
    private final JdbcTemplate jdbcTemplate;

    // Database connection details injected from application.properties
    // used specifically for the Raw JDBC demonstration method
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String dbDriver;

    /**
     * CONSTRUCTOR INJECTION:
     * Recommended Spring DI pattern where dependencies are supplied through the constructor.
     * Ensures immutability, easier unit testing, and guaranteed bean readiness.
     */
    public StudentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * RowMapper implementation to map a MySQL row from 'students' table to a Student object.
     */
    private final RowMapper<Student> studentRowMapper = (rs, rowNum) -> {
        Student student = new Student();
        student.setStudentId(rs.getInt("student_id"));
        student.setStudentName(rs.getString("student_name"));
        student.setEmail(rs.getString("email"));
        student.setCourseName(rs.getString("course_name"));
        student.setProgrammingScore(rs.getDouble("programming_score"));
        student.setMathematicsScore(rs.getDouble("mathematics_score"));
        // Technical skills are loaded separately from the 'student_skills' child table
        return student;
    };

    // ========================================================================
    // 1. INSERT OPERATION (Create)
    // ========================================================================
    /**
     * Inserts a student and their technical skills into MySQL.
     * If an explicit studentId is provided (e.g., student roll number), it inserts it;
     * otherwise, MySQL auto-generates the primary key.
     */
    public int insert(Student student) {
        int generatedId;

        if (student.getStudentId() != null && student.getStudentId() > 0) {
            // Explicit ID provided: Insert with given ID
            String sql = "INSERT INTO students (student_id, student_name, email, course_name, " +
                         "programming_score, mathematics_score) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    student.getStudentId(),
                    student.getStudentName(),
                    student.getEmail(),
                    student.getCourseName(),
                    student.getProgrammingScore(),
                    student.getMathematicsScore()
            );
            generatedId = student.getStudentId();
        } else {
            // Auto-increment primary key ID
            String sql = "INSERT INTO students (student_name, email, course_name, " +
                         "programming_score, mathematics_score) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, student.getStudentName());
                ps.setString(2, student.getEmail());
                ps.setString(3, student.getCourseName());
                ps.setDouble(4, student.getProgrammingScore());
                ps.setDouble(5, student.getMathematicsScore());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            generatedId = (key != null) ? key.intValue() : 0;
            student.setStudentId(generatedId);
        }

        // Insert technical skills into student_skills table
        saveSkills(generatedId, student.getTechnicalSkills());
        return generatedId;
    }

    /**
     * Helper to insert skills into the student_skills table.
     */
    public void saveSkills(int studentId, List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO student_skills (student_id, skill_name) VALUES (?, ?)";
        for (String skill : skills) {
            if (skill != null && !skill.trim().isEmpty()) {
                jdbcTemplate.update(sql, studentId, skill.trim());
            }
        }
    }

    // ========================================================================
    // 2. SELECT ALL OPERATION (Read All)
    // ========================================================================
    /**
     * Retrieves all student records and populates their technical skills.
     */
    public List<Student> findAll() {
        String sql = "SELECT * FROM students ORDER BY student_id ASC";
        List<Student> students = jdbcTemplate.query(sql, studentRowMapper);

        if (students.isEmpty()) {
            return students;
        }

        // Batch load skills for all students efficiently using a Map
        Map<Integer, List<String>> skillsMap = loadAllSkillsMap();
        for (Student s : students) {
            s.setTechnicalSkills(skillsMap.getOrDefault(s.getStudentId(), new ArrayList<>()));
        }
        return students;
    }

    /**
     * Helper: loads all skills from student_skills into a Map<studentId, List<skill>>.
     */
    private Map<Integer, List<String>> loadAllSkillsMap() {
        String sql = "SELECT student_id, skill_name FROM student_skills ORDER BY id ASC";
        Map<Integer, List<String>> map = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            int studentId = rs.getInt("student_id");
            String skill = rs.getString("skill_name");
            map.computeIfAbsent(studentId, k -> new ArrayList<>()).add(skill);
        });

        return map;
    }

    // ========================================================================
    // 3. SELECT BY ID OPERATION (Read One)
    // ========================================================================
    /**
     * Retrieves a single student by primary key ID.
     */
    public Optional<Student> findById(int id) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try {
            Student student = jdbcTemplate.queryForObject(sql, studentRowMapper, id);
            if (student != null) {
                student.setTechnicalSkills(findSkillsByStudentId(id));
            }
            return Optional.ofNullable(student);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Helper: Retrieves skills for a single student ID.
     */
    public List<String> findSkillsByStudentId(int studentId) {
        String sql = "SELECT skill_name FROM student_skills WHERE student_id = ? ORDER BY id ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("skill_name"), studentId);
    }

    // ========================================================================
    // 4. SEARCH BY EMAIL AND COURSE
    // ========================================================================
    /**
     * Retrieves a student by their unique email address.
     */
    public Optional<Student> findByEmail(String email) {
        String sql = "SELECT * FROM students WHERE LOWER(email) = LOWER(?)";
        try {
            Student student = jdbcTemplate.queryForObject(sql, studentRowMapper, email.trim());
            if (student != null) {
                student.setTechnicalSkills(findSkillsByStudentId(student.getStudentId()));
            }
            return Optional.ofNullable(student);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves students matching a course name (case-insensitive substring match).
     */
    public List<Student> findByCourse(String courseName) {
        String sql = "SELECT * FROM students WHERE LOWER(course_name) LIKE LOWER(?) ORDER BY student_id ASC";
        List<Student> students = jdbcTemplate.query(sql, studentRowMapper, "%" + courseName.trim() + "%");
        Map<Integer, List<String>> skillsMap = loadAllSkillsMap();
        for (Student s : students) {
            s.setTechnicalSkills(skillsMap.getOrDefault(s.getStudentId(), new ArrayList<>()));
        }
        return students;
    }

    // ========================================================================
    // 5. UPDATE OPERATION (Update)
    // ========================================================================
    /**
     * Updates student details, scores, and replaces skills in MySQL.
     */
    public int update(Student student) {
        String sql = "UPDATE students SET student_name = ?, email = ?, course_name = ?, " +
                     "programming_score = ?, mathematics_score = ? WHERE student_id = ?";
        int rows = jdbcTemplate.update(sql,
                student.getStudentName(),
                student.getEmail(),
                student.getCourseName(),
                student.getProgrammingScore(),
                student.getMathematicsScore(),
                student.getStudentId()
        );

        if (rows > 0) {
            // Delete old skills and insert updated skills
            jdbcTemplate.update("DELETE FROM student_skills WHERE student_id = ?", student.getStudentId());
            saveSkills(student.getStudentId(), student.getTechnicalSkills());
        }
        return rows;
    }

    // ========================================================================
    // 6. DELETE OPERATION (Delete)
    // ========================================================================
    /**
     * Deletes a student by ID. Foreign key ON DELETE CASCADE will delete associated skills.
     */
    public int deleteById(int id) {
        // Explicitly clean up child table as defensive programming
        jdbcTemplate.update("DELETE FROM student_skills WHERE student_id = ?", id);
        return jdbcTemplate.update("DELETE FROM students WHERE student_id = ?", id);
    }

    public boolean existsById(int id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM students WHERE student_id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM students WHERE LOWER(email) = LOWER(?)", Integer.class, email.trim());
        return count != null && count > 0;
    }

    // ========================================================================
    // 7. RAW JDBC DEMONSTRATION (MANDATORY SYLLABUS REQUIREMENT)
    // ========================================================================
    /**
     * Demonstrates traditional Raw JDBC operations using:
     * - Class.forName("com.mysql.cj.jdbc.Driver")
     * - DriverManager.getConnection(...)
     * - PreparedStatement
     * - ResultSet
     * - Try-with-resources for automatic closing
     * 
     * VIVA COMPARISON:
     * Raw JDBC requires explicit driver loading, connection creation, SQL statement
     * preparation, parameter binding, manual ResultSet traversal, and explicit resource
     * management (closing statements and connections in finally blocks).
     * 
     * In contrast, Spring's JdbcTemplate automates all resource acquisition, exception
     * translation, and connection pooling, dramatically reducing boilerplate code.
     */
    public Optional<Student> findByIdRawJdbc(int id) {
        System.out.println("\n[RAW JDBC DEMO] Executing Raw JDBC findById for studentId = " + id);

        String sql = "SELECT * FROM students WHERE student_id = ?";
        String skillsSql = "SELECT skill_name FROM student_skills WHERE student_id = ?";

        // Step 1: Explicitly load the MySQL JDBC Driver class
        try {
            Class.forName(dbDriver != null ? dbDriver : "com.mysql.cj.jdbc.Driver");
            System.out.println("[RAW JDBC DEMO] 1. MySQL Driver Class loaded successfully via Class.forName()");
        } catch (ClassNotFoundException e) {
            System.err.println("[RAW JDBC DEMO] Driver Class not found: " + e.getMessage());
            return Optional.empty();
        }

        // Step 2, 3, 4: Establish Connection and PreparedStatement using try-with-resources
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            System.out.println("[RAW JDBC DEMO] 2. Database Connection established via DriverManager.getConnection()");

            // Step 5: Parameter binding (1-based index)
            ps.setInt(1, id);
            System.out.println("[RAW JDBC DEMO] 3. PreparedStatement bound with parameter: id = " + id);

            // Step 6: Execute query and process ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getInt("student_id"));
                    student.setStudentName(rs.getString("student_name"));
                    student.setEmail(rs.getString("email"));
                    student.setCourseName(rs.getString("course_name"));
                    student.setProgrammingScore(rs.getDouble("programming_score"));
                    student.setMathematicsScore(rs.getDouble("mathematics_score"));

                    // Fetch skills using a second Raw JDBC PreparedStatement
                    List<String> skills = new ArrayList<>();
                    try (PreparedStatement psSkills = connection.prepareStatement(skillsSql)) {
                        psSkills.setInt(1, id);
                        try (ResultSet rsSkills = psSkills.executeQuery()) {
                            while (rsSkills.next()) {
                                skills.add(rsSkills.getString("skill_name"));
                            }
                        }
                    }
                    student.setTechnicalSkills(skills);

                    System.out.println("[RAW JDBC DEMO] 4. Record mapped from ResultSet successfully: " + student.getStudentName());
                    return Optional.of(student);
                } else {
                    System.out.println("[RAW JDBC DEMO] 4. No record found for studentId = " + id);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            System.err.println("[RAW JDBC DEMO] SQLException occurred: " + e.getMessage());
            return Optional.empty();
        }
        // Note: Connection, PreparedStatement, and ResultSet are automatically closed
        // by the try-with-resources block!
    }
}
