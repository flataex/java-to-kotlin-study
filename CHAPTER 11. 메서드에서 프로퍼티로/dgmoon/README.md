# ch11. 메서드에서 프로퍼티로

자바에서는 직접 필드에 접근하는 대신에 보통 접근자 메서드를 작성한다.

코틀린에서는 접근자 메서드만 지원하고, 필드 직접 접근을 지원하지 않는다.(자바 클래스에서는 공개 필드에 접근하는 코드를 생성하지만, 코틀린 컴파일러는 공개 필드를 직접 정의하지 않는다.)

자바의 필드와 접근자
```java
public class PersonWithAccessor {
    private final String givenName;
    private final String familyName;
    private final LocalDate dateOfBirth;

    public PersonWithAccessor {
        ...
    }

    public String getGivenName() {
        ...
    }

    public String getFamilyname() {
        ...
    }

    public LocalDate getDateOfBirth() {
        ...
    }
}
```

코틀린의 프로퍼티(비공개 필드, 접근자 메서드, AllArgsConstructor 생성)
```kotlin
data class PersonWithProperties(
    private final String givenName;
    private final String familyName;
    private final LocalDate dateOfBirth;
) {
}
```

계산된 프로퍼티는 필드로 뒷받침되지 않는 프로퍼티를 말한다. 코틀린에서는 생성자 밖에서 계산된 프로퍼티를 정의한다.

```kotlin
data class PersonWithProperties(
    private final String givenName;
    private final String familyName;
    private final LocalDate dateOfBirth;
) {
    val fullName get() = "$givenName $familyName"
}
```

코틀린에서 프로퍼티를 정의하면 컴파일러가 자바의 명명 관습에 따라 필드와 접근자를 생성해준다.

프로퍼티를 선택할지 메서드를 선택할지에 대한 대략적인 규칙은 같은 타입에 속한 다른 프로퍼티에만 의존하고 계산 비용이 싼 경우에는 계산된 프로퍼티를 택하라는 것이다.

가변 프로퍼티(var)는 사용할 수 있지만 사용하는 경우는 아주 드물고, 코틀린에서는 불변 데이터를 더 선호한다.
