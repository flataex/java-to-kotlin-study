객체 지향 다형성은 **연산이 가끔 변경되는 데이터 타입의 변경 가능성을 가능하게 해준다.** <br>
하지만 이는 **데이터 타입보다 연산이 자주 바뀔때는 별로 도움이 되지 않는다.**

<br>

```kt
if (item instanceof Journey) { 
    var journey = (Journey) item; 
    return ...
} else if (item instanceof Accommodation) { 
    var accommodation = (Accommodation) item; 
    return ...
} else if (item instanceof RestaurantBooking) { 
    var restaurant = (RestaurantBooking) item;
    return ...
} else {
    throw new IllegalStateException("should never happen");
}
```

위 타입 검사기는 모든 가능한 하위 클래스를 제대로 검증하지 못한다. <br>
그리고 만약 새 타입이 추가 되었다면, ItineraryItem의 하위 타입에 따라 분기하는 모든 코드를 추가해야 하고, 그렇지 않다면 IllegalStateException를 던지며 실패할 것이다.

코틀린에는 클래스를 조직화하고 런타임 타입 검사를 더 안전하고 편리하게 해주는 **봉인된 클래스**가 있다. <br>
**봉인된 클래스**는 직접적인 하위 클래스가 고정되어 있는 추상 클래스이고, 이는 하위 클래스를 같은 컴파일 유닛과 패키지 안에 정의해야 한다. <br>
이런 제한으로 실행 시점 타입 검사에는 자바의 실행 시점 타입 검사와 같은 문제가 없다.

<br>
<br>

| 다형성                                         | 봉인된 클래스 |
|---------------------------------------------|---------|
| 데이터 타입에 적용할 수 있는 연산보다 데이터 타입이 더 자주 변경될 때 사용 | 데이터 타입보다 데이터 타입에 적용할수 있는 연산이 더 자주 변경될 때 사용 |

<br>
<hr>

### 사용 예시

```kt
sealed class ItineraryItem {
    abstract val id: Id<ItineraryItem>
}

data class Accommodation(
    override val id: Id<Accommodation>,
    val location: Location,
    val checkInFrom: ZonedDateTime,
    val checkOutBefore: ZonedDateTime,
    val pricePerNight: Money
) : ItineraryItem() {
    val nights = Period.between(
        checkInFrom.toLocalDate(),
        checkOutBefore.toLocalDate()
    ).days
    val totalPrice: Money = pricePerNight * nights
}

data class Attraction(
    override val id: Id<Attraction>,
    val location: Location,
    val notes: String
) : ItineraryItem()

data class Journey(
    override val id: Id<Journey>,
    val travelMethod: TravelMethod,
    val departsFrom: Location,
    val departureTime: ZonedDateTime,
    val arrivesAt: Location,
    val arrivalTime: ZonedDateTime,
    val price: Money,
    val path: List<Position>,
) : ItineraryItem()

data class RestaurantBooking(
    override val id: Id<RestaurantBooking>,
    val location: Location,
    val time: ZonedDateTime
) : ItineraryItem()
```

1. ItineraryItem를 sealed class로 선언한다.
2. id를 봉인된 클래스의 추상 프로퍼티로 선언해 모든 하위 클래스가 식별자를 포함하게 강제한다.
3. ItineraryItem는 순수한 데이터 클래스들로만 이뤄진 봉인된 클래스이고, 필요한 연산은 각 모듈에 속한 확장 함수가 된다.

<br>

```kt
val ItineraryItem.costs: List<Money>
    get() = when (this) {
        is Accommodation -> listOf(totalPrice)
        is Attraction -> emptyList()
        is Journey -> listOf(price)
        is RestaurantBooking -> emptyList()
    }
```

타입 검사기도 이렇게 리팩토링 가능하다. <br>
이 방식이 컴파일러가 모든 타입을 제대로 처리하는지 검사해주기 때문에 훨씬 안전하다.








