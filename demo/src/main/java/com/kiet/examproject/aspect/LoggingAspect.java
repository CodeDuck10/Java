package com.kiet.examproject.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * ============================================================================
 * ASPECT-ORIENTED PROGRAMMING (AOP): LoggingAspect
 * ============================================================================
 * VIVA EXPLANATION - WHAT IS AOP?
 * Aspect-Oriented Programming (AOP) is a programming paradigm that complements
 * Object-Oriented Programming (OOP) by allowing the separation of cross-cutting
 * concerns (code that spans across multiple modules, such as logging, security,
 * transaction management, and performance monitoring) without altering the
 * core business logic.
 * 
 * VIVA CORE CONCEPTS IN AOP:
 * 1. ASPECT: A class containing cross-cutting logic (marked with @Aspect).
 * 2. JOIN POINT: A candidate point in program execution where advice can be applied.
 *    In Spring AOP, a join point is always the execution of a method.
 * 3. POINTCUT: A predicate or expression that matches specific join points.
 *    Defines "WHERE" the cross-cutting concern applies.
 * 4. ADVICE: The action taken at a join point. Defines "WHAT" to execute and "WHEN":
 *    - @Before: Runs before the target method executes.
 *    - @After: Runs after the target method finishes (regardless of outcome).
 *    - @AfterReturning: Runs only if the target method completes successfully.
 *    - @AfterThrowing: Runs if the target method throws an exception.
 *    - @Around: Wraps the method execution, allowing custom logic before and after.
 * 5. WEAVING: The process of applying aspects to target objects to create proxy objects.
 *    In Spring AOP, weaving occurs dynamically at runtime using CGLIB or JDK Dynamic Proxies.
 * ============================================================================
 */
@Aspect
@Component
public class LoggingAspect {

    /**
     * POINTCUT DEFINITIONS:
     * Pointcut targeting all methods within the controller package.
     */
    @Pointcut("execution(* com.kiet.examproject.controller..*(..))")
    public void controllerMethods() {}

    /**
     * Pointcut targeting all methods within the service package.
     */
    @Pointcut("execution(* com.kiet.examproject.service..*(..))")
    public void serviceMethods() {}

    /**
     * Combined Pointcut matching either controller OR service layer methods.
     */
    @Pointcut("controllerMethods() || serviceMethods()")
    public void appLayerMethods() {}

    /**
     * BEFORE ADVICE:
     * Intercepts execution right before the target method is called.
     * Logs class name, method name, and method arguments.
     */
    @Before("appLayerMethods()")
    public void logBeforeMethodExecution(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        System.out.println("[AOP @Before] >>> Entering: " + className + "." + methodName + "()");
        if (args != null && args.length > 0) {
            System.out.println("              Arguments: " + Arrays.toString(args));
        }
    }

    /**
     * AFTER ADVICE:
     * Intercepts execution right after the target method finishes (finally block behavior).
     * Logs method completion confirmation.
     */
    @After("appLayerMethods()")
    public void logAfterMethodExecution(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        System.out.println("[AOP @After]  <<< Completed: " + className + "." + methodName + "()");
    }
}
