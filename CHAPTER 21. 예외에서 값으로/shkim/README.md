```kt
fun Sequence<String>.toHighValueCustomerReport(): Sequence<String> {
    val valuablecustomers = this
        .withoutHeader() 
        .map(String::toCustomerData) 
        .filter { it.score >= 10 } 
        .sortedBy(CustomerData::score) 
        .toList()
    
    return sequenceOf("ID\tName\tSpend") +
            valuableCustomers.map(CustomerData::outputLine) +
            valuablecustomers.summarised()
}

private fun List<CustomerData>.summarised(): String =
    sumByDouble { it.spend }.let { total ->
        "\tTOTAL\t${total.toMoneyString()}" 
    }

private fun Sequence<String>.withoutHeader() = drop(1)

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

private val CustomerData.outputLine: String
    get() = "$id\t$marketingName\t${spend.toMoneyString()}"

private fun Double.toMoneyString() = this.formattedAs("%#.2f")

private fun Any?.formattedAs(format: String) = String.format(format, this)

private val CustomerData.marketingName: String
    get() = "${familyName.toUpperCase()}, $givenName"
```

<br>

위 코드에서, toCustomerData() 메서드는 오류가 발생할 여지가 있다. <br>
구분자를 split으로 분리하는데, 분리한 조각이 충분히 많지 않으면 IndexOutOfBoundsException이 발생할 수 있고, parts[3]이 Int가 아니거나 parts[4]가 Double이 아니면 NumberFormatException이 발생할 것이다. <br>

예측할 수 있는 오류가 발생했을 때 프로그램을 중단하기 위해 예외를 써서는 안된다. (코드가 실패할 수 있다고 알려줄 기회가 사라진다.) <br>
코드가 이런 예외를 던지면 호출하는 쪽에서는 구현 코드를 한 줄씩 보면서 오류 원인을 추론해야 한다.

예외를 던지지 않기로 하면 가장 값 싼 변경은 실패 시 null을 반환하는 것이다.

```kt
internal fun String.toCustomerData(): CustomerData? =
    split("\t").let { parts ->
        if (parts.size < 4)
            return null
        val score = parts[3].toInt0rNull() ?: 
            return null
        val spend = if (parts.size == 4) 0.0 else parts[4].toDouble0rNuU() ?:
            return null
        CustomerData(
            id = parts[0],
            givenName = parts[1],
            familyName = parts[2],
            score = score,
            spend = spend
        )
    }
```

이렇게 고친 후, toCustomerData()를 호출하는 쪽도 수정해준다.

```kt
fun Sequence<String>.toHighValueCustomerReport(): Sequence<String> {
    val valuablecustomers = this
        .withoutHeader() 
        .map(String::toCustomerData)
        .filterNotNull()
        .filter { it.score >= 10 } 
        .sortedBy(CustomerData::score) 
        .toList()
    
    return sequenceOf("ID\tName\tSpend") +
            valuableCustomers.map(CustomerData::outputLine) +
            valuablecustomers.summarised()
}
```

<br>
<hr>

이제 우리는 toCustomerData()가 실패할 수 있다는 사실을 알리고 어디서 실패할지도 알린다. <br>
그렇다면 왜 실패했는지 더 잘 알려줄 수는 없을까 ?

null이 될 수 있는 타입 대신 결과 타입을 사용하면 오류 발생 시 실패의 내용에 대해 더 자세히 알려줄 수 있다.

````kotlin
sealed class ParseFailure(open val line: String)

data class NotEnoughFieldsFailure(override val line: String) :
    ParseFailure(line)

data class ScoreIsNotAnIntFailure(override val line: String) :
    ParseFailure(line)

data class SpendIsNotADoubleFailure(override val line: String) :
    ParseFailure(line)

````

```kotlin
internal fun String.toCustomerData(): Result<CustomerData, ParseFailure> =
    split("\t").let { parts ->
        if (parts.size < 4)
            return Failure(NotEnoughFieldsFailure(this))
        val score = parts[3].toInt0rNull() ?: 
            return Failure(ScoreIsNotAnIntFailure(this))
        val spend = if (parts.size == 4) 0.0 else parts[4].toDouble0rNuU() ?:
            return Failure(SpendIsNotADoubleFailure(this))
        CustomerData(
            id = parts[0],
            givenName = parts[1],
            familyName = parts[2],
            score = score,
            spend = spend
        )
    }
```

```kt
fun Sequence<String>.toHighValueCustomerReport(
    onErrorLine: (ParseFailure) -> Unit = {}
): Sequence<String> {
    val valuablecustomers = this
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
            valuablecustomers.summarised()
}
```

<br>
<hr>

toHighValueCustomerReport()는 파일을 읽어서, 보고서를 생성하는 코드이다.

따라서 toHighValueCustomerReport()는 IOException이 발생하면서 실패할 수 있다. <br>
이에 대한 책임은 호출자에 있기 때문에, toHighValueCustomerReport()가 IOException으로 인해 실패할 수 있다는 사실을 알고 그 오류를 적절히 처리해야 한다.

```kotlin
fun main() {
    val statusCode = try {
        using( 
            System.'in'.reader(),
            System,out.writer(),
            System.err.writer()
        ) { reader, writer, error ->
            val errorLines = mutableListOf<ParseFailure>() 
            val reportLines = reader
                .asLineSequence() 
                .toHighValueCustomerReport {
                    errorLines += it 
                }
            if (errorLines.isEmpty()) { 
                reportLines.writeTo(writer) 
                0
            } else { 
                errorLines.writeTo(error) 
                -1
            } 
        }
    } catch (x: lOException) {
        System.err.println("I0 error processing report ${x.message}")
        -1
    }
    exitProcess(statusCode) 
}
```




