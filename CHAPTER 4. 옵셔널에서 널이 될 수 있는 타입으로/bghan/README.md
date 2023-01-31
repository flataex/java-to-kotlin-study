# 4장 옵셔널에서 널이 될 수 있는 타입으로

## 4.1 없음을 표현하기

자바에서 `null`을 표현하는 방법
- 변수명에 OrNull postfix 붙임 ex) addressOrNull &rarr; 코드가 더럽고 NPE 계속 의식해야함
- `@Nullable`, `@NotNullable` 사용

코틀린에선 `null` 대신 `optional`, `maybe` 권장
- 코틀린 타입에선 T = T?의 하위 타입 ex) String 타입 값을 String? 타입 값에 할당 가능
- T &ne; Optional<T>의 하위 타입 ex) String 타입 값을 Optional 타입 값에 할당하기 위해 Optional<String> 감싸야함

∴ `String? -> String` 은 쉽지만, `String? -> Optional<String>` 은 어려움


## 4.2 옵셔널에서 널 가능성으로 리팩터링하기

<table>

<tr>
<td>Java</td><td>Kotlin</td>
</tr>

<td>

```java
public class Legs {

    public static Optional<Leg> findLongestLegOver(
            List<Leg> legs,
            Duration duration
    ) {
        Leg result = null;

        for (Leg leg : legs) {
            if (isLongerThan(leg, duration)) {
                if (result == null ||
                        isLongerThan(leg, result.getPlannedDuration())) {
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

</td>

<td>

```kotlin
object Legs {

    @JvmStatic
    fun findLongestLegOver(
        legs: List<Leg>,
        duration: Duration
    ): Optional<Leg> {
        return Optional.ofNullable(longestLegOver(legs, duration))
    }

    fun longestLegOver(legs: List<Leg>, duration: Duration): Leg? {
        var result: Leg? = null

        for (leg in legs) {
            if (isLongerThan(leg, duration))
                if (result == null ||
                    isLongerThan(leg, result.plannedDuration)
                )
                    result = leg
        }

        return result
    }

    private fun isLongerThan(leg: Leg, duration: Duration): Boolean {
        return leg.plannedDuration.compareTo(duration) > 0
    }
}
```

</td>
</table>

#### Iteration과 for loop
> Kotlin에서는 Iterable이 아닌 다른 type을 for loop에 사용 가능

- Iterable 확장한 타입
- Iterator를 반환하는 iterator() 메서드를 제공하는 타입
- Iterator를 반환하는 T.iterator() 확장 함수가 영역 안에 정의된 T 타입

#### [Kotlin - object와 class 키워드의 차이점](https://codechacha.com/ko/kotlin-object-vs-class/)
> object로 클래스를 정의하면, 싱클턴(Singleton) 패턴이 적용되어 객체가 한번만 생성되도록 함 &rarr; java에서의 싱글톤 보장을 위한 boilerplate &darr;

#### [Kotlin var과 val의 차이점은 무엇인가?](https://kotlinworld.com/173)
> var : mutable 변수로 읽기 쓰기 모두 허용되는 변수 &rarr; variable의 약자
> val : immuatable 변수로 읽기만 허용되는 변수 &rarr; value의 약자

<table>

<tr>
<td>Kotlin v1</td><td>Kotlin v2</td>
</tr>

<tr>
<td>

```kotlin
@Test
fun is_absent_when_no_legs() {
        assertEquals(
            Optional.empty<Any>(), 
            findLongestLegOver(emptyList(), Duration.ZERO)
        ) 
}

@Test
fun is_absent_when_no_legs_long_enough() {
        assertEquals(
            Optional.empty<Any>(),
            findLongestLegOver(legs, oneDay)
        ) 
}
```

</td>

<td>

```kotlin
@Test
fun 'is absent when no legs'() {
    assertNull(longestLegOver(emptyList(), Duration.ZERO)) 
}
@Test
fun 'is absent when no legs long enough'() {
    assertNull(longestl_egOver(legs, oneDay))
}
```

</td>

</tr>

<tr>

<td>

```kotlin
@Test
fun is_longest_leg_when_one_match() { 
    assertEquals(
        "one day",
        findLongestLegOver(legs, oneDay.minusMiUis(l))
            .orElseThrow().description
    ) 
}

