package com.pariksha.attempt;

import com.pariksha.attempt.AttemptDtos.ExamPaperResponse;
import com.pariksha.attempt.AttemptDtos.ResultDetail;
import com.pariksha.attempt.AttemptDtos.ResultSummary;
import com.pariksha.attempt.AttemptDtos.StudentExamSummary;
import com.pariksha.attempt.AttemptDtos.SubmissionResponse;
import com.pariksha.attempt.AttemptDtos.SubmitRequest;
import com.pariksha.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student - Take Exams")
public class StudentExamController {

    private final StudentExamService studentExamService;

    @GetMapping("/exams")
    @Operation(summary = "Exams assigned to me (through my classrooms)")
    public List<StudentExamSummary> myExams(@AuthenticationPrincipal AuthUser user) {
        return studentExamService.myExams(user);
    }

    @PostMapping("/exams/{examId}/start")
    @Operation(summary = "Start the exam (or resume if already in progress) and receive the question paper")
    public ExamPaperResponse start(@AuthenticationPrincipal AuthUser user, @PathVariable Long examId) {
        return studentExamService.start(user, examId);
    }

    @PostMapping("/exams/{examId}/submit")
    @Operation(summary = "Submit answers - the exam is evaluated automatically")
    public SubmissionResponse submit(@AuthenticationPrincipal AuthUser user, @PathVariable Long examId,
                                     @Valid @RequestBody SubmitRequest request) {
        return studentExamService.submit(user, examId, request);
    }

    @GetMapping("/results")
    @Operation(summary = "My result history")
    public List<ResultSummary> myResults(@AuthenticationPrincipal AuthUser user) {
        return studentExamService.myResults(user);
    }

    @GetMapping("/results/{examId}")
    @Operation(summary = "Detailed result with question-by-question review")
    public ResultDetail myResult(@AuthenticationPrincipal AuthUser user, @PathVariable Long examId) {
        return studentExamService.myResult(user, examId);
    }
}
