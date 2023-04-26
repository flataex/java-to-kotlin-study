# 열린 클래스에서 봉인된 클래스로

- 소프트웨어가 계속 발전하면서 새로운 기능을 추가할 때 얼마나 설계를 잘했느냐에 따라 수정하는 코드의 양이 변합니다.
- 구체 타입이 아닌 인터페이스에 의존하면 쉽게 새로운 타입을 추가할 수 있다는 장점이 있습니다.
    - 하지만 새로운 기능이 추가될 때마다 해당 인터페이스가 점점 커진다는 문제점이 존재합니다.
- 하나의 인터페이스에 너무 많은 클래스가 의존하게 되면 다음과 같은 문제점이 존재합니다.
    - 협업시, 사용자들간의 병합충돌이 자주 발생합니다.
    - 인터페이스가 거대해지면서 여러 클래스들이 인터페이스에게 의존도가 높아지면서 결합도를 증가시킬 수 있습니다.
- 자바에서 instaceof를 이용하여 타입 검사를 할 수 있지만 모든 상황에 대해서 검사하기 위해서는 계속하여 분기처리를 해야하므로 실수하기 쉽습니다.
- 코틀린에서는 클래스를 조직화하고 런타임 타입 검사를 더 안전하고 편리하게 해주는 기능인 봉인된 클래스가 있습니다.
    - 정적 타입 검사기는 when 식이 수행하는 봉인된 클래스의 하위 타입에 대한 실행 시점 타입 검사가 모든 경우를 처리하고 그외의 경우를 처리하지 않는 다는 점을 보장합니다.

## 다형성을 쓸까 봉인된 클래스를 쓸까?
- 코틀린에서는 기존 코드를 변경하지 않고 타입과 연산을 변경할 수 없기 때문에 다형성이나 봉인된 클래스를 활용해야 합니다.
    - 데이터 타입에 적용할 수 있는 연산보다 데이터 타입이 더 자주 변경될 때 객체지향 다형성이 더 적합합니다.
    - 데이터 타입보다 데이터 타입에 적용할 수 있는 연산이 더 자주 변경될 때 봉인된 클래스 계층이 더 적합합니다.
- 봉인된 클래스는 다운 캐스트가 목적이므로 다운 캐스트로만 사용하는 것을 권장합니다.
## 인터페이스를 봉인된 클래스로 변환하기
- 인터페이스를 사용할경우 애플리케이션의 도메인의 핵심 클래스와 외부 모듈간의 결합이 발생하게 됩니다.
    - 이를 해결하기 위해 봉인된 클래스 계층과 독립적인 함수로 처리하도록 변경할 수 있습니다.

    ```kotlin
    sealed class ItineraryItem {
    	abstract val id: Id<ItineraryItem>
    }
    
    val ItineraryItem.costs: List<Money>
        get() = when (this) {
            is Accommodation -> costs
            is Attraction -> costs
            is Journey -> costs
            is RestaurantBooking -> costs
        }
    
    val ItineraryItem.description: String
        get() = when (this) {
            is Accommodation -> description
            is Attraction -> description
            is Journey -> description
            is RestaurantBooking -> description
        }
    
    val ItineraryItem.mapOverlay: MapOverlay
        get() = when (this) {
            is Accommodation -> mapOverlay
            is Attraction -> mapOverlay
            is Journey -> mapOverlay
            is RestaurantBooking -> mapOverlay
        }
    
    data class Accommodation(
        override val id: Id<Accommodation>,
        val location: Location,
        val checkInFrom: ZonedDateTime,
        val checkOutBefore: ZonedDateTime,
        val pricePerNight: Money
    ) : ItineraryItem() {
        val nights = Period.between(checkInFrom.toLocalDate(), checkOutBefore.toLocalDate()).days
        val totalPrice: Money = pricePerNight * nights
    }
    ```
  
## 정리
- 코틀린은 자바와 마찬가지로 객체지향의 다형성을 활용해서 기존 함수의 코드를 변경하지 않고도 쉽게 새로운 타입을 추가할 수 있습니다.
- 봉인된 클래스와 안전한 실행 시점 타입 검사를 활용하면 기존 데이터 타입의 코드를 변경하지 않고도 기존 데이터 타입에 대해 적용할 수 있는 새로운 함수를 추가할 수 있습니다.
- 데이터 타입이 많이 변화한다면 인터페이스, 데이터 타입의 연산이 많이 변경된다면 봉인된 클래스를 사용하는 것이 좋습니다.