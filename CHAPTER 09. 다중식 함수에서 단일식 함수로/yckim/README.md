# 다중식 함수에서 단일식 함수로

## 들어가면서
- 다중식 함수보다는 단일식 함수로 사용하는 것이 가독성 측면에서 더 좋습니다.
- 동작 함수의 경우에는 단일식 함수로 작성하는 것이 어색하고 그 동작과 다른 부수 효과가 있을 수 있기 때문에 위험합니다.
- 단일식 형태를 계산에만 사용하면 가독성과 부수효과로 인해 발생할 문제들이 발생하지 않아 쉽게 리팩토링할 수 있습니다.

## 테이크 1 : 인라이닝
- 코틀린에서는 대입이 문으로 분류됩니다.
- 인라이닝을 이용해서 문을 식으로 변경하더라도 다른 문들로 인해 변경되지 않으며, 오히려 코드가 이해하기 어려워지게 되었습니다.

## 테이크 2 : 새 함수 도입하기
- 리팩터링을 하다 교착상태에 빠질때 문제의 원인을 인라이닝 하여 분리하면 다음 리팩터링을 진행할 수도 있습니다.
- 리팩토링은 항상 성공적일 수 없으며 다양한 방법을 시도해봐야 합니다.

## 테이크 3 : let
- let을 이용하여 단일식 함수를 만들 수 있습니다.

## 테이크 4 : 한 걸음 물러서기
- 리팩터링시 다양한 시도를 해보는 것이 중요하며 리팩토링 후 이전 코드보다 확실히 이점이 있는지 생각해보아야 합니다.
- 실패시 throw보다는 null을 반환하는 식으로 코드를 작성하면 더 쉽게 리팩터링을 진행할 수 있습니다.
    - 현재 상황에서는 자바를 코틀린으로 변환시킨 것이기 때문에 throw를 사용했지만 처음부터 코틀린으로 작성한다면 `Type?` 형태로 반환하면 된다.