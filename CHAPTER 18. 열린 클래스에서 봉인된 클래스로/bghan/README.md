# 열린 클래스에서 봉인된 클래스로

## 18.2 인터페이스를 [봉인된 클래스](https://kotlinworld.com/165)로 변환하기

```kotlin

import java.time.Period
import java.time.ZonedDateTime
import javax.xml.stream.Location

sealed class ItineraryItem {
    abstract val id: Id<ItineraryItem>
    abstract val description: String
    abstract val costs: List<Money>
    abstract val mapOverlay: MapOverlay
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
    
    override val description
        get() = "$nights nights at ${location.userReadableName}"
    
    override val costs
        get() = listOf(totalPrice)
    
    override val mapOverlay
        get() = PointOverlay(
            id = id,
            position = location.position,
            text = location.userReadableName,
            icon = StandardIcons.HOTEL
        )
}
```

- `Iteinerary Item`을 변경하지 않고 새로운 연산 추가 가능
- when 절을 통해 구체적인 타입에 따라 안전하게 추가

```kotlin
val ItineraryItem.mapOverlay: MapOverlay get() = when (this) {
    is Accommodation -> mapOverlay
    is Attraction -> mapOverlay
    is Journey -> mapOverlay
    is RestaurantBooking -> mapOverlay
}
```

- when 절은 `ItineraryItem` 모든 하위 클래스를 검사해야만 컴파일 가능

```kotlin
val ItineraryItem.description: String
    get() = when (this) {
        is Accommodation -> "$nights nights at ${location.userReadableName}"
        is Attraction -> location.userReadableName
        is Journey -> "${departsFrom.userReadableName} " +
                "to ${arrivesAt.userReadableName} " +
                "by ${travelMethod.userReadableName}"
        is RestaurantBooking -> location.userReadableName
    }
```

```kotlin

import java.time.Periodimport java.time.ZonedDateTime
import javax.xml.stream.Location

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
```

- `ItineraryItem` 모델은 순수한 데이터 클래스로 이루어진 `sealed class`
- 각 필요한 연산은 각 특징에 대한 모듈에 속한 확장 함수