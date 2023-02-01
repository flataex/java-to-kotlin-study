# 다중식 함수에서 단일식 함수로

## 9.1 테이크 1: 인라이닝

<table>

<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>

<tr>

<td>

```kotlin
fun parse(value: String): EmailAddress {
    val atIndex = value.lastIndexOf("@")
    
    require(!(atIndex < 1 || atIndex == value.length - 1)) {
        "EmailAddress must be two parts separated by @"
    }
    
    return EmailAddress(
        value.substring(0, atIndex),
        value.substring(atIndex + 1)
    )
}
```
</td>

<td>

```kotlin
fun parse(value: String): EmailAddress {
    require(!(value.lastIndexOf("@") < 1 || value.lastIndexOf("@") == value.length - 1)) {
        "EmailAddress must be two parts separated by @"
    }
    
    return EmailAddress(
        value.substring(0, value.lastIndexOf("@")),
        value.substring(value.lastIndexOf("@") + 1)
    )
}
```
</td>
</tr>
</table>

## 9.2 테이크 2: 새 함수 도입하기

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td><td align="center">v3</td>
</tr>

<tr>

<td>

```kotlin
fun parse(value: String): EmailAddress {
    return emailAddress(value, value.lastIndexOf('@'))
}

private fun emailAddress(value: String, atIndex: Int): EmailAddress {
    require(!(atIndex < 1 || value.lastIndexOf("@") == value.length - 1)) {
        "EmailAddress must be two parts separated by @"
    }

    return EmailAddress(
        value.substring(0, value.lastIndexOf("@")),
        value.substring(value.lastIndexOf("@") + 1)
    )
}
```
</td>

<td>

```kotlin
fun parse(value: String): EmailAddress {
    return emailAddress(value, value.lastIndexOf('@'))
}

private fun emailAddress(value: String, atIndex: Int): EmailAddress {
    return if (atIndex < 1 || atIndex == value.length - 1) {
        throw IllegalArgumentException(
            "EmailAddress must be two parts separated by @"
        )
    } else {
        EmailAddress(
            value.substring(0, value.lastIndexOf("@")),
            value.substring(value.lastIndexOf("@") + 1)
        )
    }
}
```
</td>

<td>

```kotlin
fun parse(value: String) = 
    emailAddress(value, value.lastIndexOf('@'))

private fun emailAddress(value: String, atIndex: Int): EmailAddreess =
    when {
        atIndex < 1 || atIndex == value.length - 1 ->
            throw IllegalArgumentException(
                "EmailAddress must be two parts separated by @"
            )
        else -> EmailAddress(
            value.substring(0, value.lastIndexOf("@")),
            value.substring(value.lastIndexOf("@") + 1)
        )
    }
```
</td>
</tr>
</table>

## 9.3 테이크 3: let

> let이란,  매개변수화된 타입 T의 확장 함수<br>
> 자기 자신을 받아서 R을 반환하는((T) -> R) 람다 식을 입력으로 받고, 반환값 R을 반환

```kotlin
val person = Person("", 0)
val resultIt = person.let {
    it.name = "James"
    it.age = 56
    it // (T)->R 부분에서의 R에 해당하는 반환값.
}

val resultStr = person.let {
    it.name = "Steve"
    it.age = 59
    "{$name is $age}" // (T)->R 부분에서의 R에 해당하는 반환값.
}

// (T)->R 부분에서의 R에 해당하는 반환값 없음
val resultUnit = person.let {
    it.name = "Joe"
    it.age = 63
}

println("$resultIt")   // Person(name=James, age=56)
println("$resultStr")  // Steve is 59
println("$resultUnit") // kotlin.Unit
```

<table>

<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>

<tr>

<td>

```kotlin
fun parse(value: String): EmailAddress {
    val atIndex = value.lastIndexOf('@')
    
    return atIndex.let {
        require(!(atIndex < 1 || atIndex == value.length - 1)) {
            "EmailAddress must be two parts separated by @"
        }

        EmailAddress(
            value.substring(0, atIndex),
            value.substring(atIndex + 1)
        )
    }
}
```
</td>

<td>

```kotlin
fun parse(value: String): EmailAddress {
    value.lastIndexOf('@').let { atIndex -> 
        require(!(atIndex < 1 || atIndex == value.length - 1)) {
            "EmailAddress must be two parts separated by @"
        }

        EmailAddress(
            value.substring(0, atIndex),
            value.substring(atIndex + 1)
        )
    }
}
```
</td>
</tr>
</table>

## 9.4 테이크 4: 한걸음 물러서기

<table>

<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>

<tr>
<td>

```kotlin
fun parse(value: String): EmailAddress {
    val atIndex = value.lastIndexOf('@')

    require(!(atIndex < 1 || atIndex == value.length - 1)) {
        "EmailAddress must be two parts separated by @"
    }
    
    val leftPart = value.substring(0, atIndex)
    val rightPart = value.substring(atIndex + 1)
    
    return EmailAddress(
        leftPart,
        rightPart
    )
}
```
</td>

<td>

```kotlin
fun parse(value: String): EmailAddress {
    val (leftPart, rightPart) = split(value)
    
    return EmailAddress(
        leftPart,
        rightPart
    )
}

private fun split(value: String): Pair<String, String> {
    val atIndex = value.lastIndexOf('@')

    require(!(atIndex < 1 || atIndex == value.length - 1)) {
        "EmailAddress must be two parts separated by @"
    }

    val leftPart = value.substring(0, atIndex)
    val rightPart = value.substring(atIndex + 1)
    
    return Pair(leftPart, rightPart)
}
```
</td>
</tr>

<tr>
<td>

```kotlin
fun parse(value: String): EmailAddress {
    val (leftPart, rightPart) = value.split('@')

    return EmailAddress(
        leftPart,
        rightPart
    )
}

private fun String.split(divider: char): Pair<String, String> {
    val atIndex = value.lastIndexOf('@')

    require(!(atIndex < 1 || atIndex == value.length - 1)) {
        "EmailAddress must be two parts separated by @"
    }

    val leftPart = value.substring(0, atIndex)
    val rightPart = value.substring(atIndex + 1)

    return Pair(leftPart, rightPart)
}
```
</td>

<td>

```kotlin
fun parse(value: String): EmailAddress =
    value.split('@').let { (leftPart, rightPart) ->
        EmailAddress(leftPart, rightPart)
    }
```
</td>
</tr>
</table>