-- Master data shared by all institutes (same idea as Standard / Subject / Chapter in the old Parikshadb)

INSERT IGNORE INTO standards (name) VALUES
 ('Class 6'), ('Class 7'), ('Class 8'), ('Class 9'), ('Class 10'),
 ('Class 11'), ('Class 12'), ('JEE'), ('NEET');

INSERT IGNORE INTO subjects (name) VALUES
 ('Mathematics'), ('Physics'), ('Chemistry'), ('Biology'), ('English'), ('Computer Science');

INSERT IGNORE INTO chapters (subject_id, name)
SELECT s.id, c.name FROM subjects s
JOIN (SELECT 'Mathematics' AS subject, 'Algebra' AS name
      UNION ALL SELECT 'Mathematics', 'Geometry'
      UNION ALL SELECT 'Mathematics', 'Trigonometry'
      UNION ALL SELECT 'Mathematics', 'Probability'
      UNION ALL SELECT 'Physics', 'Kinematics'
      UNION ALL SELECT 'Physics', 'Laws of Motion'
      UNION ALL SELECT 'Physics', 'Electricity'
      UNION ALL SELECT 'Chemistry', 'Atomic Structure'
      UNION ALL SELECT 'Chemistry', 'Chemical Bonding'
      UNION ALL SELECT 'Biology', 'Cell Biology'
      UNION ALL SELECT 'Computer Science', 'Data Structures'
      UNION ALL SELECT 'Computer Science', 'Databases') c
  ON c.subject = s.name;
