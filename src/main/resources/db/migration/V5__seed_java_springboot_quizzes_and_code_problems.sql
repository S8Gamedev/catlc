-- V5__seed_java_springboot_quizzes_and_code_problems.sql

BEGIN;

-- 1) Ensure skills exist
INSERT INTO skills (name, category, description, difficulty_level, created_at, created_by_user_id)
SELECT 'Java', 'Programming', 'Core Java programming language fundamentals and concepts', 'INTERMEDIATE', NOW(), 1
    WHERE NOT EXISTS (
    SELECT 1 FROM skills WHERE name = 'Java'
);

INSERT INTO skills (name, category, description, difficulty_level, created_at, created_by_user_id)
SELECT 'Spring Boot', 'Framework', 'Spring Boot application development and REST API fundamentals', 'INTERMEDIATE', NOW(), 1
    WHERE NOT EXISTS (
    SELECT 1 FROM skills WHERE name = 'Spring Boot'
);

-- 2) Associate skills to users 1, 2, 3 through user_skill_progress
INSERT INTO user_skill_progress (
    user_id, skill_id, mastery, learned_percent, confidence_level,
    times_practiced, times_quizzed, times_learned_new, retention_score,
    status, created_at, updated_at
)
SELECT u.user_id, s.id, 0, 0, 0, 0, 0, 0, 100, 'ACTIVE', NOW(), NOW()
FROM (VALUES (1), (2), (3)) AS u(user_id)
         CROSS JOIN skills s
WHERE s.name IN ('Java', 'Spring Boot')
  AND NOT EXISTS (
    SELECT 1
    FROM user_skill_progress usp
    WHERE usp.user_id = u.user_id
      AND usp.skill_id = s.id
);

-- 3) Insert 15 Java MCQ questions
INSERT INTO questions (
    skill_id, type, difficulty, prompt,
    option_a, option_b, option_c, option_d,
    correct_option_index, explanation
)
SELECT s.id, 'MCQ', q.difficulty, q.prompt,
       q.option_a, q.option_b, q.option_c, q.option_d,
       q.correct_option_index, q.explanation
FROM skills s
         JOIN (
    VALUES
        ('EASY', 'Which keyword is used to inherit a class in Java?', 'this', 'super', 'extends', 'implements', 2, 'In Java, a class inherits another class using the extends keyword.'),
        ('EASY', 'Which method is the entry point of a Java application?', 'start()', 'run()', 'main()', 'init()', 2, 'The JVM starts execution from public static void main(String[] args).'),
        ('EASY', 'Which primitive type is used for true/false values?', 'int', 'boolean', 'char', 'String', 1, 'boolean stores true or false values.'),
        ('MEDIUM', 'What is the size of int in Java?', '16 bits', '32 bits', '64 bits', 'Depends on OS', 1, 'Java int is always 32-bit regardless of platform.'),
        ('MEDIUM', 'Which collection does not allow duplicate elements?', 'List', 'Queue', 'Set', 'ArrayList', 2, 'A Set stores unique elements only.'),
        ('MEDIUM', 'What does JVM stand for?', 'Java Variable Method', 'Java Virtual Machine', 'Joint Vector Model', 'Java Verified Module', 1, 'JVM stands for Java Virtual Machine.'),
        ('EASY', 'Which operator is used to compare two values for equality in Java primitives?', '=', '==', '!=', 'equals', 1, 'For primitives, == compares values.'),
        ('MEDIUM', 'Which exception is checked at compile time?', 'NullPointerException', 'ArithmeticException', 'IOException', 'ArrayIndexOutOfBoundsException', 2, 'IOException is a checked exception.'),
        ('MEDIUM', 'Which access modifier makes a member visible only within the same class?', 'public', 'protected', 'default', 'private', 3, 'private restricts visibility to the same class only.'),
        ('HARD', 'Which feature allows one interface reference to point to different class objects?', 'Encapsulation', 'Polymorphism', 'Abstraction', 'Compilation', 1, 'Polymorphism allows one interface to represent many forms.'),
        ('EASY', 'Which package is automatically imported in every Java program?', 'java.io', 'java.lang', 'java.util', 'java.net', 1, 'java.lang is imported by default.'),
        ('MEDIUM', 'Which loop is guaranteed to execute at least once?', 'for', 'while', 'do-while', 'enhanced for', 2, 'do-while checks the condition after the first iteration.'),
        ('MEDIUM', 'Which keyword prevents method overriding?', 'final', 'static', 'const', 'sealed', 0, 'A final method cannot be overridden.'),
        ('HARD', 'Which memory area stores objects created with new?', 'Stack', 'Heap', 'Code cache', 'Register', 1, 'Objects are allocated on the heap.'),
        ('EASY', 'Which class is used to create immutable strings?', 'StringBuilder', 'StringBuffer', 'String', 'CharArray', 2, 'String objects are immutable in Java.')
) AS q(difficulty, prompt, option_a, option_b, option_c, option_d, correct_option_index, explanation)
              ON TRUE
