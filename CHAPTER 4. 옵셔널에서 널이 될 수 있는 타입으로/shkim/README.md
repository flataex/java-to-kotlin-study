### 예제 코드는 여기에
https://github.com/MALLLAG/java-to-kotlin-example/tree/main/src/main/java/com/example/demo

<br>

## 없음을 표현하기

`코틀린을 null을 포용한다.` <br>

자바에서는 null 안정성을 위해 Optional을 도입했다. <br>
코틀린에서도 Optional을 사용할 수 있지만, null 가능성을 지원하기 위해 구체적으로 설계된 언어 기능을 사용하지 못하게 될 것이다.

코틀린 타입 시스템에서는 T는 T?의 하위 타입이다. *(자바에서는 모든 참조타입이 널이 될 수 있지만 코틀린에서는 “?” 를 붙여서 타입을 널이 될 수 있는 타입으로 지정해야한다.)* <br>
그래서 null이 될 수 없는 String 타입의 값을 null이 될 수 있는 String이 필요한 곳에 항상 쓸 수 있다. <br>
반면 T는 Optional<T>의 하위 타입이 아니기 때문에, 유연한 사용이 불가능 !

<br>

## 옵셔널에서 null 가능성으로 리팩토링하기

*아래 코드를 코틀린으로 개선해보자*

```java
public class Leg {
    private final String name;
    private final Duration plannedDuration;

    private Leg(String name, Duration plannedDuration) {
        this.name = name;
        this.plannedDuration = plannedDuration;
    }

    public static Leg leg(String name, Duration duration) {
        return new Leg(name, duration);
    }

    public Duration getPlannedDuration() {
        return plannedDuration;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return name;
    }
}
```

```java
public class Legs {

    public static Optional<Leg> findLongestLegOver(
            List<Leg> legs,
            Duration duration
    ) {
        Leg result = null;
        for (Leg leg : legs) {
            if (isLongerThan(leg, duration)) {
                if (result == null
                        || isLongerThan(leg, result.getPlannedDuration())
                ) {
                    result = leg;
                }
            }
        }
        return Optional.ofNullable(result);
    }

    private static boolean isLongerThan(Leg leg, Duration duration) {
        return leg.getPlannedDuration().compareTo(duration) > 0;
    }
}
```

```java
public class LongestLegOverTests {

    private final List<Leg> legs = List.of(
            leg("one hour", Duration.ofHours(1)),
            leg("one day", Duration.ofDays(1)),
            leg("two hours", Duration.ofHours(2))
    );
    private final Duration oneDay = Duration.ofDays(1);

    @Test
    public void is_absent_when_no_legs() {
        assertEquals(
                Optional.empty(),
                findLongestLegOver(emptyList(), Duration.ZERO)
        );
    }

    @Test
    public void is_absent_when_no_legs_long_enough() {
        assertEquals(
                Optional.empty(),
                findLongestLegOver(legs, oneDay)
        );
    }

    @Test
    public void is_longest_leg_when_one_match() {
        assertEquals(
                "one day",
                findLongestLegOver(legs, oneDay.minusMillis(1))
                        .orElseThrow().getDescription()
        );
    }

    @Test
    public void is_longest_leg_when_more_than_one_match() {
        assertEquals(
                "one day",
                findLongestLegOver(legs, Duration.ofMinutes(59))
                        .orElseThrow().getDescription()
        );
    }
}
```

<br>

***ver-1*** <br>

> 코틀린에서 null이 될 수 없는 parameter를 지정하면 컴파일러가 null 검사를 추가해줌. <br>

```kt
import java.time.Duration
import java.util.*

object Legs {

    @JvmStatic
    fun findLongestLegOver(
        legs: List<Leg>,
        duration: Duration
    ): Optional<Leg> {
        var result: Leg? = null
        for (leg in legs) {
            if (isLongerThan(leg, duration)) {
                if (result == null
                    || isLongerThan(leg, result.plannedDuration)
                ) {
                    result = leg
                }
            }
        }
        return Optional.ofNullable(result)
    }

    private fun isLongerThan(leg: Leg, duration: Duration): Boolean {
        return leg.plannedDuration.compareTo(duration) > 0
    }
}
```

