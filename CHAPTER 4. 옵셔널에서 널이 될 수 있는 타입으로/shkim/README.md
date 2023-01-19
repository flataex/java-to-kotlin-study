### 예제 코드는 여기에
https://github.com/MALLLAG/java-to-kotlin-example/tree/main/src/main/java/com/example/demo

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