WHERE s.name = 'Java';

-- 4) Insert 15 Spring Boot MCQ questions
INSERT INTO questions (
    skill_id, type, difficulty, prompt,
    option_a, option_b, option_c, option_d,
    correct_option_index, explanation
)
SELECT s.id, 'MCQ', q.difficulty, q.prompt,
       q.option_a, q.option_b, q.option_c, q.option_d,
       q.correct_option_index, q.explanation
FROM skills s
         JOIN (
    VALUES
        ('EASY', 'Which annotation starts a Spring Boot application?', '@EnableWeb', '@SpringBootApplication', '@SpringStart', '@BootApp', 1, '@SpringBootApplication is the primary annotation used on the main class.'),
        ('EASY', 'Which file is commonly used for Spring Boot configuration?', 'pom.xml', 'application.properties', 'index.html', 'build.gradle', 1, 'application.properties is a standard configuration file.'),
        ('EASY', 'Which embedded server is default in Spring Boot starter web?', 'Jetty', 'Undertow', 'Tomcat', 'GlassFish', 2, 'Spring Boot starter web uses embedded Tomcat by default.'),
        ('MEDIUM', 'Which annotation is used to create a REST controller?', '@Controller', '@Service', '@Repository', '@RestController', 3, '@RestController combines @Controller and @ResponseBody behavior.'),
        ('MEDIUM', 'What does dependency injection help with?', 'Increasing compile time', 'Tight coupling', 'Loose coupling', 'Disabling beans', 2, 'Dependency injection promotes loose coupling.'),
        ('MEDIUM', 'Which annotation maps HTTP GET requests?', '@PostMapping', '@GetMapping', '@RequestBody', '@Bean', 1, '@GetMapping handles HTTP GET requests.'),
        ('EASY', 'Which annotation injects a Spring-managed bean?', '@Value', '@Autowired', '@Qualifier', '@ComponentScan', 1, '@Autowired injects dependencies managed by Spring.'),
        ('MEDIUM', 'Which starter is used for JPA support?', 'spring-boot-starter-test', 'spring-boot-starter-data-jpa', 'spring-boot-starter-actuator', 'spring-boot-starter-validation', 1, 'spring-boot-starter-data-jpa provides JPA and Hibernate support.'),
        ('MEDIUM', 'Which annotation marks a service class?', '@Repository', '@Entity', '@Service', '@Configuration', 2, '@Service marks service-layer components.'),
        ('HARD', 'What is Actuator mainly used for?', 'Database schema generation', 'Monitoring and management endpoints', 'Client-side rendering', 'Java compilation', 1, 'Spring Boot Actuator provides monitoring and operational endpoints.'),
        ('EASY', 'Which annotation binds JSON request data to a method parameter?', '@RequestBody', '@PathVariable', '@RequestParam', '@ResponseBody', 0, '@RequestBody maps the HTTP request body to an object.'),
        ('MEDIUM', 'Which annotation is used to define URL path variables?', '@PathVariable', '@ModelAttribute', '@Qualifier', '@Primary', 0, '@PathVariable binds URI template variables.'),
        ('MEDIUM', 'Which layer usually contains business logic?', 'Controller', 'Service', 'Entity', 'application.properties', 1, 'Business logic is typically placed in the service layer.'),
        ('HARD', 'Which annotation customizes bean creation in a config class?', '@Bean', '@Entity', '@Id', '@Table', 0, '@Bean defines a bean inside a configuration class.'),
        ('EASY', 'What does Spring Initializr mainly help generate?', 'Database records', 'Starter project structure', 'Browser cache', 'Docker image layers', 1, 'Spring Initializr generates a ready-to-use project skeleton.')
) AS q(difficulty, prompt, option_a, option_b, option_c, option_d, correct_option_index, explanation)
              ON TRUE
WHERE s.name = 'Spring Boot';

