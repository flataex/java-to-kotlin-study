package travelator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import travelator.KotlinLegs.findLongestLegOver
import travelator.KotlinLegs.longestLegOver
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.List
import java.util.concurrent.ThreadLocalRandom

class KotlinLongestLegOverTests {
    private val legs = List.of<KotlinLeg>(
        leg("one hour", Duration.ofHours(1)),
        leg("one day", Duration.ofDays(1)),
        leg("two hours", Duration.ofHours(2))
    )
    private val oneDay = Duration.ofDays(1)

    @Test
    fun `is absent when no leg`() {
        Assertions.assertEquals(
            Optional.empty<Any>(),
            findLongestLegOver(emptyList(), Duration.ZERO)
        )
    }

    @Test
    fun `is absent when no legs long enough`() {
        Assertions.assertEquals(
            Optional.empty<Any>(),
            findLongestLegOver(legs, oneDay)
        )
    }

    @Test
    fun is_longest_leg_when_one_match() {
        Assertions.assertEquals(
            "one day",
            longestLegOver(legs, oneDay.minusMillis(1))
            !!.description
        )
    }

    @Test
    fun is_longest_leg_when_more_than_one_match() {
        Assertions.assertEquals(
            "one day",
            longestLegOver(legs, Duration.ofMinutes(59))
                ?.description
        )
    }

    private fun leg(description: String, duration: Duration): KotlinLeg? {
        val start = ZonedDateTime.ofInstant(
            Instant.ofEpochSecond(ThreadLocalRandom.current().nextInt().toLong()),
            ZoneId.of("UTC")
        )
        return KotlinLeg(description, start, start.plus(duration))
    }
}