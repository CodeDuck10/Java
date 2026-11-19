package com.kiet.examproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * EXAM PROJECT ENTRY POINT: ExamprojectApplication
 * ============================================================================
 * Problem Statement: Student Profile and Skill Development Management System
 * Course: CS336B - Object Oriented Programming Using Java (Mid Semester Exam-II)
 * 
 * VIVA EXPLANATION - @SpringBootApplication:
 * This is a meta-annotation that combines three crucial Spring annotations:
 * 1. @Configuration: Tags the class as a source of bean definitions for the
 *    ApplicationContext (Spring's Inversion of Control container).
 * 2. @EnableAutoConfiguration: Directs Spring Boot to automatically configure
 *    beans (DataSource, JdbcTemplate, DispatcherServlet, Jackson, etc.) based
 *    on dependencies present in pom.xml.
 * 3. @ComponentScan: Automatically scans the current package ('com.kiet.examproject')
 *    and all sub-packages (model, dao, service, controller, config, aspect) to
 *    discover and register Spring-managed components (@Component, @Service, @Repository, @RestController).
 * ============================================================================
 */
@SpringBootApplication
public class ExamprojectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamprojectApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("   STUDENT PROFILE & SKILL DEVELOPMENT MANAGEMENT SYSTEM STARTED   ");
        System.out.println("==================================================================");
        System.out.println(" >> Web Dashboard:       http://localhost:8080/");
        System.out.println(" >> REST API Base:       http://localhost:8080/api/students");
        System.out.println(" >> Merit List API:      http://localhost:8080/api/students/merit-list");
        System.out.println(" >> Same Skills API:     http://localhost:8080/api/students/same-skills");
        System.out.println(" >> Needs Imprv API:     http://localhost:8080/api/students/needs-improvement");
        System.out.println("==================================================================");
    }
}
