package com.pariksha.exam;

import com.pariksha.question.QuestionDtos.QuestionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class ExamDtos {

    private ExamDtos() {
    }

    public record ExamQuestionRequest(
            @NotNull Long questionId,
            @NotNull @Positive Double positiveMarks,
            @PositiveOrZero Double negativeMarks) {
    }

    public record ExamRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 1000) String description,
            @NotNull @Min(1) Integer durationMinutes,
            @NotNull LocalDateTime startTime,
            @NotNull LocalDateTime endTime,
            boolean showResultImmediately,
            @NotEmpty List<@Valid ExamQuestionRequest> questions) {
    }

    public record ExamResponse(Long id, String name, String description, int durationMinutes,
                               LocalDateTime startTime, LocalDateTime endTime, double totalMarks,
                               int totalQuestions, boolean showResultImmediately, ExamStatus status) {
        public static ExamResponse from(Exam e) {
            return new ExamResponse(e.getId(), e.getName(), e.getDescription(), e.getDurationMinutes(),
                    e.getStartTime(), e.getEndTime(), e.getTotalMarks(), e.getQuestions().size(),
                    e.isShowResultImmediately(), e.getStatus());
        }
    }

    public record ExamQuestionResponse(int questionNumber, double positiveMarks, double negativeMarks,
                                       QuestionResponse question) {
        public static ExamQuestionResponse from(ExamQuestion eq) {
            return new ExamQuestionResponse(eq.getQuestionNumber(), eq.getPositiveMarks(), eq.getNegativeMarks(),
                    QuestionResponse.from(eq.getQuestion()));
        }
    }

    public record ExamDetailResponse(ExamResponse exam, List<ExamQuestionResponse> questions) {
        public static ExamDetailResponse from(Exam e) {
            return new ExamDetailResponse(ExamResponse.from(e),
                    e.getQuestions().stream().map(ExamQuestionResponse::from).toList());
        }
    }
}