-- 5) Create 3 Java quizzes for user 1 and map 5 unique Java questions each
WITH java_skill AS (
    SELECT id AS skill_id FROM skills WHERE name = 'Java'
),
     java_questions AS (
         SELECT q.id, ROW_NUMBER() OVER (ORDER BY q.id) AS rn
         FROM questions q
                  JOIN java_skill js ON q.skill_id = js.skill_id
     ),
     created_quizzes AS (
INSERT INTO quizzes (title, status, created_at, user_id)
VALUES
    ('Java Quiz 1', 'ACTIVE', NOW(), 1),
    ('Java Quiz 2', 'ACTIVE', NOW(), 1),
    ('Java Quiz 3', 'ACTIVE', NOW(), 1)
    RETURNING id, title
    )
INSERT INTO quiz_questions (quiz_id, question_id)
SELECT cq.id, jq.id
FROM created_quizzes cq
         JOIN java_questions jq
              ON (cq.title = 'Java Quiz 1' AND jq.rn BETWEEN 1 AND 5)
                  OR (cq.title = 'Java Quiz 2' AND jq.rn BETWEEN 6 AND 10)
                  OR (cq.title = 'Java Quiz 3' AND jq.rn BETWEEN 11 AND 15);

-- 6) Create 3 Spring Boot quizzes for user 1 and map 5 unique Spring Boot questions each
WITH sb_skill AS (
    SELECT id AS skill_id FROM skills WHERE name = 'Spring Boot'
),
     sb_questions AS (
         SELECT q.id, ROW_NUMBER() OVER (ORDER BY q.id) AS rn
         FROM questions q
                  JOIN sb_skill ss ON q.skill_id = ss.skill_id
     ),
     created_quizzes AS (
INSERT INTO quizzes (title, status, created_at, user_id)
VALUES
    ('Spring Boot Quiz 1', 'ACTIVE', NOW(), 1),
    ('Spring Boot Quiz 2', 'ACTIVE', NOW(), 1),
    ('Spring Boot Quiz 3', 'ACTIVE', NOW(), 1)
    RETURNING id, title
    )
INSERT INTO quiz_questions (quiz_id, question_id)
SELECT cq.id, sq.id
FROM created_quizzes cq
         JOIN sb_questions sq
              ON (cq.title = 'Spring Boot Quiz 1' AND sq.rn BETWEEN 1 AND 5)
                  OR (cq.title = 'Spring Boot Quiz 2' AND sq.rn BETWEEN 6 AND 10)
                  OR (cq.title = 'Spring Boot Quiz 3' AND sq.rn BETWEEN 11 AND 15);

-- 7) Insert 3 Java DSA coding problems
INSERT INTO code_problems (
    title, prompt, category, difficulty_level,
    sample_input, expected_output, created_at, skill_id
)
SELECT cp.title, cp.prompt, cp.category, cp.difficulty_level,
       cp.sample_input, cp.expected_output, NOW(), s.id
FROM skills s
         JOIN (
    VALUES
        (
            'Reverse a String',
            'Given a string, return the reversed string. Implement the solution in Java.',
            'DSA',
            'EASY',
            'hello',
            'olleh'
        ),
        (
            'Find Maximum in Array',
            'Given an integer array, return the maximum element. Implement the solution in Java.',
            'DSA',
            'EASY',
            '5 1 9 3 7',
            '9'
        ),
        (
            'Move Zeros to End',
            'Given an integer array, move all zeros to the end while maintaining the relative order of non-zero elements.',
            'DSA',
            'MEDIUM',
            '0 1 0 3 12',
            '1 3 12 0 0'
        )
) AS cp(title, prompt, category, difficulty_level, sample_input, expected_output)
              ON TRUE
WHERE s.name = 'Java';

-- 8) Insert at least 2 test cases per coding problem
WITH p AS (
    SELECT id, title
    FROM code_problems
    WHERE title IN ('Reverse a String', 'Find Maximum in Array', 'Move Zeros to End')
)
INSERT INTO code_test_cases (problem_id, stdin, expected_output, is_sample, weight)
SELECT p.id, tc.stdin, tc.expected_output, tc.is_sample, tc.weight
FROM p
         JOIN (
    VALUES
        ('Reverse a String', 'hello', 'olleh', TRUE, 1),
        ('Reverse a String', 'Java', 'avaJ', FALSE, 1),

        ('Find Maximum in Array', '5 1 9 3 7', '9', TRUE, 1),
        ('Find Maximum in Array', '-2 -9 -1 -7', '-1', FALSE, 1),

        ('Move Zeros to End', '0 1 0 3 12', '1 3 12 0 0', TRUE, 1),
        ('Move Zeros to End', '1 0 2 0 0 3', '1 2 3 0 0 0', FALSE, 1)
) AS tc(problem_title, stdin, expected_output, is_sample, weight)
              ON p.title = tc.problem_title;

COMMIT;