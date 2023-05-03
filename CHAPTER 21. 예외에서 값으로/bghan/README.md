# 예외에서 값으로

## 21.1 잘못된 원인 파악하기

```kotlin
private val CustomerData.marketingName: String
    get() = "${familyName.toUpperCase()}, $givenName"
```

- `CustomerData`가 java로 구현됐다면 `familyName` null 가능하여 NPE 발생 가능
- 하지만 kotlin으로 구현돼 NPE 가능성 x

```kotlin
private fun Any?.formattedAs(format: String) = String.format(format, this)
```

- `String.format(format, this)` 은 format이 다른 입력과 맞아 떨어지지 않으면 `IllegalFormatException` 발생
- 매개변수가 취할 수 있는 값 중 일부만 결과 반환
- 이 코드에선 `format = %#.2f` 이고, 호출하는 유일한 코드는 `Double.toMoneyString()` 이기에 실패 x

```kotlin
private val CustomerData.outputLine: String
    get() = "$id\t$marketingName\t${spend.toMoneyString()}"
```

- 실패할 가능성이 없다고 추론한 코드만 호출
- `실패 전파성`에 따라 안전함

> 실패 전파성이란, f가 오류를 발생시킬 수 있고 g가 f를 호출하면 g도 f가 발생시키는 오류 발생 가능

```kotlin
internal fun String.toCustomerData(): CustomerData = 
    split("\t").let { parts -> 
        CustomerData(
            id = parts[0],
            givenName = parts[1],
            familyName = parts[2],
            score = parts[3].toInt(),
            spend = if (parts.size == 4) 0.0 else parts[4].toDouble()
        )
    }
```

- split을 통해 얻은 list element 개수가 3개 이하면 `IndexOutOfBoundsException` 발생 가능
- parts[3]이 Int를 표현하지 않거나, parts[4]가 Double을 표현하지 않으면 `NumberFormatException` 발생 가능
- 예외 던지기 대신엔 가장 적은 비용인 **null 반환**

```kotlin
internal fun String.toCustomerData(): CustomerData? =
    split("\t").let { parts ->
        if (parts.size < 4)
            null
        else 
            CustomerData(
                id = parts[0],
                givenName = parts[1],
                familyName = parts[2],
                score = parts[3].toInt(),
                spend = if (parts.size == 4) 0.0 else parts[4].toDouble()
            )
    }
```

- Int, Double로 변환할 수 없는 경우에도 예외 던짐
- 추가로 다른 메서드(= `toHighValueCustomerReport`) 깨짐

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
fun Sequence<String>.toHighValueCustomerReport(): Sequence<String> {
    val valuableCustomers = this
        .withoutHeader()
        .map(String::toCustomerData)        // null 반환 가능
        .filter { it.score >= 10 }          // it이 Null이 될 수 있어 컴파일 실패
        .sortedBy(CustomerData::score)
        .toList()
    
    return sequenceOf("ID\tName\tSpend") +
            valuableCustomers.map(CustomerData::outputLine) +
            valuableCustomers.summarised()
}
```
</td>
<td>

```kotlin
fun Sequence<String>.toHighValueCustomerReport(): Sequence<String> {
    val valuableCustomers = this
        .withoutHeader()
        .map(String::toCustomerData)        // null 반환 가능
        .filterNotNull()                    // null element 제거
        .filter { it.score >= 10 }          // it이 Null이 될 수 없음
        .sortedBy(CustomerData::score)
        .toList()
    
    return sequenceOf("ID\tName\tSpend") +
            valuableCustomers.map(CustomerData::outputLine) +
            valuableCustomers.summarised()
}
```
</td>
</tr>
</table>

- `filterNotNull`을 써서 잘못된 입력 줄을 무시
- 하지만 **오류가 발생한 경우를 제대로 처리**해야함

#### 오류 처리 방안 : 문제 수집 및 오류 보고 후 프로그램 중단

```kotlin
fun Sequence<String>.toHighValueCustomerReport(): Sequence<String> {
    val valuableCustomers = this
        .withoutHeader()
        .map{ line ->
            val customerData = line.toCustomerData()
            if (customerData == null)                   // 오류 수집
                onErrorLine(line)
            customerData
        }
        .filterNotNull()
        .filter { it.score >= 10 }
        .sortedBy(CustomerData::score)
        .toList()
    
    return sequenceOf("ID\tName\tSpend") +
            valuableCustomers.map(CustomerData::outputLine) +
            valuableCustomers.summarised()
}
```

## 21.2 오류 표현하기

> 오류가 실패할 수 있다는 **사실**, **어디서**, **왜** 실패했는지 명시

```kotlin
internal fun String.toCustomerData(): Result<CustomerData, ParseFailure> = 
    split("\t").let { parts ->
        if (parts.size < 4)
            return Failure(NotEnoughFieldsFailure(this))
        
        val score = parts[3].toIntOrNull() ?:
            return Failure(ScoreIsNotAnIntFailure(this))
        
        val spend = if (parts.size == 4) 0.0 else parts[4].toDoubleOrNull ?:
            return Failure(SpendIsNotADoubleFailure(this))
        
        Success(
            CustomerData(
                id = parts[0],
                givenName = parts[1],
                familyName = parts[2],
                score= score,
                spend = spend
            )
        )
    }
```

```kotlin
sealed class ParseFailure(open val line: String)

data class NotEnoughFieldsFailure(override val line: String) :
    ParseFailure(line)

data class ScoreIsNotAnIntFailure(override val line: String) :
    ParseFailure(line)

data class SpendIsNotADoubleFailure(override val line: String) :
    ParseFailure(line)
```

- `ParseFailure` 표현된 parsing error 원인 수집

```kotlin
fun Sequence<String>.toHighValueCustomerReport(
    onErrorLine: (ParseFailure) -> Unit = {}
): Sequence<String> {
    val valuableCustomers = this
        .withoutHeader()
        .map { line -> 
            line.toCustomerData().recover {
                onErrorLine(it)
                null
            }
        }
        .filterNotNull()
        .filter { it.score >= 10 }
        .sortedBy(CustomerData::score)
        .toList()
    
    return sequenceOf("ID\tName\tSpend") +
            valuableCustomers.map(CustomerData::outputLine) +
            valuableCustomers.summarised()
}
```

- `recover` 블록에서 null 반환하고 `filterNotNull`로 null 제외 시키는 과정 아쉬움
  - 동작 직접적 설명 x
  - 정상 처리 경로 이해 방해