@Test
fun is_longest_leg_when_more_than_one_match() { 
    assertEquals(
        "one day", 
        findLongestLegOver(legs, Duration.ofMinutes(59))
            .orElseThrow().description
    ) 
}
```
</td>

<td>

```kotlin
@Test
fun 'is longest leg when one match'() {
    assertEquals(
        "one day",
        longestLegOver(legs, oneDay.minusMiUis(l))
        !!.description 
    )
}

@Test
fun 'is longest leg when more than one match'() {
    assertEquals(
        "one day",
        longestLegOver(legs, Duration.ofMinutes(59))
            ?.description
    ) 
}
```
</td>
</tr>
</table>

#### [Kotlin ?, !!](https://anythingcafe.tistory.com/20)
> ? 는 변수에 null 허용
> !! 는 해당 변수가 현재 널 값이 아니라고 컴파일러에게 알려줘서 컴파일 에러가 나지 않도록 할 때 사용

## 4.3 코틀린다운 코드로 리팩터링하기

<table>

<tr>
<td>

```kotlin
import java.time.Duration


fun longestLegOver(legs: List<Leg>, duration: Duration): Leg? {
    var result: Leg? = null

    for (leg in legs) {
        if (Legs.isLongerThan(leg, duration))
            if (result == null ||
                Legs.isLongerThan(leg, result.plannedDuration))
                result = leg
    }

    return result
}

private fun Leg.isLongerThan(duration: Duration) =
    plannedDuration.compareTo(duration) > 0
```

</td>
</tr>

<tr>
<td>

```kotlin
fun longestLegOver(
    legs: List<Leg>,
    duration: Duration
): Leg? {
    val longestLeg: Leg? = legs.maxByOrNull(Leg::plannedDuration)

    return if (longestLeg != null && longestLeg.plannedDuration > duration)
        longestLeg
    else
        null
}
```
</td>
</tr>

```kotlin
fun longestLegOver( 
    legs: List<Leg>,
    duration: Duration 
): Leg? { 
    val longestLeg = legs.maxByOrNull(Leg::plannedDuration) ?: return null
    return if (longestLeg.plannedDuration > duration) 
        longestLeg 
    else 
        null 
}
```

<td>
<tr>

```kotlin
fun longestLegOver(
    legs: List<Leg>, 
    duration: Duration
): Leg? = 
    legs.maxByOrNuU(Leg::plannedDuration)?.let { longestLeg -> 
        if (longestLeg.plannedDuration > duration) 
            longestLeg
        else 
            null 
    }
```

</tr>
</td>

<tr>
<td>

```kotlin
fun longestLegOver(
    legs: List<Leg>, 
    duration: Duration
): Leg? { 
    val longestLeg = legs.maxByOrNuU(Leg::plannedDuration)
    
    return when {
        longestLeg == null -> null
        longestLeg.plannedDuration > duration -> longestLeg
        else -> null
    }
}
```

</td>
</tr>

<tr>
<td>

```kotlin

fun longestLegOver(
    legs: List<Leg>,
    duration: Duration
): Leg? =
    legs.maxByOrNull(Leg::plannedDuration)?.takeIf { longestLeg -> 
        longestLeg.plannedDuration > duration
    }
```

</td>
</tr>

</table>

#### [Kotlin maxByOrNull docs](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/max-by-or-null.html)

#### [Kotlin 의외로 놓치기 쉬운 when, 제대로 알아보기](https://readystory.tistory.com/200)

#### [언제 takeIf()와 takeUnless()를 쓸까?](https://wooooooak.github.io/kotlin/2019/05/20/WhenToUseTakeIfAndTakeUnless/)

#### [Kotlin let, with, run, apply, also 차이 비교 정리](https://blog.yena.io/studynote/2020/04/15/Kotlin-Scope-Functions.html)