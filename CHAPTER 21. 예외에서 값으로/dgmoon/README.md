# ch.21 예외에서 값으로

오류를 제대로 보고하는 방법을 찾는다.

## 잘못된 원인 파악하기

```kotlin
fun Sequence<String>.toHighValueCustomerReport(): Sequence<String> {
	val valuablecustomers = this
		.withoutHeader()
		.map(String::toCustomerData)
		.filter { it.score >= 10 }
		.sortedBy(CustomerData::score)
		.toList()
	return sequenceOf(”ID\tName\tSpend") +
		valuableCustomers.map(CustomerData::outputLine) +
		valuableCustomers.summarised()
}

private fun List<CustomerData>.summarised(): String =
	sumByDouble { it.spend }.let { total ->
		"\tTOTAL\t${total.toMoneyString()}"
}

private fun Sequence<String>.withoutHeader() = drop(l)

internal fun String.toCustomerData(): CustomerData =
	split("\t").let { parts ->
		CustomerData(
			id = parts[0], // split으로 분리한 조각이 적으면, IndexOutOfBoundException 가능성
			givenName = parts[l],
			familyName = parts[2],
			score = parts[3].tolnt(), // parts[3]이 int 표현이 아니면 NumberFormatException 가능성
			spend = if (parts.size == 4) 0.0 else parts[4].toDouble() // parts[4]이 double 표현이 아니면 NumberFormatException 가능성
		)
}

private val CustomerData.outputLine: String
	get() = "$id\t$marketingName\t${spend.toMoneyString()}" // 아래 함수들이 실패 가능성이 없기 때문에 이 코드도 안전하다.(실패의 전파성)

private fun Double.toMoneyString() = this.formattedAs("%#.2f")

private fun Any?.formattedAs(format: String) = String.format(format, this) // 파라미터가 "%#.2f"로, 정상 문자열이므로 실패하지 않는다.

private val CustomerData.marketingName: String
	get() = "${familyName.toUpperCase()}, $givenName" // familyName은 null-safe하다.
```

오류를 완벽하게 처리하려면, 가장 먼저 잘못이 될 수 있는 요소를 알아내야 한다. 코틀린은 힌트를 제공하는 자바의 체크 예외가 존재하지 않는다.

두 언어 모두 자신이 실패할 방법을 외부에 알려주는 형태로 작성되지 않았다면, 직관과 경험에 의지해야 한다.

예측할 수 있는 오류 발생시, 예외를 사용하면 코드가 실패할 수 있다고 알려줄 기회가 사라지므로 예외를 사용하면 안 된다.(코틀린에 체크 예외가 없는 이유) 예외를 던지지 않는 다면, 가장 간단하게 변경할 수 있는 방법은 실패 시 null을 반환하는 것이다. toCustomer()를 다음과 같이 변경하였다. try-catch 구문을 사용하는 것보다 오류에 대해 능동적으로 대응하였다. 그러나 여전히 int나 double로 변환 불가인 경우에는 예외가 발생한다.

```kotlin
internal fun String.toCustomerData(): CustomerData? =
	split("\t").let { parts ->
		if (parts.size < 4)
			null
		else
			CustomerData(
			id = parts[0],
			givenName = parts[l],
			familyName = parts[2],
			score = parts[3].tolnt(),
			spend = if (parts.size = 4) 0.0 else parts[4].toDouble()
		)
}
```

이렇게 변경하면서 toCustomerData()는 null을 반환할 수 있게 되었고, toHighValue/CustomerReport()에서 it이 nullable 해지면서 컴파일이 안 되게 되었으므로 수정이 필요하다.

아래 코드에서 filterNotNull을 사용하도록 수정하였다.

```kotlin
fun Sequence<String>.toHighValueCustomerReport(): Sequence<String> {
val valuablecustomers = this
		.withoutHeader()
		.map(String::toCustomerData)
		.filterNotNull()
		.filter { it.score >= 10 }
		.sortedBy(CustomerData::score)
		.toList()
	return sequenceOf("ID\tName\tSpend") +
		valuablecustomers.map(CustomerData::outputLine) +
		valuableCustomers.summarised()
}
```

코틀린 표준 라이브러리의 String::toXXXOrNull()을 사용하여 발생가능성이 있는 모든 오류를 null로 표현한다. 

```kotlin
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
		givenName = parts[l],
		familyName = parts[2],
		score = score,
		spend = spend
	)
}
```

main()에서는 오류 발생 시 System.err를 통해 오류를 출력하고 프로그램을 끝낸다.

