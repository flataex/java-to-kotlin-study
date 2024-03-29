# ch4. 옵셔널에서 널이 될 수 있는 타입으로

## 없음을 표현하기

자바에서는 자바 8 이전까지 nullability를 관습이나 문서, 직관에 의존했고 최근 수년간 표시가 없으면 nullable하지 않다고 간주해왔다. 또한 어떤 코드는 @Nullable이나 @NotNullable 애너테이션을 사용하여 코드 검증 도구의 도움을 받기도 해왔다. 자바 8에서 Optional 타입이 도입된 것도 중요한 사실이다. 자바는 널이라는 무거운 짐에 시달리고 있고 Optional에도 한계가 있다.

반면에, 코틀린에서는 널을 포용한다. 선택 가능성을 시스템의 일부분으로 넣어 없음을 의미하는 값을 일관성 있게 다룰 수 있다. 하지만 Map<K, V>의 key에 해당하는 value가 없는 경우에는 null, List<T>.get(index)의 index에 해당하는 값이 없을 경우에는 IOOB 예외를 반환하는 등 자바와의 호환성을 유지하려는 점 때문에 완벽하지는 않다.

그래도 일반적으로는 코틀린으로만 작성된 API와 같이 1급 nullability에 대해 알게 된 후, 그것을 지원하지 않는 언어로 돌아오면 불안감을 느끼게 될 것이다. 

함수형 프로그래머들은 코틀린의 nullability 대신 Optional 타입(또는 Maybe 타입)을 사용하라고 권장한다. Optional 타입을 쓰면 잠재적 부재, 오류, 비동기성 등 모두 똑같은 도구를 사용해 처리할 수 있기는 하지만, 그럴 경우 코틀린이 nullability를 지원하기 위해 구체적으로 설계된 언어 기능을 쓰지 못한다는 문제점이 있다.

선택 가능성을 표현하는 래퍼 타입을 사용하지 말아야 하는 이유는 T는 T?의 하위 타입이므로 널이 될 수 없는 String 타입의 값을 널이 될 수 있는 String이 필요한 곳에 항상 쓸 수 있다. 반대로 T는 Optional<T>의 하위 타입이 아니므로 그럴 수 없기 때문이다. nullability를 사용하면 Optional인 값을 Optional이 아닌 값으로 쉽게 변경할 수 있지만, Optional을 사용하면 이런 변경이 쉽지 않다.

## 옵셔널에서 널 가능성으로 리팩터링하기
함수에서 널이 될 수 없는 파라미터를 지정하면 컴파일러가 함수 본문이전에 널 검사를 추가해준다.

코틀린 for 루프는 : 대신 in을 쓴다.

코틀린에서 Optional.elseThrow()는 !!와 같다.

## 코틀린다운 코드로 리팩터링하기
좌변이 null이 아니면 좌변 값을 돌려주고 null이면 우변값을 돌려주는 엘비스 연산자 ?:를 사용할 수 있다.

또한, ?.을 사용하면 수신 객체가 null이면 null로 평가되고, 그렇지 않으면 그대로 필드를 반환한다.(?.leg의 경우 ?가 null이면 null, ?이 null이 아니면 ?.leg 반환)

## 다음으로 나아가기
코틀린은 부재를 처리해야 할 때 확실히 책임을 질 수 있도록 해주고 부재를 처리하지 않아도 될때 부재를 처리하느라 짓눌릴 필요가 없게 해준다. 10장에서 널이 될 수 있는 타입과 안전한 호출, 엘비스 연산자, 확장 함수 등의 기능을 조합하는 방법을 살펴본다.
