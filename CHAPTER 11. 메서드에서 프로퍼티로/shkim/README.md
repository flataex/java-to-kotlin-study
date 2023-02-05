## 필드, 접근자, 프로퍼티

코틀린 언어는 필드 직접 접근을 지원하지 않는다. <br>
코틀린 설계자들이 이렇게 결정한 이유는 클라이언트에 영향을 끼치지 않고 표현을 변경할 수 있도록 접근자 사용을 권장하기 때문이다. <br>
접근자를 더 권장하기 위해 비공개 멤버 변수와 접근자를 한 프로퍼티 선언에 넣을 수 있게 해준다. <br>
따라서 자바에서는 필드에 직접 접근하게 허용할 수 있다.

아래의 선언은 givenName, familyName, dateOfBirth라는 비공개 필드를 선언하고 <br>
getXXX()의 접근자 메서드도 생성하며 <br>
모든 필드를 초기화하는 생성자도 생성한다.

```kt
data class PersonWithProperties(
    val givenName: String,
    val familyName: String,
    val dateOfBirth: LocalDate
){
    
}
```

<br>

## 계산된 프로퍼티 vs 메서드

**같은 타입에 속한 다른 프로퍼티에만 의존하고, 계산 비용이 비싸면 계산된 프로퍼티를 선택하라**

아래의 fullName같은 경우가 계산된 프로퍼티가 좋은 경우이다.
```kt
data class PersonWithProperties(
    val givenName: String,
    val familyName: String,
    val dateOfBirth: LocalDate
){
    val fullName get() = "$givenName $familyName"
}
```

<br>

사람의 나이의 경우는 호출된 시점에 따라 결과가 달라지므로 프로퍼티가 아니라 함수여야 한다. <br>
프로퍼티는 시간과 무관하고 입력에만 의존하는 계산이어야만 한다.
```kt
data class PersonWithProperties( 
    val givenName: String,
    val familyName: String,
    val dateOfBirth: LocalDate
){
    fun age() = Period.between(dateOfBirth, LocalDate.now()).years
}
```


