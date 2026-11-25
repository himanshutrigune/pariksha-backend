package com.pariksha.exam;

import com.pariksha.common.BaseEntity;
import com.pariksha.institute.Institute;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Called "Test" in the old Parikshadb. */
@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
public class Exam extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institute_id", nullable = false)
    private Institute institute;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    /** Students can start the exam only between startTime and endTime. */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_marks", nullable = false)
    private double totalMarks;

    @Column(name = "show_result_immediately", nullable = false)
    private boolean showResultImmediately = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ExamStatus status = ExamStatus.DRAFT;

    @Column(name = "created_by")
    private Long createdBy;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionNumber ASC")
    @BatchSize(size = 50)
    private List<ExamQuestion> questions = new ArrayList<>();
}
