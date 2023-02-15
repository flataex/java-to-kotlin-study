## 이터러블

```kt
fun averageNonBlankLength(strings: List<String>): Double = 
    (strings
        .filter { it.isNotBlank() }
        .map(String::length)
        .sum()
        / strings.size.toDouble())
```

코틀린 filter와 map은 List를 반환하므로 이 식은 메모리에 2개의 리스트를 추가로 만든다. <br>
성능에 신경을 쓴다면 리스트를 채워넣기 위한 시간과 유지하기 위한 메모리가 필요하기 때문에 문제가 될 수 있다.

하지만 이 코틀린 코드는 컬렉션 크기가 크지 않다면 빠르게 작동한다. <br>
컬렉션이 크다면 코틀린에서는 시퀀스로 전환할 수 있다.

<br>

## 코틀린 시퀀스

```kt
fun averageNonBlankLength(strings: List<String>): Double = 
    (strings
        .asSequence()
        .filter { it.isNotBlank() }
        .map(String::length)
        .sum()
        / strings.size.toDouble())
```

코틀린 시퀀스는 자바 스트림과 같은 지연 계산을 제공한다. <br>
map은 다른 시퀀스를 반환하고, 최종 연산은 파이프라인 평가가 요구할 때 이뤄진다.

averageNonBlankLength의 시퀀스 버전에서는 각 단계의 중간 결과를 저장하기 위한 중간 리스트 저장 비용이 들지 않는다. <br>
하지만 원소 갯수가 적다면 파이프라인을 만들고 실행하는 비용이 리스트를 생성하는 비용보다 비싸진다.

<br>

## 스트림, 이터러블, 시퀀스 사이에 선택하기

지연 계산이 필요한 경우

- 입력을 읽는 작업을 다 끝내기 전에 결과를 얻어야 할 필요가 있다.
- (중간 결과를 포함해) 메모리 용량보다 더 큰 데이터를 처리해야 할 필요가 있다.
- 파이프라인 단계가 긴 큰 컬렉션. (중간 단계의 컬렉션을 만들어내는 과정이 느릴 수 있다.)
- 파이프라인 뒤쪽 단계에서만 얻을 수 있는 정보를 활용해 파이프라인의 앞쪽 단계에서 원소 중 일부를 건너뛸 수 있는 경우.

<br>




