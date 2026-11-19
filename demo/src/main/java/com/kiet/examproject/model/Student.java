package com.kiet.examproject.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * ============================================================================
 * ENTITY MODEL: Student
 * ============================================================================
 * Question Paper Requirement (Q1 & Q2):
 * Represents a student's personal information, academic scores, and technical skills.
 * 
 * VIVA EXPLANATION - WHAT IS A MODEL?
 * In MVC (Model-View-Controller) architecture, the Model represents the core business
 * data and state of the application. It encapsulates real-world entities with private
 * properties and exposes public methods, adhering to OOP principles of Encapsulation.
 * ============================================================================
 */
public class Student {

    // 1. Primary Key: Unique Student Identifier
    private Integer studentId;

    // 2. Personal Information
    private String studentName;
    private String email;
    private String courseName;

    // 3. Academic Scores (Validated between 0.0 and 100.0)
    private Double programmingScore;
    private Double mathematicsScore;

    // 4. List of Technical Skills (Demonstrating List collection)
    private List<String> technicalSkills = new ArrayList<>();

    /**
     * Default No-Argument Constructor:
     * Required by Spring MVC and Jackson JSON Serializer/Deserializer
     * to instantiate objects via reflection when handling HTTP requests.
     */
    public Student() {
    }

    /**
     * Parameterized Constructor:
     * Allows convenient instantiation with all mandatory attributes.
     */
    public Student(Integer studentId, String studentName, String email, String courseName,
                   Double programmingScore, Double mathematicsScore, List<String> technicalSkills) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.courseName = courseName;
        this.programmingScore = programmingScore;
        this.mathematicsScore = mathematicsScore;
        if (technicalSkills != null) {
            this.technicalSkills = new ArrayList<>(technicalSkills);
        }
    }

    // ========================================================================
    // GETTERS AND SETTERS (Encapsulation)
    // ========================================================================

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Double getProgrammingScore() {
        return programmingScore;
    }

    public void setProgrammingScore(Double programmingScore) {
        this.programmingScore = programmingScore;
    }

    public Double getMathematicsScore() {
        return mathematicsScore;
    }

    public void setMathematicsScore(Double mathematicsScore) {
        this.mathematicsScore = mathematicsScore;
    }

    public List<String> getTechnicalSkills() {
        return technicalSkills;
    }

    public void setTechnicalSkills(List<String> technicalSkills) {
        this.technicalSkills = (technicalSkills != null) ? new ArrayList<>(technicalSkills) : new ArrayList<>();
    }

    // ========================================================================
    // BUSINESS LOGIC / CALCULATED PROPERTIES (Q2 REQUIREMENTS)
    // ========================================================================

    /**
     * Q2.1: Calculate each student's average score.
     * Formula: (programmingScore + mathematicsScore) / 2.0
     * Rounded to two decimal places for precision.
     */
    public double getAverageScore() {
        double prog = (programmingScore != null) ? programmingScore : 0.0;
        double math = (mathematicsScore != null) ? mathematicsScore : 0.0;
        double avg = (prog + math) / 2.0;
        return Math.round(avg * 100.0) / 100.0;
    }

    /**
     * Q2.2: Assign a performance category based on the average:
     * - 85 or above: Excellent
     * - 70 to 84.99: Good
     * - 50 to 69.99: Satisfactory
     * - Below 50:    Needs Improvement
     */
    public String getPerformanceCategory() {
        double avg = getAverageScore();
        if (avg >= 85.0) {
            return "Excellent";
        } else if (avg >= 70.0) {
            return "Good";
        } else if (avg >= 50.0) {
            return "Satisfactory";
        } else {
            return "Needs Improvement";
        }
    }

    /**
     * Q2.3: Identify the stronger subject for each student.
     * Handles cases where scores are equal.
     */
    public String getStrongerSubject() {
        double prog = (programmingScore != null) ? programmingScore : 0.0;
        double math = (mathematicsScore != null) ? mathematicsScore : 0.0;

        if (prog > math) {
            return "Programming";
        } else if (math > prog) {
            return "Mathematics";
        } else {
            return "Equal";
        }
    }

    /**
     * Q2.7: Check if student needs improvement in programming but has strong mathematics.
     * Condition: programmingScore < 50.0 AND mathematicsScore >= 75.0
     */
    public boolean isProgrammingNeedsImprovementMathStrong() {
        double prog = (programmingScore != null) ? programmingScore : 0.0;
        double math = (mathematicsScore != null) ? mathematicsScore : 0.0;
        return prog < 50.0 && math >= 75.0;
    }

    /**
     * Q2.6 Helper: Returns a normalized Set of technical skills.
     * All skill strings are trimmed and lowercased to allow case-insensitive,
     * order-independent Set equality comparison: setA.equals(setB).
     */
    public Set<String> getNormalizedSkillSet() {
        Set<String> set = new HashSet<>();
        if (technicalSkills != null) {
            for (String s : technicalSkills) {
                if (s != null && !s.trim().isEmpty()) {
                    set.add(s.trim().toLowerCase());
                }
            }
        }
        return set;
    }

    // ========================================================================
    // STANDARD OBJECT METHODS (toString, equals, hashCode)
    // ========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId) ||
               Objects.equals(email, student.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, email);
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", email='" + email + '\'' +
                ", courseName='" + courseName + '\'' +
                ", programmingScore=" + programmingScore +
                ", mathematicsScore=" + mathematicsScore +
                ", averageScore=" + getAverageScore() +
                ", category=" + getPerformanceCategory() +
                ", strongerSubject=" + getStrongerSubject() +
                ", technicalSkills=" + technicalSkills +
                '}';
    }
}
