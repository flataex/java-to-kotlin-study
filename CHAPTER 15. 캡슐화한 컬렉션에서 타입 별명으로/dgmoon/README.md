## ch.15 캡슐화한 컬렉션에서 타입 별명으로

자바의 컬렉션 인터페이스는 가변적이다. 하지만 `코틀린에서는 컬렉션을 불변인 값 타입으로 다룬다.`

공유된 컬렉션을 변경하면 많은 문제가 발생하기 쉽기 때문에 변경하지 않는 방법을 택할 수 있지만, 자바에서는 add와 set 메서드로 쉽게 컬렉션을 변경할 수 있다. 대신, 자바는 컬렉션을 그대로 공유하지 않는 안전한 방법을 택했다.

```kotlin
	public class Route {
		private final List<Journey> journeys;

		public Route(List<Journey> journeys) {
				this.journeys = journeys;
		}

		public int size() {
				return journeys.size();
		}

		public Journey get(int index) {
				return journeys.get(index);
		}

		public Location getDepartsFrom() {
				return get(0).getDepartsFrom();
		}

		public Location getArrivesAt() {
				return get(size() - l).getArrivesAt();
		}

		public Duration getDuration() {
				return Duration.between(
						get(0).getDepartureTime(),
						get(size() - l).getArrivalTime());

```

- 책에서 정의한 Route 클래스는 List처럼 작동하도록 구현한 클래스.(size(), get() 등의 메서드를 갖고 있음.) → `컬렉션 캡슐화`
- 이렇게 하면, List를 불변으로 만들고 Route와 관련된 메서드들을 발견하기 쉽게 만들 수 있다.
    - `하지만, 코틀린에서는 List를 불변리스트로 만들고 확장 함수를 사용하기만 하면 된다.`
- 컬렉션을 캡슐화할 경우, 컬렉션 안에 연산을 정의해야 해당 연산을 컬렉션의 내용에 적용할 수 있기 때문에 자꾸 클래스 안에 메서드를 추가하게 된다. 이렇게 메서드를 추가할수록 애플리케이션 다른 부분과 결합도가 커짐.

### 도메인 컬렉션 합성하기

컬렉션을 캡슐화하지 않고, 다른 컬렉션 API를 이용하여 필요한 연산을 정의할 수도 있음 → `그러나 정적 함수는 발견하기 어렵고, 자바 표준 라이브러리가 가변 컬렉션을 사용한다는 문제점 존재`

공유된 컬렉션을 쓰는 다른 이유는 컬렉션에 이름을 부여하고 다른 컬렉션과 구분할 수 있다. → `코틀린에서는 클래스 대신 타입 별명(type alias)를 사용해 해당 리스트 타입에 연관된 이름을 사용할 수 이다. (List<Journey>에 Route라는 별명을 사용 가능)`

```kotlin
typealias Route = List<Journey>
```

### 다른 프로퍼티가 있는 컬렉션

하지만, 캡슐화된 클래스 내에 다른 프로퍼티가 있는 경우 그냥 치환이 어려울 수도 있다. 이때는 해당 프로퍼티가 컬렉션을 구현하면, 캡슐화되지 않는 컬렉션의 장점을 취할 수 있다.

### 캡슐화된 컬렉션 리팩터링하기

기존 Route 클래스는 메서드가 너무 많고 다른 코드들과의 결합도를 높이고 있다. 

- 리팩터링 방안들
    - 연산을 확장으로 변환하기(확장 함수 사용)
        - 코틀린의 확장 함수는 자바의 메서드만큼 발견 가능성이 좋으므로 메서드의 좋은 대안이다.
    - 타입 별명 치환
        - Route가 List<Journey>를 감싸면서 코틀린 표준 라이브러리를 사용할 수 없게 만든다.
        - 게다가 List를 수정해야할 경우 감싼 클래스(Route)를 벗겨 내 연산을 수행한 후 다시 감싸야 하는 경우가 발생한다.
        - 이런 문제를 해결하기 위해 타입별명을 사용할 수 있다.
            
            ```kotlin
            typealias Route = List<Journey>
            
            val Route.departsFrom: Location
            	get() = first().departsFrom
            
            val Route.arrivesAt: Location
            	get() = last().arrivesAt
            
            val Route.duration: Duration
            	get() = Duration.between(
            		first().departureTime,
            		last().arrivalTime
            	)
            ```
            
    - 다른 프로퍼티와 함께 있는 컬렉션 리팩터링하기
        - 컬렉션과 다른 프로퍼티가 있으면 타입 별명을 사용할 수 없음
            
            ```kotlin
            class Itinerary(
            	val id: Id<Itinerary>,
            	val route: Route
            ) {
            	fun hasJourneyLongerThan(duration: Duration) =
            		route.any { it.duration > duration }
            }
            ```
            
        - Route를 위임으로 구현해서 Journeys를 직접 질의하면 됨
            - 위임한 인터페이스(Route)의 메서드 구현을 직접 제공함으로써 위임 객체의 동작을 오버라이드할수 있음
            
            ```kotlin
            data class Itinerary(
            	val id: Id<Itinerary>,
            	val route: Route
            ) : Route by route {
            	fun withTransformedRoute(transform: (Route).() -> Route) =
            		copy(route = transform(route))
            }
            ```
            
        - 기존 Itinerary에서 새 Itinerary를 생성하긴 어려운데 List<Journey>를 사용하면 되고 이때 데이터 클래스를 쓰면 또 도움이 된다.

가변 컬렉션을 캡슐화하는 클래스에서 메서드를 확장함수로 옮겨 확장 함수를 사용하는 곳으로 더 가깝게 옮기고 클래스 자체를 없애면서 타입 별명으로 대신할 수 있었다.

리스트 같은 내장 타입을 재사용하는 것도 비용이 들긴 한는 것을 명심해야 한다. 리스트는 너무 많은 곳에 쓰여서 사용된 코드를 찾는게 어렵다.
