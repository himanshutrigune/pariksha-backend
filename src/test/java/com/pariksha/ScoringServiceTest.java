package com.pariksha;

import com.pariksha.attempt.ScoringService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTest {

    private final ScoringService scoring = new ScoringService();

    @Test
    void correctAnswerGivesPositiveMarks() {
        assertThat(scoring.marksFor(true, true, 4, 1)).isEqualTo(4.0);
    }

    @Test
    void wrongAnswerGivesNegativeMarks() {
        assertThat(scoring.marksFor(true, false, 4, 1)).isEqualTo(-1.0);
    }

    @Test
    void skippedQuestionGivesZero() {
        assertThat(scoring.marksFor(false, false, 4, 1)).isZero();
    }

    @Test
    void percentageIsRoundedToTwoDecimals() {
        assertThat(scoring.percentage(1, 3)).isEqualTo(33.33);
        assertThat(scoring.percentage(5, 0)).isZero();
    }
}
