package com.pariksha.academic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AcademicDtos {

    private AcademicDtos() {
    }

    public record NameRequest(@NotBlank @Size(max = 128) String name) {
    }

    public record NamedResponse(Long id, String name) {
        public static NamedResponse from(Standard s) {
            return new NamedResponse(s.getId(), s.getName());
        }

        public static NamedResponse from(Subject s) {
            return new NamedResponse(s.getId(), s.getName());
        }
    }

    public record ChapterResponse(Long id, String name, Long subjectId) {
        public static ChapterResponse from(Chapter c) {
            return new ChapterResponse(c.getId(), c.getName(), c.getSubject().getId());
        }
    }
}
