-- ==============================================================================
-- DATABASE SCHEMA: schema.sql
-- Topic: Student Profile and Skill Development Management System
-- Course: CS336B - Object Oriented Programming Using Java (MSE-II)
-- Database: mse_java_exam_db
-- ==============================================================================

-- 1. Main Student Entity Table
CREATE TABLE IF NOT EXISTS students (
    student_id INT NOT NULL AUTO_INCREMENT,
    student_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    programming_score DOUBLE NOT NULL,
    mathematics_score DOUBLE NOT NULL,
    PRIMARY KEY (student_id)
);

-- 2. Technical Skills Child Table (One-to-Many relationship with cascading delete)
CREATE TABLE IF NOT EXISTS student_skills (
    id INT NOT NULL AUTO_INCREMENT,
    student_id INT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
