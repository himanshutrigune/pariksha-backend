package com.pariksha.attempt;

import org.springframework.stereotype.Component;

/** Pure marking rule - kept separate so it is trivial to unit test. */
@Component
public class ScoringService {

    /**
     * @return +positive for a correct answer, -negative for a wrong answer, 0 if the question was skipped
     */
    public double marksFor(boolean attempted, boolean correct, double positiveMarks, double negativeMarks) {
        if (!attempted) {
            return 0;
        }
        return correct ? positiveMarks : -negativeMarks;
    }

    public double percentage(double score, double maxScore) {
        if (maxScore <= 0) {
            return 0;
        }
        return Math.round(score / maxScore * 10000.0) / 100.0;
    }
}
