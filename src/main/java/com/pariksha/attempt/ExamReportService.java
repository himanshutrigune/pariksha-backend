package com.pariksha.attempt;

import com.pariksha.attempt.AttemptDtos.ExamAnalytics;
import com.pariksha.attempt.AttemptDtos.LeaderboardEntry;
import com.pariksha.attempt.AttemptDtos.QuestionStat;
import com.pariksha.exam.Exam;
import com.pariksha.exam.ExamQuestion;
import com.pariksha.exam.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reports for ADMIN / TEACHER: leaderboard and exam analytics (replaces the old TestAnalysis table). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamReportService {

    private final ExamService examService;
    private final ExamAttemptRepository attemptRepository;
    private final ScoringService scoringService;

    public List<LeaderboardEntry> leaderboard(Long instituteId, Long examId) {
        examService.find(instituteId, examId);   // also verifies the exam belongs to this institute
        List<ExamAttempt> attempts = attemptRepository.findLeaderboard(examId, AttemptStatus.SUBMITTED);

        List<LeaderboardEntry> entries = new ArrayList<>();
        int position = 0;
        int rank = 0;
        double previousScore = 0;
        for (ExamAttempt a : attempts) {
            position++;
            if (position == 1 || Double.compare(a.getTotalScore(), previousScore) != 0) {
                rank = position;            // equal scores share the same rank (1, 2, 2, 4 ...)
            }
            previousScore = a.getTotalScore();
            entries.add(new LeaderboardEntry(rank, a.getStudent().getId(), a.getStudent().getUser().getName(),
                    a.getStudent().getRollNumber(), a.getTotalScore(), a.getCorrectCount(), a.getWrongCount(),
                    a.getUnattemptedCount(), a.getSubmittedAt()));
        }
        return entries;
    }

    public ExamAnalytics analytics(Long instituteId, Long examId) {
        Exam exam = examService.find(instituteId, examId);
        List<ExamAttempt> submitted = attemptRepository.findLeaderboard(examId, AttemptStatus.SUBMITTED);
        long inProgress = attemptRepository.countByExamIdAndStatus(examId, AttemptStatus.IN_PROGRESS);

        DoubleSummaryStatistics stats = submitted.stream().mapToDouble(ExamAttempt::getTotalScore).summaryStatistics();
        boolean hasData = stats.getCount() > 0;

        Map<Long, long[]> perQuestion = new HashMap<>();   // questionId -> [attempted, correct]
        for (Object[] row : attemptRepository.questionStats(examId, AttemptStatus.SUBMITTED)) {
            perQuestion.put(((Number) row[0]).longValue(),
                    new long[]{((Number) row[1]).longValue(), ((Number) row[2]).longValue()});
        }

        List<QuestionStat> questionStats = new ArrayList<>();
        for (ExamQuestion eq : exam.getQuestions()) {
            long[] counts = perQuestion.getOrDefault(eq.getQuestion().getId(), new long[]{0, 0});
            questionStats.add(new QuestionStat(eq.getQuestionNumber(), eq.getQuestion().getId(), counts[0], counts[1],
                    scoringService.percentage(counts[1], counts[0])));
        }

        return new ExamAnalytics(exam.getId(), exam.getName(), exam.getTotalMarks(), stats.getCount(), inProgress,
                hasData ? Math.round(stats.getAverage() * 100.0) / 100.0 : 0,
                hasData ? stats.getMax() : 0,
                hasData ? stats.getMin() : 0,
                questionStats);
    }
}
