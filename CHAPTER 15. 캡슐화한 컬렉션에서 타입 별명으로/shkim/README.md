```java
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
        return get(size() - 1).getArrivesAt();
    }

    public Duration getDuration() {
        return Duration.between(
                get(0).getDepartureTimeQ,
                get(size() - 1).getArrivalTime());
    }
}
```

위 클래스는 다음과 같은 역할들을 한다.

1. Route는 Journey의 List를 캡슐화한다.
2. 원 데이터가 생성자에 전달된다.
3. UI 표시 등을 위한, 데이터에 대한 접근은 size와 get 메서드에 따라 제공된다.
4. Route 클래스는 캡슐화한 리스트의 내용을 사용하는 애플리케이션 로직을 구현한다.

하지만 Route는 List<Journey>의 가치를 떨어뜨린다.

## 도메인 컬렉션 합성하기

다른 클래스 경계 안쪽에 도메인 모델의 데이터 구조를 감추는 대신에 도메인 모델이 적절한 데이터 구조가 되도록 한다면 <br>
도메인 데이터에 사용할 수 있는 연산의 종류가 확장된다.

클라이언트 코드는 컬렉션을 캡슐화한 도메인 클래스 안에 연산을 정의하지 않고 풍부한 컬렉션 API를 통해 필요한 연산을 정의할 수 있다.

여러 가지 List<Journey>의 유형을 구분하는 게 중요하지 않은 경우 코틀린은 클래스 대신 타입 별명을 사용해 Route라는 이름이 List<Journey>와 연관되게 해준다. <br>
코틀린에서 이렇게 타입 별명을 지정하면 컬렉션을 도메인 타입으로 사용하기 위한 장애물이 사라진다.

```kt
typealias Route = List<Journey>
```

<br>

## 다른 프로퍼티가 있는 컬렉션

항상 타입 별명으로 클래스를 대신할 수는 없다.

Itinerary에는 Id 타입의 프로퍼티가 들어있는데, 이런 경우 이 클래스를 자신 내부에 있는 컬렉션으로 그냥 치환할 수 없다. 

```kt
class Itinerary(
    val id: Id<Itinerary>, 
    val route: Route
){
    
}
```

## 연산을 확장으로

```kt
class Route(
    val journeys: List<Journey>
)

val Route.duration: Duration
    get() = Duration.between(
        get(0).getDepartureTimeQ,
        get(size() - 1).getArrivalTime()
    )

val Route.arrivesAt: Location
    get() = get(size() - 1).getArrivesAt()

operator fun Route.get(index: Int) = journeys[index]

fun Route.size(): Int = journeys.size()

val Route.departsFrom: Location
    get() = get(0).getDepartsFrom()
```

이제 모든 Route 연산은 확장 함수가 되었다. <br>
이젠 결합을 줄이기 위해 아예 다른 모듈로 이동시킬 수도 있다.

## 타입 별명 치환

List를 감싸는 클래스는 나쁜 클래스이다. <br>
감싸는 클래스로 인해 코틀린 표준 라이브러리가 제공하는 기능을 사용할 수 없기 때문이다.

우리는 Route가 Journey의 List를 가지기를 원하지 않는다. Route가 Journey의 List이기를 원한다. <br>
혹은 Journey의 List가 Route가 되기를 원할 수도 있다.

이런 경우 Route와 List<Journey> 가 같은 타입이라고 선언하는 타입 별명을 사용할 수 있다.

```kt
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

fun <T> Iterable<T>.withItemAt(index: Int, replacedBy: T): List<T> =
    this.toMutableList().apply {
        this[index] = replacedBy
    }
```

<br>

이렇게 하면 코틀린에서 Route를 쓰는 코드는 그냥 list의 연산일 뿐이다.

```kt
val route = listOf(journey1, journey2, journey3) 
assertEquals(
    listOf(journey1, replacement, journey3),
    route.withItemAt(1, replacement) 
)
```

<br>

## 다른 프로퍼티와 함께 있는 컬렉션 리팩토링하기

```kt
class Itinerary(
    val id: Id<Itinerary>, 
    val route: Route
){
    
}
```

<br>

이런 경우는 Route를 위임으로 구현해서 Journeys를 직접 질의할 수 있다는 이점을 이용하자 <br>
이제 Itinerary를 Route처럼 다룰 수 있다.


*by route는 Itinerary 객체가 생성자 인자로 받은 route 객체에 Route 인터페이스의 모든 메서드를 전달한다고 선언하는 것*

```kt
class Itinerary(
    val id: Id<Itinerary>, 
    val route: Route
) : Route by route {
    
}
```


