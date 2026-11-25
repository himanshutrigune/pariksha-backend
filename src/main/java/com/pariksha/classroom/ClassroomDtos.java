package com.pariksha.classroom;

import com.pariksha.exam.Exam;
import com.pariksha.exam.ExamStatus;
import com.pariksha.student.Student;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ClassroomDtos {

    private ClassroomDtos() {
    }

    public record ClassroomRequest(@NotBlank @Size(max = 128) String name) {
    }

    public record StudentIdsRequest(@NotEmpty Set<Long> studentIds) {
    }

    public record ExamIdsRequest(@NotEmpty Set<Long> examIds) {
    }

    public record ClassroomResponse(Long id, String name, int studentCount, int examCount) {
        public static ClassroomResponse from(Classroom c) {
            return new ClassroomResponse(c.getId(), c.getName(), c.getStudents().size(), c.getExams().size());
        }
    }

    public record StudentBrief(Long id, String name, String rollNumber) {
        static StudentBrief from(Student s) {
            return new StudentBrief(s.getId(), s.getUser().getName(), s.getRollNumber());
        }
    }

    public record ExamBrief(Long id, String name, ExamStatus status) {
        static ExamBrief from(Exam e) {
            return new ExamBrief(e.getId(), e.getName(), e.getStatus());
        }
    }

    public record ClassroomDetail(Long id, String name, List<StudentBrief> students, List<ExamBrief> exams) {
        public static ClassroomDetail from(Classroom c) {
            return new ClassroomDetail(c.getId(), c.getName(),
                    c.getStudents().stream().map(StudentBrief::from)
                            .sorted(Comparator.comparing(StudentBrief::name)).toList(),
                    c.getExams().stream().map(ExamBrief::from)
                            .sorted(Comparator.comparing(ExamBrief::id)).toList());
        }
    }
}
