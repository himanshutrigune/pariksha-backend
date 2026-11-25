package com.pariksha.question;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class QuestionDtos {

    private QuestionDtos() {
    }

    public record OptionRequest(
            @NotBlank @Size(max = 1000) String text,
            boolean correct) {
    }

    public record QuestionRequest(
            @NotBlank @Size(max = 5000) String content,
            @NotNull Long subjectId,
            Long chapterId,
            Long standardId,
            @NotNull Difficulty difficulty,
            @Size(max = 5000) String explanation,
            @NotNull @Size(min = 2, max = 6, message = "A question needs 2 to 6 options") List<@Valid OptionRequest> options) {
    }

    public record OptionResponse(Long id, String text, boolean correct) {
        public static OptionResponse from(QuestionOption o) {
            return new OptionResponse(o.getId(), o.getOptionText(), o.isCorrect());
        }
    }

    public record QuestionResponse(Long id, String content, Long subjectId, String subjectName,
                                   Long chapterId, String chapterName, Long standardId,
                                   Difficulty difficulty, String explanation, List<OptionResponse> options) {
        public static QuestionResponse from(Question q) {
            return new QuestionResponse(
                    q.getId(),
                    q.getContent(),
                    q.getSubject().getId(),
                    q.getSubject().getName(),
                    q.getChapter() == null ? null : q.getChapter().getId(),
                    q.getChapter() == null ? null : q.getChapter().getName(),
                    q.getStandard() == null ? null : q.getStandard().getId(),
                    q.getDifficulty(),
                    q.getExplanation(),
                    q.getOptions().stream().map(OptionResponse::from).toList());
        }
    }
}
