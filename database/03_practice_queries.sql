-- Practice queries for MySQL Workbench (run after the app has started once and created the demo data)
USE pariksha_db;

-- 1. All students of the demo institute with their login e-mail
SELECT s.id, u.name, u.email, s.roll_number, st.name AS standard
FROM students s
JOIN users u ON u.id = s.user_id
LEFT JOIN standards st ON st.id = s.standard_id
ORDER BY s.roll_number;

-- 2. Question count per subject and difficulty
SELECT su.name AS subject, q.difficulty, COUNT(*) AS total_questions
FROM questions q
JOIN subjects su ON su.id = q.subject_id
WHERE q.active = 1
GROUP BY su.name, q.difficulty;

-- 3. The paper of an exam (question order + marks)
SELECT eq.question_number, q.content, eq.positive_marks, eq.negative_marks
FROM exam_questions eq
JOIN questions q ON q.id = eq.question_id
WHERE eq.exam_id = 1
ORDER BY eq.question_number;

-- 4. Leaderboard with ranking (window function - MySQL 8)
SELECT RANK() OVER (ORDER BY a.total_score DESC) AS rank_no,
       u.name, a.total_score, a.correct_count, a.wrong_count, a.unattempted_count
FROM exam_attempts a
JOIN students s ON s.id = a.student_id
JOIN users u ON u.id = s.user_id
WHERE a.exam_id = 1 AND a.status = 'SUBMITTED';

-- 5. Which questions are the hardest? (lowest accuracy)
SELECT aa.question_id,
       SUM(aa.selected_option_id IS NOT NULL)                       AS attempted,
       SUM(aa.is_correct)                                           AS correct,
       ROUND(100 * SUM(aa.is_correct) / NULLIF(SUM(aa.selected_option_id IS NOT NULL), 0), 2) AS accuracy_pct
FROM attempt_answers aa
GROUP BY aa.question_id
ORDER BY accuracy_pct;

-- 6. Students who have NOT attempted an exam yet
SELECT u.name
FROM classroom_exams ce
JOIN classroom_students cs ON cs.classroom_id = ce.classroom_id
JOIN students s ON s.id = cs.student_id
JOIN users u ON u.id = s.user_id
LEFT JOIN exam_attempts a ON a.exam_id = ce.exam_id AND a.student_id = s.id
WHERE ce.exam_id = 1 AND a.id IS NULL;

-- 7. SUBQUERY: students who scored above the exam average
SELECT u.name, a.total_score
FROM exam_attempts a
JOIN students s ON s.id = a.student_id
JOIN users u ON u.id = s.user_id
WHERE a.exam_id = 1
  AND a.total_score > (SELECT AVG(a2.total_score) FROM exam_attempts a2 WHERE a2.exam_id = 1);

-- 8. INDEXING: see how MySQL runs a query, then compare with/without an index
EXPLAIN SELECT * FROM questions WHERE institute_id = 1 AND subject_id = 1;   -- uses idx_questions_institute_subject
SHOW INDEX FROM exam_attempts;

-- 9. TRANSACTION: all-or-nothing (the app does the same with @Transactional)
START TRANSACTION;
UPDATE exam_attempts SET total_score = total_score + 1 WHERE id = 1;
-- check the result, then either:
ROLLBACK;     -- undo, or COMMIT; to keep the change
