package com.pariksha.classroom;

import com.pariksha.common.BaseEntity;
import com.pariksha.exam.Exam;
import com.pariksha.institute.Institute;
import com.pariksha.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/** A group of students (e.g. "Class 10 - A"). Exams assigned to the classroom become visible to its students. */
@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
public class Classroom extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institute_id", nullable = false)
    private Institute institute;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany
    @JoinTable(name = "classroom_students",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"))
    private Set<Student> students = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "classroom_exams",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "exam_id"))
    private Set<Exam> exams = new HashSet<>();
}
