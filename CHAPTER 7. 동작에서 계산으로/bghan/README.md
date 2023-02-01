# 동작에서 계산으로

## 7.1 동작 & 7.2 계산

> 동작 : 언제 얼마나 많이 호출되느냐에 따라 결과가 달라지는 함수 &rarr; 확장 함수나 객체 필드에 의존하는 데이터가 값인 경우<br>
> 계산 : 호출된 시간과 상관없이 항상 같은 결과를 반환하는 함수 &rarr; 입력이 같을 때 동일한 계산으로 항상 같은 출력

<table>

<tr>
<td>동작</td><td>계산</td>
</tr>

<td>

```kotlin
fun Customer.fullName() = "$givenName $familyName"

val Customer.fullName get() = "$givenName $familyName"
```
</td>
<td>

```kotlin
fun fullName(customer: Customer) = "${customer.givenName} ${customer.familyName}"
```
</td>
</table>

## 7.3 동작

> println() &rarr; 호출된 시점이나 얼마나 많이 실행되느냐에 따라 결과가 달라짐<br>
> 호출하지 않으면 출력되지 않으나 한번 호출 할때나 두번 호출할때와는 다른 결과

```kotlin
class Customers {
    fun save(data: CustomerData): Customer { // 동작(DB 상태가 save 호출 횟수에 따라 다름)
        ...
    }
    
    fun find(id: String): Customer? {        // 동작(save 호출 여부에 종속적임)
        ...
    }
}
```
## 7.4 왜 계산과 동작에 신경쓰는가?

> 반환값으로 치환할 수 있는 방법은 언제 호출하든 같은 결과가 나올때만 가능

- Referential Transparency(= 참조 투명성) : 이 함수를 호출한 모든 부분에서 반환값으로 바꿔도 똑같이 동작하는 함수

## 7.5 왜 계산을 선호하는가?

> 계산이 훨씬 다루기 쉽기 때문 = 더 순수한 코드 = 의존도가 낮음

## 7.6 동작을 계산으로 리팩터링하기

### 7.6.1 기존 코드

> 동작은 시간예 예민하다.
 
- 반복문을 돌면서 바뀌는 시간 &rarr; 반복문 진입 전 시간 설정

<table>
<tr>
<td>AS-IS</td><td>TO-BE</td>
</tr>

<tr>
<td>

```java
public class InMemoryTrips implements Trips {
    
    @Override
    public Set<Trip> currentTripsFor(String customerId) {
        return tripsFor(customerId)
                .stream()
                .filter(trip -> trip.isPlannedToBeActiveAt(clock.instant()))
                .collect(toSet());
    }
}
```
</td>

<td>

```java
public class InMemoryTrips implements Trips {
    
    @Override
    public Set<Trip> currentTripsFor(String customerId) {
        var now = clock.instant();
        
        return tripsFor(customerId)
                .stream()
                .filter(trip -> trip.isPlannedToBeActiveAt(now))
                .collect(toSet());
    }
}
```
</td>
</tr>
</table>

> 시간에 의존하는 테스트는 성공하기 어렵다. &rarr; 원하는 결과를 얻기 위해 mock 시계를 만들어 주입

```java
import java.util.Optional;

public class TrackingTests {
    final StoppedClock clock = new StoppedClock();
    final InMemoryTrips trips = new InMemoryTrips(clock);
    final Tracking tracking = new Tracking(trips);

    @Test
    public void returns_empty_when_no_trip_planned_to_happen_now() {
        clock.now = anInstant();

        assertEquals(Optional.empty(), tracking.currentTripFor("aCustomer"));
    }

    @Test
    public void returns_single_active_booked_trip() {
        var diwaliTrip = givenATrip("cust1", "Diwali", "2020-11-13", "2020-11-15", BOOKED);
        givenATrip("cust1", "Christmas", "2020-12-24", "2020-11-26", BOOKED);
        
        clock.now = diwaliTrip.getPlannedStartTime().toInstant();
        
        assertEquals(
                Optional.of(diwaliTrip),
                tracking.currentTripFor("cust1")
        );
    }
}
```

```java
public class InMemoryTrips implements Trips {
    @Override
    public Set<Trip> currentTripsFor(String customerId, Instant at) {  // parameter로 받아 주입
        return tripsFor(customerId)
                .stream()
                .filter(trip -> trip.isPlannedToBeActiveAt(at))
                .collect(toSet());
    } 
}
```

> parameter로 주입하는 로직으로 변경되어 테스트 코드도 개선

```java
@Test
public void returns_empty_when_no_trip_planned_to_happen_now() {
    clock.now = anInstant();
    assertEquals(
            Optional.empty(),
            tracking.currentTripFor("cust1", clock.now)
    );
}

@Test
public void returns_single_active_booked_trip() {
    var diwaliTrip = givenATrip("cust1", "Diwali", "2020-11-13", "2020-11-15", BOOKED);
    givenATrip("cust1", "Christmas", "2020-12-24", "2020-11-26", BOOKED);
    
    assertEquals(
            Optional.of(diwaliTrip),
            tracking.currentTripFor("cust1", diwaliTrip.getPlannedStartTime().toInstant())
    );
}
```