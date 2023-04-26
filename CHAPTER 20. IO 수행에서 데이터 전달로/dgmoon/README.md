# ch20. I/O 수행에서 전달로

## 테스트에 귀 기울이기

```kotlin
private fun check(
		inputLines: List<String>,
		expectedLines: List<String>
) {
	val output = StringWriter()
	generate(
		StringReader(inputLines.joinToString("\n")),
		output
	)
	assertEquals(expectedLines.joinToString("\n"), output.toString()
}
```

check 함수는 계산이 아니다. 하지만 코드를 아래와 같이 바꾸면, inputLines를 가져와서 outputLines를 만들어내는 계산을 볼 수  있고, generate의 경우 check 함수의 동작의 부수효과가 미치는 영역을 지역 변수로 제한하여 계산으로 만들었다.

```kotlin
private fun check(
	inputLines: List<String>,
	expectedLines: List<String>
) {
	val output = StringWriter()
	val reader = StringReader(inputLines.joinToString("\n"))
	generate(reader, output)
	val outputLines = output.toString().lines()
	assertEquals(expectedLines, outputLines)
}
```

즉, I/O 발생 장소를 프로그램 진입점에 가깝게 옮기면 계산으로 처리할 수 있는 부분이 많아진다.

## I/O에서 데이터로

```kotlin
@Throws(IOException::class)
fun generate(reader: Reader, writer: Writer) {
	val valuablecustomers = reader
		.readLines()
		.toValuableCustomers()
		.sortedBy(CustomerData::score)
		writer.appendLine("ID\tName\tSpend")
		for (customerData in valuableCustomers) {
			writer.appendLine(customerData.outputLine)
		}
		writer.append(valuableCustomers.summarised())
}
```

‘파라미터 도입’으로 generate()가 List를 읽게 만들고, 반환값도 List로 변경한 후, 계산 부분 ‘함수 추출’을 적용한다. 마지막으로 resultLines과 generate 함수를 인라이닝해준다.

```kotlin
fun generate(lines: List<String>): List<String> {
	val valuableCustomers = lines
		.withoutHeader()
		.map(String:：toCustomerData)
		.filter { it.score >= 10 }
		.sortedBy(CustomerData::score)
	return listOf("ID\tName\tSpend") +
		valuableCustomers.map(CustomerData::outputLine) +
		valuablecustomers.summarised()
}
```

## 효율적인 쓰기

```kotlin
fun main() {
	System.'in'.reader().use { reader ->
		System.out.writer().use { writer ->
			writer.append(
			generate(
			reader.readLines()
			).joinToString("\n”)
			)
		}
	}
}
```

함수형 분해를 할 때, 전체 입력을 메모리에 읽어서 처리한 후 전체 출력을 메모리에 만들어서 표준 출력에 기록하는 경우 OutOfMemory 에러가 발생할 가능성이 있다.

List를 이터레이션하면서 각 줄을 따로따로 기록했더라면 메모리 부족은 발생하지 않았을 것이다. 

```kotlin
fun main() {
	System.`in`.reader().use { reader ->
		System.out.writer().use { writer ->
			writer.appendLines(
				generate(reader. readLinesO)
			)
		}
	}
}
fun Writer.appendLines(lines: Sequence<CharSequence>): Writer {
	return this.also {
		lines.forEach(this::appendLine)
	}
}
```

## 효과적인 읽기

```kotlin
fun generate(lines: List<String>): Sequence<String> {
	val valuableCustomers = lines
		.withoutHeader()
		.map(String::toCustomerData)
		.filter { it.score >= 10 }
		.sortedBy(CustomerData::score)
	return sequenceOf("ID\tName\tSpend") +
		valuableCustomers.map(CustomerData::outputLine) +
		valuablecustomers.summarised()
}

```

읽는 부분에서도 메모리 절약을 할 수 있는데, 위 generate() 함수에서 valueableCustomers를 만들어내는 연산 파이프라인은 라인마다 List를 만들어낸다. 이때, Sequence를 사용하면 중간 컬렉션을 사용하지 않을 수 있지만, lines를 Sequence로 바꾸고나면 문제가 발생한다.

```kotlin
fun generate(lines: List<String>): Sequence<String> {
	val valuableCustomers: Sequence<CustomerData> = lines
		.asSequence()
		.withoutHeader()
		.map(String::toCustomerData)
		.filter { it.score >= 10 }
		.sortedBy(CustomerData::score)
	return sequenceOf("ID\tName\tSpend") +
		valuablecustomers.map(CustomerData::outputLine) +
		valuableCustomers.summarised()
}
private fun Sequence<String>.withoutHeader() = drop(l)

private fun Sequence<CustomerData>.summarised(): String =
	sumByDouble { it.spend }.let { total ->
	"\tTOTAL\t${total.toMoneySt ring()}”
}
```

원인은 valuablecustomers를 두 번 이터레이션하기 때문인데, 시퀀스를 두 번 이터레이션 하면서 IllegalStateException이 발생한다.

시퀀스에 toList() 호출하도록하여 끝내도록 하고, 파라미터 도입으로 파라미터를 sequence로 변환한다.

```kotlin
fun main() {
		System.'in'.reader().use { reader ->
			System.out.writer().use { writer ->
				writer.appendLines(
					generate(
						reader.readLines().asSequence().constrainOnce()
				)
			)
		}
	}
```

특히 윗부분에서 모든 줄을 다 읽고 시퀀스로 바꾸지말고, Reader에서 buffered().lineSequence()를 이용하여 시퀀스를 얻으면 메모리를 많이 절약할 수 있다.

```kotlin
fun main() {
		System.'in'.reader().use { reader ->
			System.out.writer().use { writer ->
				writer.appendLines(
				generate(
					reader.buffered().lineSequence()
			)
		)
	}
}
```