```kt
import com.example.demo.nullability.Legs.findLongestLegOver
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*
import java.util.List

class LongestLegOverTests {

    private val legs = List.of(
        Leg.leg("one hour", Duration.ofHours(1)),
        Leg.leg("one day", Duration.ofDays(1)),
        Leg.leg("two hours", Duration.ofHours(2))
    )
    private val oneDay = Duration.ofDays(1)

    @Test
    fun is_absent_when_no_legs() {
        Assertions.assertEquals(
            Optional.empty<Any>(),
            findLongestLegOver(emptyList(), Duration.ZERO)
        )
    }

    @Test
    fun is_absent_when_no_legs_long_enough() {
        Assertions.assertEquals(
            Optional.empty<Any>(),
            findLongestLegOver(legs, oneDay)
        )
    }

    @Test
    fun is_longest_leg_when_one_match() {
        Assertions.assertEquals(
            "one day",
            findLongestLegOver(legs, oneDay.minusMillis(1))
                .orElseThrow().description
        )
    }

    @Test
    fun is_longest_leg_when_more_than_one_match() {
        Assertions.assertEquals(
            "one day",
            findLongestLegOver(legs, Duration.ofMinutes(59))
                .orElseThrow().description
        )
    }
}
```

<br>

***ver-2***

> 점진적으로 migration하기 위해 두 개의 findLongestLegOver를 만들자. <br>
> 하나는 Optional<Leg>를 반환, 다른 하나는 Leg?를 반환. <br>
> 자바 클라에서는 findLongestLegOver를 사용하고, 코틀린 클라에서는 longestLegOver를 사용하면 된다. <br>
> 자바에서 orElseThrow()는 코틀린에서는 !!와 같다. <br>
> 코틀린에서 ?.는 안전한 호출 연산자로, null이 아닐 때만 평가가 계속되고, null이면 null로 평가된다.

```kt
import java.time.Duration
import java.util.*

object Legs {

    @JvmStatic
    fun findLongestLegOver(
        legs: List<Leg>,
        duration: Duration
    ): Optional<Leg> {
        return Optional.ofNullable(longestLegOver(legs, duration))
    }

    fun longestLegOver(
        legs: List<Leg>,
        duration: Duration
    ): Leg? {
        var result: Leg? = null
        for (leg in legs) {
            if (isLongerThan(leg, duration)) {
                if (result == null
                    || isLongerThan(leg, result.plannedDuration)
                ) {
                    result = leg
                }
            }
        }
        return result
    }

    private fun isLongerThan(leg: Leg, duration: Duration): Boolean {
        return leg.plannedDuration.compareTo(duration) > 0
    }
}
```

```kt
import com.example.demo.nullability.Legs.longestLegOver
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.List

class LongestLegOverTests {

    private val legs = List.of(
        Leg.leg("one hour", Duration.ofHours(1)),
        Leg.leg("one day", Duration.ofDays(1)),
        Leg.leg("two hours", Duration.ofHours(2))
    )
    private val oneDay = Duration.ofDays(1)

    @Test
    fun `is absent when no legs`() {
        assertNull(longestLegOver(emptyList(), Duration.ZERO))
    }

    @Test
    fun `is absent when no legs long enough`() {
        assertNull(longestLegOver(legs, oneDay))
    }

    @Test
    fun `is longest leg when one match`() {
        assertEquals(
            "one day",
            longestLegOver(legs, oneDay.minusMillis(1))
                !!.description
        )
    }

    @Test
    fun `is longest leg when more than one match`() {
        assertEquals(
            "one day",
            longestLegOver(legs, Duration.ofMinutes(59))
                ?.description
        )
    }
}
```

<br>

***ver-3***

> 코틀린스러운 코드로 리팩토링하면 다음과 같다
> 엘비스 연산자인 ?:는 좌변이 null이 아니면 좌변을, null이면 우변을 돌려준다

```kt
import java.time.Duration

fun List<Leg>.longestOver(duration: Duration): Leg? {
    val longestLeg = maxByOrNull(Leg::plannedDuration)
    return when {
        longestLeg == null -> null
        longestLeg.plannedDuration > duration -> longestLeg
        else -> null
    }
}
```

```kt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.List

class LongestLegOverTests {

    private val legs = List.of(
        Leg.leg("one hour", Duration.ofHours(1)),
        Leg.leg("one day", Duration.ofDays(1)),
        Leg.leg("two hours", Duration.ofHours(2))
    )
    private val oneDay = Duration.ofDays(1)

    @Test
    fun `is absent when no legs`() {
        assertNull(emptyList<Leg>().longestOver(Duration.ZERO))
    }

    @Test
    fun `is absent when no legs long enough`() {
        assertNull(legs.longestOver(oneDay))
    }

    @Test
    fun `is longest leg when one match`() {
        assertEquals(
            "one day",
            legs.longestOver(oneDay.minusMillis(1))
                !!.description
        )
    }

    @Test
    fun `is longest leg when more than one match`() {
        assertEquals(
            "one day",
            legs.longestOver(Duration.ofMinutes(59))
                ?.description
        )
    }
}
```

<br>

## 정리

자바 Optional 타입은 투박하게 느껴지고, Optional을 null이 될 수 있는 타입으로 쉽게 migration 가능하다. <br>
모든 코드를 변환할 준비가 되지 않더라도, 둘을 혼용해서 사용도 가능하다.