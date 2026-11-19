package com.kiet.examproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================================
 * SPRING CONFIGURATION CLASS: AppConfig
 * ============================================================================
 * VIVA EXPLANATION - WHAT IS @Configuration?
 * Indicates that this class declares one or more @Bean methods and may be
 * processed by the Spring IoC container to generate bean definitions and
 * service requests for those beans at runtime.
 * 
 * VIVA EXPLANATION - SPRING BEAN SCOPES (SYLLABUS REQUIREMENT):
 * 1. SINGLETON (Default Scope):
 *    Only ONE instance of the bean is created per Spring IoC Container.
 *    Any injection or getBean() call returns the exact same shared instance.
 *    Ideal for stateless services, DAOs, and controllers.
 * 
 * 2. PROTOTYPE:
 *    A NEW instance of the bean is created every time it is requested from
 *    the Spring container (via getBean() or ObjectProvider.getObject()).
 *    Ideal for stateful objects or temporary processing helpers.
 * 
 * 3. REQUEST (Web-aware only):
 *    Creates a single instance per HTTP request lifecycle. Destroyed when request ends.
 * 
 * 4. SESSION (Web-aware only):
 *    Creates a single instance per HTTP Session lifecycle. Ideal for user login states.
 * 
 * 5. APPLICATION (Web-aware only):
 *    Creates a single instance per ServletContext. Shared across all web requests.
 * 
 * 6. WEBSOCKET (Web-aware only):
 *    Creates a single instance per WebSocket lifecycle.
 * ============================================================================
 */
@Configuration
public class AppConfig {

    /**
     * Helper Class defined as a Prototype Bean.
     * Every time this bean is requested from the Spring Container,
     * a fresh instance with a unique instance ID and creation timestamp is instantiated.
     */
    public static class StudentProcessingHelper {
        private final String instanceId;
        private final String createdAt;

        public StudentProcessingHelper() {
            this.instanceId = "HELPER-" + System.identityHashCode(this);
            this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            System.out.println("[Spring Container] >> New Prototype Bean Created: " + instanceId + " at " + createdAt);
        }

        public String getInstanceId() {
            return instanceId;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String formatReportHeader(String reportTitle) {
            return "=== [PROTOTYPE BEAN " + instanceId + "] " + reportTitle + " (Generated: " + createdAt + ") ===";
        }
    }

    /**
     * Declares a Prototype-scoped Spring Bean.
     * VIVA NOTE: @Scope("prototype") overrides the default Singleton scope!
     */
    @Bean
    @Scope("prototype")
    public StudentProcessingHelper studentProcessingHelper() {
        return new StudentProcessingHelper();
    }
}
