package com.pariksha.exam;

import com.pariksha.question.Question;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A question placed in an exam, with its own marking scheme (like TestQuestion in the old DB). */
@Entity
@Table(name = "exam_questions")
@Getter
@Setter
@NoArgsConstructor
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "question_number", nullable = false)
    private int questionNumber;

    @Column(name = "positive_marks", nullable = false)
    private double positiveMarks;

    @Column(name = "negative_marks", nullable = false)
    private double negativeMarks;
}
