package com.pariksha.attempt;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.List;

public final class AttemptDtos {

    private AttemptDtos() {
    }

    // ------------------------------------------------------------- student: exam list

    /** UPCOMING (not started), LIVE (can be taken now), ENDED (window closed) */
    public enum Availability { UPCOMING, LIVE, ENDED }

    /** NOT_STARTED, IN_PROGRESS or SUBMITTED */
    public record StudentExamSummary(Long examId, String name, String description, LocalDateTime startTime,
                                     LocalDateTime endTime, int durationMinutes, double totalMarks,
                                     int totalQuestions, Availability availability, String attemptStatus) {
    }

    // ------------------------------------------------------------- student: taking the exam

    /** Option shown to the student - note: no "correct" flag! */
    public record PaperOption(Long id, String text) {
    }

    public record PaperQuestion(Long questionId, int questionNumber, String content,
                                double positiveMarks, double negativeMarks, List<PaperOption> options) {
    }

    public record ExamPaperResponse(Long attemptId, Long examId, String examName, LocalDateTime startedAt,
                                    LocalDateTime deadline, long remainingSeconds, List<PaperQuestion> questions) {
    }

    public record AnswerRequest(
            @NotNull Long questionId,
            Long selectedOptionId,                       // null = skipped
            @PositiveOrZero Integer timeSpentSeconds) {
    }

    public record SubmitRequest(@NotNull List<@Valid AnswerRequest> answers) {
    }

    // ------------------------------------------------------------- results

    public record ResultSummary(Long attemptId, Long examId, String examName, double score, double maxScore,
                                double percentage, int correct, int wrong, int unattempted,
                                int rank, LocalDateTime submittedAt) {
    }

    public record SubmissionResponse(Long attemptId, String message, ResultSummary result) {
    }

    public record ReviewOption(Long id, String text, boolean correct) {
    }

    public record AnswerReview(int questionNumber, Long questionId, String content, List<ReviewOption> options,
                               Long selectedOptionId, boolean correct, double marksAwarded,
                               int timeSpentSeconds, String explanation) {
    }

    public record ResultDetail(ResultSummary summary, List<AnswerReview> review) {
    }

    // ------------------------------------------------------------- teacher: reports

    public record LeaderboardEntry(int rank, Long studentId, String studentName, String rollNumber,
                                   double score, int correct, int wrong, int unattempted,
                                   LocalDateTime submittedAt) {
    }

    public record QuestionStat(int questionNumber, Long questionId, long attempted, long correct,
                               double accuracyPercent) {
    }

    public record ExamAnalytics(Long examId, String examName, double totalMarks, long submitted, long inProgress,
                                double averageScore, double highestScore, double lowestScore,
                                List<QuestionStat> questionStats) {
    }
}