```kotlin
fun main() {
	System.'in'.reader().use { reader ->
		System.out.writer().use { writer ->
		val errorLines = mutableListOf<String>()
		val reportLines = reader
			.asLineSequence()
			.toHighValueCustomerReport {
				errorLines += it
			}
			if (errorLines.isNotEmpty()) {
				System.err.writer().use { error ->
				error.appendLine("Lines with errors")
				errorLines.asSequence().writeTo(error)
			}
				exitProcess(-l)
			} else {
				reportLines.writeTo(writer)
			}
		}
	}
}
```

## 오류 표현하기

왜 실패했는지 알려주기 위한 방법을 설명한다.

null 대신 결과 타입을 사용하면 오류 발생 시 어떤 실패인지 자세히 알려줄 수 있다. (toCustomerData()의 리턴 타입을 null→ Result로)

```kotlin
internal fun String.toCustomerData(): Result<CustomerData, ParseFailure> =
	split("\t").let { parts ->
	if (parts.size < 4)
		return Failure(NotEnoughFieldsFailure(this))
	val score = parts[3].toIntOrNuU() ?:
		return Failure(ScoreIsl\lotAnIntFailure(this))
	val spend = if (parts.size == 4) 0.0 else parts[4].toDouble0rNull() ?:
		return Failure(SpendIsNotADoubleFailure(this))
		Success(
			CustomerData(
				id = parts[0],
				givenName = parts[l],
				familyName = parts[2],
				score = score,
				spend = spend
			)
		)
}
```

파싱이 실패한 원인을 표현하는 봉인된 클래스를 만든다.

```kotlin
sealed class ParseFailure(open val line： String)
data class NotEnoughFieldsFailure(override val line: String) :
	ParseFailure(line)
data class ScoreIsNotAnIntFailure(override val line: String) :
	ParseFailure(line)
data class SpendIsNotADoubleFailure(override val line: String) :
	ParseFailure(line)
```

```kotlin
fun Sequence<String>.toHighValueCustomerReport(
	onErrorLine: (ParseFailure) -> Unit = {}
): Sequence<String> {
	val valuablecustomers = this
		.withoutHeader()
		.map { line ->
			line.toCustomerData().recover { // onErrorLine()을 호출하여 에러를 출력하고 null을 리턴한다.
				onErrorLine(it)
				null
			}
		}
		.filterNotNull()
		.filter { it.score >= 10 }
		.sortedBy(CustomerData::score)
		.toList()
	return sequenceOf("ID\tName\tSpend") +
		valuablecustomers.map(CustomerData::outputLine) +
		vaLuableCustomers.summarised()
}
```

오류를 다르게 처리하기 위해 ParseFailure의 실행 시점 타입을 사용하지는 않았지만, 오류 메시지에타입 이름을 사용했기에 약간의 시간적 이득을 얻을 수 있었다. 또한, 봉인된 클래스에 when 식을 사용해 실패 유형을 구분할 수도 있었다.

진정한 함수형 오류 처리는 flatMap 연쇄를 사용한다. 그러나 들여쓰기가 너무 깊다.

```kotlin
internal fun String.toCustomerData(): Result<CustomerData, ParseFailure> =
	split("\t").let { parts ->
		parts
			.takeUnless { it.size < 4 }
			.asResultOr { NotEnoughFieldsFailure(this) }
			.flatMap { parts ->
				parts[3].toInt0rNull()
					.asResultOr { ScorelsNotAnlntFailure(this) }
					.flatMap { score: Int ->
						(if (parts.size == 4) 0.0
						else parts[4].toDoubleOrNull())
							.asResultOr { SpendlsNotADoubleFailure(this) }
							.flatMap { spend ->
								Success(
									CustomerData(
										id = parts[0],
										givenName = parts[l],
										familyName = parts[2],
										score = score,
										spend = spend
									)
							)
					}
			}
	}
}
```

## I/O를 어떻게 처리할까?

I/O 오류에 대해 생각해본다.

List를 도입하고 나서 Sequence를 도입 했기에 쓰기 실패 가능성은 없다. 실제 출력을 하는 것은 보고서 생성 코드를 호출하고 있는 쪽에 책임이 있기 때문이다. 한편, 읽을 때는 Sequence의 각 String을 이터레이션하고 이 문자열들은 프로덕션 시에 파일에서 가져오기 때문에 Sequence 연산이 IOException이 발생하여 실패할수 있다.

toHighValueCustomerReport()가 IOException으로 실패할 수 있으며, 이에 대한 책임은 main 함수에 있으므로 main 함수에 오류 처리 코드를 추가한다.

```kotlin
fun main() {
	val statusCode = try {
		using(
			System.'in'.reader().
			System, out. writerQ,
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
		System.err.println("I0 error processing report ${x.message}”)
		-1
	}
	exitProcess(statusCode)
}
```
