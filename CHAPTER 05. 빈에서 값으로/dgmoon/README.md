# ch5.  빈에서 값으로

## 빈
자바 빈을 정의하려면 디폴트 생성자, 게터와 세터를 제공해야 한다.(serializable하기도 해야 함)

자바빈즈가 도입될 때 개발자들은 대부분의 객체가 가변일 것이라고 생각했다.

## 값
어떤 객체가 값 의미론을 따른다는 말은 객체의 상호작용에 있어 정체성(동일성)이 아니라 내부의 값만 중요하다는 말이다. 자바 원시 타입 값은 모두 값 의미론을 따른다.하지만 객체는 값의미론을 따를 수도 있고 그렇지 않을 수도 있다. 특히, 가변 객체는 값 의미론을 따르지 않는다.

책에서는, 값을 불변의 데이터 조각으로, 값 타입은 이런 불변 데이터 조각의 동작을 정의하는 타입으로 정의한다.

자바빈즈 빈은 값이 아니다. UI 컴포넌트가 단순하 데이터만이 아니기 때문이다.

POJO는 값이 아니다. POJO는 불변일 수도 있고 값 의미론을 따를 수 있지만 여전히 가변 POJO가 기본이기 때문이다.

## 값을 선호해야만 하는 이유는 무엇인가?
- 맵의 키나 집합원소로 불변 객체를 넣을 수 있다.

- 불변 객체의 불변 컬렉션에 대해 이터레이션하는 경우 원소가 달라질지 염려할 필요가 없다.

- 초기 상태를 깊이 복사하지 않고도 다양한 시나리오를 탐험할 수 있다.

- 여러 스레드에서 불변 객체를 안전하게 공유할 수 있다.

## 빈을 값으로 리팩터링하기
예제에서는 빈이나 POJO를 값으로 리팩터링한다.

@JvmOverloads 애너테이션은 컴파일러가 greeting, locale, currency의 디폴트 값을 서로 조합한 여러 생성자를 만들어 내도록 지정한다.

## 다음으로 나아가기
예제는 가변 객체에 대한 불변 참조를 불변 객체에 대한 가변 참조로 바꿈으로써, 상태 변경을 애플리케이션 진입점이나 이벤트 핸들러로 이동시키는 방법을 보여줬다. 이렇게 하면 우리 코드에서 가변성으로 인한 영향이나 복잡성을 처리해야 하는 부분을 줄일 수 있다.

자바빈즈는 UI 프레임워크에서 사용하기 위해 만들어졌으며, UI는 가변 객체의 마지막 보루다. 생명주기 관련 요구사항이 더 엄격할 경우 공유객체나 변경 이벤트보다 불변 객체를 선호할 수도 있다.
