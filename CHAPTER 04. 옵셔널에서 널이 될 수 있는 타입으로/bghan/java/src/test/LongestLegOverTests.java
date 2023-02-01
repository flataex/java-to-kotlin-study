import main.Leg;
import main.Legs;
import org.junit.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class LongestLegOverTests {

    private final List<Leg> legs = List.of(
            new Leg("one hour", Duration.ofHours(1)),
            new Leg("one day", Duration.ofDays(1)),
            new Leg("two hours", Duration.ofHours(2))
    );

    private final Duration oneDay = Duration.ofDays(1);

    @Test
    public void is_absent_when_no_legs() {
        assertEquals(
                Optional.empty(),
                Legs.findLongestLegOver(Collections.emptyList(), Duration.ZERO)
        );
    }

    @Test
    public void is_absent_when_no_legs_long_enough() {
        assertEquals(
                Optional.empty(),
                Legs.findLongestLegOver(legs, oneDay)
        );
    }

    @Test
    public void is_longest_leg_when_one_match() {
        assertEquals(
                "one day",
                Legs.findLongestLegOver(legs, oneDay.minusMillis(1))
                        .orElseThrow().getDescription()
        );
    }

    @Test
    public void is_longest_leg_when_more_than_one_match() {
        assertEquals(
                "one day",
                Legs.findLongestLegOver(legs, Duration.ofMinutes(59))
                        .orElseThrow().getDescription()
        );
    }
}