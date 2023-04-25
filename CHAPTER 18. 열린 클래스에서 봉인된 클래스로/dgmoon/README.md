# ch18. 열린 클래스에서 봉인된 클래스로

소프트웨어가 커져가면, 그에 따라 요구 사항이 늘어나고 새로운 데이터 타입이나 연산을 추가할 필요가 생긴다.

Itinerary 클래스는 14장 이후 점차 더 많은 유형의 아이템이 추가되면서, 각 아이템을 별도의 컬렉션에 저장하는 대신 ItinearyItem을 한 컬렉션으로 유지하기로 했다.

```kotlin
data class Itinerary(
	val id Id<Itinerary>,
	val items: List<Itineraryltem>
) : Iterable<ItineraryItem> by items
```

```kotlin
interface ItineraryItem {
val id: Id<ItineraryItem>
val description: String
val costs: List<Money>
val mapOverlay: MapOverlay
...
```

이렇게 다형성을 이용하면 Itinerary 클래스는 구체적인 타입에 의존하지 않아도 된다.(ItineraryItem ← Itinerary) 하지만, 새로운 기능을 추가할수록 ItineraryItem에 연산이 추가되면서 비대해지고 결합도를 증가시킨다.

`객체 지향 다형성`(자바)은 연산이 가끔 변경되는 데이터 타입의 변경가능성을, `봉인된 클래스`(17이 후 자바, 코틀린)는 연산이 가끔 변경되는 데이터 타입에 대한 연산의 변경가능성을 다루는데 적합하다.

```kotlin
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

위 코드 처럼 타입 검사를 할 때, 새로운 타입이 추가된다면 항상 위 코드를 고쳐야만 한다. 하지만 코틀린에서는 이 방법을 우회한다.

코틀린에서는 클래스를 조직화하고 런타임 검사를 더 안전하고 편리하게 해주는 `봉인된 클래스`가 있다. 봉인된 클래스는 직접적인 하위 클래스가 고정되어 있는 추상 클래스이다. 봉인된 클래스는 하위 클래스를 같은 컴파일 유닛과 패키지 안에 두는 제한을 두어 하위 클래스의 런타임 타입 검사가 봉인된 클래스의 하위 타입에 대해서만 처리한다는 점을 보장한다.

## 다형성을 쓸까 봉인된 클래스를 쓸까

코틀린을 사용해서 설계할 때 타입이 연산보다 더 자주 변경될 것으로 예상된다면 ‘객체 지향 다형성’이, 반대라면 ‘봉인된 클래스가 적합하다.

## 인터페이스를 봉인된 클래스로 변환하기

ItineraryItem 인터페이스의 구현체들을 인터페이스와 같은 파일 안으로 옮기고(IDE의 ‘클래스 이동’ 이용) 인터페이스 예약어를 sealed class로 변경한다. 기존 ItineraryItem 인터페이스의 추상 메서드들은 제거한다.(확장 함수로 변경할 것이기 때문)

```kotlin
sealed class ItineraryItem {
	abstract val id: Id<ItineraryItem> //Itinerary가 클래스이므로 추상 필드로 변경
}

data class Accommodation(
	override val id: Id<Accommodation>,
	val location: Location,
	val checkInFrom: ZonedDateTime,
	val checkOutBefore: ZonedDateTime,
	val pricePerNight: Money
) : Itineraryltem() {
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
) : Itineraryltem()

data class Journey(
	override val id: IckJourney〉,
	val travelMethod: TravelMethod,
	val departsFrom: Location,
	val departureTime: ZonedDateTime,
	val arrivesAt: Location,
	val arrivalTime: ZonedDateTime,
	val price: Money,
	val path: List<Position>,
	...
) : Itineraryltem()
data class RestaurantBooking(
	override val id : Id<RestaurantBooking>,
	val location: Location,
	val time : ZonedDateTime
) : Itineraryltem() 
```

이제 다형적 메서드를 사용하지 않고도 새로운 연산을 추가할 수 있다. 새로 추가되는 연산들은 각 모듈에 속한 확장 함수로 변환하게 된다. `when 식`을 사용해 구체적인 아이템 타입에 따라 안전하게 디스패치하는 `확장 함수`를 사용한다.

```kotlin
fun ItineraryItem.toCalendarEvent(): CalendarEvent? = when (this) {
	is Accommodation -> CalendarEvent(
		start = checkInFrom,
		end = checkOutBefore,
		description = description,
		alarms = ListOf(
			Alarm(checkInFrom, "Check in open"),
			Alarm(checkOutBefore.minusHours(l), "Check out")
		)
	)
	is Attraction -> null
	is Journey -> CalendarEvent(
		start = departureTime,
		end = arrivalTime,
		description = description,
		location = departsFrom,
		alarms = listOf(
			Alarm(departureTime.minusHours(1)))
	)
	is RestaurantBooking -> CalendarEvent(
		start = time,
		description= description,
		location = location,
		alarms = listOf(
			Alarm(time.minusHours(1)))
		)
	}
```
