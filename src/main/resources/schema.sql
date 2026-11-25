-- =====================================================================
-- Pariksha DB schema  (MySQL 8+)  - run automatically at startup by Spring (spring.sql.init)
-- You can also open this file in MySQL Workbench and run it manually.
-- =====================================================================

CREATE TABLE IF NOT EXISTS institutes (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    logo_url    VARCHAR(500) NULL,
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_institutes_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS users (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    institute_id   BIGINT       NOT NULL,
    name           VARCHAR(150) NOT NULL,
    email          VARCHAR(190) NOT NULL,
    mobile_number  VARCHAR(20)  NULL,
    password_hash  VARCHAR(100) NOT NULL,
    role           VARCHAR(20)  NOT NULL,            -- ADMIN | TEACHER | STUDENT
    active         TINYINT(1)   NOT NULL DEFAULT 1,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_institute_role (institute_id, role),
    CONSTRAINT fk_users_institute FOREIGN KEY (institute_id) REFERENCES institutes (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS standards (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(128) NOT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_standards_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS subjects (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(128) NOT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_subjects_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS chapters (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    subject_id  BIGINT       NOT NULL,
    name        VARCHAR(128) NOT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_chapters_subject_name (subject_id, name),
    CONSTRAINT fk_chapters_subject FOREIGN KEY (subject_id) REFERENCES subjects (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS students (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    institute_id    BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    standard_id     BIGINT       NULL,
    roll_number     VARCHAR(30)  NOT NULL,
    gender          VARCHAR(11)  NULL,
    parent_contact  VARCHAR(20)  NULL,
    joining_date    DATE         NOT NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_students_user (user_id),
    UNIQUE KEY uk_students_institute_roll (institute_id, roll_number),
    CONSTRAINT fk_students_institute FOREIGN KEY (institute_id) REFERENCES institutes (id),
    CONSTRAINT fk_students_user      FOREIGN KEY (user_id)      REFERENCES users (id),
    CONSTRAINT fk_students_standard  FOREIGN KEY (standard_id)  REFERENCES standards (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS questions (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    institute_id  BIGINT       NOT NULL,
    subject_id    BIGINT       NOT NULL,
    chapter_id    BIGINT       NULL,
    standard_id   BIGINT       NULL,
    difficulty    VARCHAR(10)  NOT NULL,             -- EASY | MEDIUM | HARD
    content       TEXT         NOT NULL,
    explanation   TEXT         NULL,
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    created_by    BIGINT       NULL,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_questions_institute_subject (institute_id, subject_id),
    KEY idx_questions_chapter (chapter_id),
    CONSTRAINT fk_questions_institute FOREIGN KEY (institute_id) REFERENCES institutes (id),
    CONSTRAINT fk_questions_subject   FOREIGN KEY (subject_id)   REFERENCES subjects (id),
    CONSTRAINT fk_questions_chapter   FOREIGN KEY (chapter_id)   REFERENCES chapters (id),
    CONSTRAINT fk_questions_standard  FOREIGN KEY (standard_id)  REFERENCES standards (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS question_options (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    question_id   BIGINT        NOT NULL,
    option_text   VARCHAR(1000) NOT NULL,
    is_correct    TINYINT(1)    NOT NULL DEFAULT 0,
    option_order  INT           NOT NULL,
    PRIMARY KEY (id),
    KEY idx_options_question (question_id),
    CONSTRAINT fk_options_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS exams (
    id                       BIGINT        NOT NULL AUTO_INCREMENT,
    institute_id             BIGINT        NOT NULL,
    name                     VARCHAR(200)  NOT NULL,
    description              VARCHAR(1000) NULL,
    duration_minutes         INT           NOT NULL,
    start_time               DATETIME(6)   NOT NULL,
    end_time                 DATETIME(6)   NOT NULL,
    total_marks              DOUBLE        NOT NULL DEFAULT 0,
    show_result_immediately  TINYINT(1)    NOT NULL DEFAULT 1,
    status                   VARCHAR(15)   NOT NULL DEFAULT 'DRAFT',   -- DRAFT | PUBLISHED | ARCHIVED
    created_by               BIGINT        NULL,
    created_at               DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at               DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_exams_institute_status (institute_id, status),
    CONSTRAINT fk_exams_institute FOREIGN KEY (institute_id) REFERENCES institutes (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS exam_questions (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    exam_id          BIGINT NOT NULL,
    question_id      BIGINT NOT NULL,
    question_number  INT    NOT NULL,
    positive_marks   DOUBLE NOT NULL,
    negative_marks   DOUBLE NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_exam_question (exam_id, question_id),
    UNIQUE KEY uk_exam_number (exam_id, question_number),
    KEY idx_exam_questions_question (question_id),
    CONSTRAINT fk_eq_exam     FOREIGN KEY (exam_id)     REFERENCES exams (id) ON DELETE CASCADE,
    CONSTRAINT fk_eq_question FOREIGN KEY (question_id) REFERENCES questions (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS classrooms (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    institute_id  BIGINT       NOT NULL,
    name          VARCHAR(128) NOT NULL,
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_classrooms_institute_name (institute_id, name),
    CONSTRAINT fk_classrooms_institute FOREIGN KEY (institute_id) REFERENCES institutes (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS classroom_students (
    classroom_id  BIGINT NOT NULL,
    student_id    BIGINT NOT NULL,
    PRIMARY KEY (classroom_id, student_id),
    KEY idx_cs_student (student_id),
    CONSTRAINT fk_cs_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms (id) ON DELETE CASCADE,
    CONSTRAINT fk_cs_student   FOREIGN KEY (student_id)   REFERENCES students (id)   ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS classroom_exams (
    classroom_id  BIGINT NOT NULL,
    exam_id       BIGINT NOT NULL,
    PRIMARY KEY (classroom_id, exam_id),
    KEY idx_ce_exam (exam_id),
    CONSTRAINT fk_ce_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms (id) ON DELETE CASCADE,
    CONSTRAINT fk_ce_exam      FOREIGN KEY (exam_id)      REFERENCES exams (id)      ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS exam_attempts (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    exam_id             BIGINT       NOT NULL,
    student_id          BIGINT       NOT NULL,
    status              VARCHAR(15)  NOT NULL DEFAULT 'IN_PROGRESS',   -- IN_PROGRESS | SUBMITTED
    started_at          DATETIME(6)  NOT NULL,
    submitted_at        DATETIME(6)  NULL,
    total_score         DOUBLE       NOT NULL DEFAULT 0,
    correct_count       INT          NOT NULL DEFAULT 0,
    wrong_count         INT          NOT NULL DEFAULT 0,
    unattempted_count   INT          NOT NULL DEFAULT 0,
    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_attempt_exam_student (exam_id, student_id),
    KEY idx_attempt_student (student_id),
    CONSTRAINT fk_attempt_exam    FOREIGN KEY (exam_id)    REFERENCES exams (id),
    CONSTRAINT fk_attempt_student FOREIGN KEY (student_id) REFERENCES students (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS attempt_answers (
    id                  BIGINT   NOT NULL AUTO_INCREMENT,
    attempt_id          BIGINT   NOT NULL,
    question_id         BIGINT   NOT NULL,
    selected_option_id  BIGINT   NULL,
    is_correct          TINYINT(1) NOT NULL DEFAULT 0,
    marks_awarded       DOUBLE   NOT NULL DEFAULT 0,
    time_spent_seconds  INT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_answer_attempt_question (attempt_id, question_id),
    KEY idx_answer_question (question_id),
    CONSTRAINT fk_answer_attempt  FOREIGN KEY (attempt_id)         REFERENCES exam_attempts (id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id)        REFERENCES questions (id),
    CONSTRAINT fk_answer_option   FOREIGN KEY (selected_option_id) REFERENCES question_options (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
