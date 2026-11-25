package com.pariksha.classroom;

import com.pariksha.classroom.ClassroomDtos.ClassroomDetail;
import com.pariksha.classroom.ClassroomDtos.ClassroomRequest;
import com.pariksha.classroom.ClassroomDtos.ClassroomResponse;
import com.pariksha.classroom.ClassroomDtos.ExamIdsRequest;
import com.pariksha.classroom.ClassroomDtos.StudentIdsRequest;
import com.pariksha.security.AuthUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classrooms")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
@Tag(name = "Classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassroomResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody ClassroomRequest request) {
        return classroomService.create(user.instituteId(), request);
    }

    @GetMapping
    public List<ClassroomResponse> list(@AuthenticationPrincipal AuthUser user) {
        return classroomService.list(user.instituteId());
    }

    @GetMapping("/{id}")
    public ClassroomDetail get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return classroomService.get(user.instituteId(), id);
    }

    @PostMapping("/{id}/students")
    public ClassroomDetail addStudents(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                       @Valid @RequestBody StudentIdsRequest request) {
        return classroomService.addStudents(user.instituteId(), id, request.studentIds());
    }

    @DeleteMapping("/{id}/students/{studentId}")
    public ClassroomDetail removeStudent(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                         @PathVariable Long studentId) {
        return classroomService.removeStudent(user.instituteId(), id, studentId);
    }

    @PostMapping("/{id}/exams")
    public ClassroomDetail addExams(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                    @Valid @RequestBody ExamIdsRequest request) {
        return classroomService.addExams(user.instituteId(), id, request.examIds());
    }

    @DeleteMapping("/{id}/exams/{examId}")
    public ClassroomDetail removeExam(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                      @PathVariable Long examId) {
        return classroomService.removeExam(user.instituteId(), id, examId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        classroomService.delete(user.instituteId(), id);
    }
}
