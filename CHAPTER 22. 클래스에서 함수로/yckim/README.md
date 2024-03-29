# 클래스에서 함수로

## 인수 테스트
- 테스트 코드를 먼저 작성하면 기능에 대한 명세를 빠르게 만들 수 있습니다.

## 단위 테스트
- 테스트 코드 작성시 빈 입력에 대한 테스트를 작성하는 것이 좋습니다.
- 테스트 코드를 작성할때는 다양한 상황에 대한 테스트를 작성하는 것이 좋습니다.

## 헤더
- 코드의 의존관계를 함수로 표현하면 사용자와 테스트 코드는 다양한 입출력에 대한 구현을 할 수 있게 됩니다.
    ```kotlin
    fun headerProviderFrom(header: String): (Int) -> String {
        val headers = header.splitFields(",")
        return { index -> headers[index] }
    }
    ```
## 다른 필드 구분자
- 함수 타입을 사용하면 문자열 분리, 인용, 이스케이프 등의 세부사항을 감출 수 있습니다.
    - 이를 통해 유연한 코드를 작성할 수 있습니다.

## 시퀀스
- List 대신 시퀀스를 사용하면 메모리를 효율적으로 사용할 수 있습니다.
- 코드를 읽을때 이해하기 쉽지 않다면 주석을 달거나 이해하기 쉬운 코드가 되도록 리팩터링하는 것이 좋습니다.
## 파일 읽기
- 테스트에서 패턴을 찾아서 함수로 표현하는 것은 테스트를 읽는 독자가 코드가 무슨일을 하는지 이해하는데 도움이 되고 테스트를 작성하는 개발자가 테스트가 미처 검사하지 못하는 조건을 찾는데도 도움이 됩니다.
- 설계 기법으로 TDD를 사용한다면 최종 테스트가 프로그램의 올바름을 검증하고 문서화를 제공하고 퇴행을 막는지 확인해야 합니다.

## 커먼즈 CSV와 비교
- 특정 프로퍼티를 제공하기 위해 타입 정의를 사용하면 기존 테스트가 깨지지 않는 상태에서 최소한의 수정만 진행할 수 있습니다.