package com.pariksha.attempt;

import com.pariksha.attempt.AttemptDtos.ExamAnalytics;
import com.pariksha.attempt.AttemptDtos.LeaderboardEntry;
import com.pariksha.security.AuthUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams/{examId}")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
@Tag(name = "Exam Reports")
public class ExamReportController {

    private final ExamReportService reportService;

    @GetMapping("/leaderboard")
    public List<LeaderboardEntry> leaderboard(@AuthenticationPrincipal AuthUser user, @PathVariable Long examId) {
        return reportService.leaderboard(user.instituteId(), examId);
    }

    @GetMapping("/analytics")
    public ExamAnalytics analytics(@AuthenticationPrincipal AuthUser user, @PathVariable Long examId) {
        return reportService.analytics(user.instituteId(), examId);
    }
}
