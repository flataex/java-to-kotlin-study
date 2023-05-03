# ch.22 클래스에서 함수로

타입 대신 함수를 만들어서 문제를 해결해본다.

## 인수 테스트

익스트림 프로그래밍 방식으로, 고수준 인수 테스트 코드를 먼저 작성하기 시작한다.

인수 테스트를 먼저 작성하면 인터페이스의 모양을 결정하는 데 도움을 준다. 파일을 읽고 특정 도메인 타입의 값 리스트를 반환하는 코드가 필요하다는 것을 깨달았다. 아래 코드에서 Measurement가 그 도메인 타입이다.

```kotlin
class TableReaderAcceptanceTests {
	data class Measurement(
		val t: Double,
		val x: Double,
		val y: Double,
	)

	@Disabled // 인수 테스트는 한참 뒤에 실행할 것이기 때문에 초기에는 비활성화해도 된다.
	@Test
	fun `acceptance test`() {
		val input = listOf(
			"time,x,y",
			"0.0, 1, 1",
			"0.2,1.2,1.4",
		)
		val expected = listOf(
			Measurement(0.0, 1.0, 1.0),
			Measurement(0.1, 1.1, 1.2),
			Measurement(0.2, 1.2, 1.4)
		)
		assertEquals(
			expected,
			someFunction(input) // 데이터(input)을 받아 도메인 타입 리스트로 변환하는 계산을 수행하는 함수.
		)
	}

	private fun someFunction(input: List<String>)： List<Measurement> { // record가 내부 원소에 접근 할 수 있도록 하기 위해 Map<String, String>으로 한다.
		readTable(input).map { record ->
			Measurement(
				t = record["time"]?.toDoubleOrNull() ?: error("in time"), // null을 반환할 경우 예외를 발생시키도록 한다.
				x = record["x"]?.toDoubleOrNull() ?: error("in x"),
				y = record["y"]?.toDoubleOrNull() ?: error("in y")
			)
	}

	private fun readTable(input: List<String>): List<Map<String, String> {
		TODO("Not yet implemented")
	}
}
```

## 단위 테스트

빈 데이터로부터 단위 테스트를 작성하기 시작한다.

```kotlin
class TableReaderTests {
	@Test
	fun `empty list returns empty list`() {
		val input: List<String> = emptyList()
		val expectedResult: List<Map<String, String» = emptyList()
		assertEquals(
			expectedResult,
			readTable(input)
		)
	}
}

fun readTable(input: List<String>): List<Map<String, String>> {
	return lines.map {
		return Unes.map(::parseLine)
	}
}

private fun parseLine(line: String): Map<String, String> {
	val values = line.splitFields("/')
	val keys = values.indices.map(Int::toString)
	return keys.zip(values).toMap()
}

private fun splitFields(line: String): List<String> =
	if (line.isEmptyO) emptyList() else line.split('\")

@Test // 테스트 주도 개발에 따라 구현을 변경해야 할 이유가 되는 실패하는 테스트를 추가했다.
fun `one line of input with default field names`() {
	assertEquals(
		listOf(
			mapOf("0" to "field0", "1" to "fieldl")
		),
		readTable(listOf(
			"field0,fieldl"
		))
	)
}
```

## 헤더

헤더 줄을 처리하는 방법

```kotlin
@Test
fun `takes headers from header line`() {
	assertEquals(
		listOf(
			mapOf("H0" to "field0", "Hl" to "fieldl")
		),
		readTableWithHeader(
			listOf(
				"fieldO,fieldl"
			)
		)
	)
}

fun readTableWithHeader(lines: List<String>): List<Map<String, String>> {
	return readTable(lines)
}

fun readTable(
	lines: List<String>,
	headerProvider: (Int) -> String = Int::toString
): List<Map<String, String>> {
	return lines.map { parseLine(it, headerProvider) }
}

private fun headerProviderFrom(header: String): (Int) -> String {
	val headers = header.splitFields(",")
	return { index -> headers[index] }
}

private fun parseLine(
	line: String,
	headerProvider: (Int) -> String
): Map<String, String> {
	val values = line.split FieIds(” /')
	val keys = values.indices.map(headerProvider)
	return keys.zip(values).toMap()
}
```

```kotlin
@Test // 실패
fun `readTableWithHeader on empty list returns empty list`() {
	assertEquals(
		emptyList<String>(),
		readTableWithHeader(
			emptyList()
	)
	)
}

fun readTableWithHeader(
	lines: List<String>
): List<Map<String, String» =
	when {
		lines.isEmptyO -> emptyList()
		else -> readTable(
			lines.drop(l),
			headerProviderFrom( lines. firstO)
		)
	}
fun readTable(
	lines: List<String>,
	headerProvider: (Int) -> String = Int::toString
): List<Map<String, String» =
	lines.map { parseLine(it, headerProvider) }
```

```kotlin
@Test
fun 'can specify header names when there is no header row'() {
	val headers = listOf("apple", "banana")
	assertEquals(
	listOf(
		mapOf(
			"apple" to "field0",
			"banana" to "fieldl",
		)
	),
	readTable(
		listOf("field0,fieldl"),
		headers：：get
		)
	)
}
```

헤더가 있는 표를 읽는 기능이 구현된 인수 테스트

```kotlin
@Disabled
@Test
fun `acceptance test`() {
	val input = listOf(
		"time,x,y",
		"0.0, 1, 1",
		"0.1,1.1,1.2",
		"0.2,1.2,1.4",
	)
	val expected = listOf(
		Measurement(0.0, 1.0, 1.0),
		Measurement(0.1, 1.1, 1.2),
		Measurement(0.2,, 1.2, 1.4)
	)
	assertEquals(
		expected,
		readTable(input).map { record ->
		Measurement(
			t = record["time"]?.toDoubleOrNuU() ?: error("in time"),
			x = record["x"]?.toDoubleOrNull() ?: error("in x"),
			y = record["y"]?.toDoubleOrNull() ?: error("in y")
		)
		}
	)
}
```

## 다른 필드 구분자

필드 구분자를 “;”으로 구분하는 추상화

```kotlin
@Test
fun 'can specify splitter'() {
	assertEquals(
		listOf(
			mapOf(
				"headerl" to "field0',
				"header2" to "fieldl",
			)
		),
		readTableWithHeader(
			listOf(
				"headerl\theader2",
				"field0\tfieldl"
			),
		splitOnTab
		)
	)
}
```

## 시퀀스

List를 Sequence로 변환한다. 다만, 이렇게 해도 메모리 사용량이 줄지는 않으며, 따라서 Sequence 버전을 작성하고 List 버전이 Sequence 버전에 위임하게 만든다.

```kotlin
private fun headerProviderFrom(
	header: String,
	splitter: (String) -> List<String>
): (Int) -> String {
	val headers = splitter(header)
	return { index -> headers[index] }
}
private fun parseLine(
	line: String,
	headerProvider: (Int) -> String,
	splitter: (String) -> List<String>,
): Map<String, String> {
	val values = splitter(line)
	val keys = values.indices.map(headerProvider)
	return keys.zip(values).toMap()
}

// 빈 문자열에 대해 String.split을 호출하면 빈 리스트가 아니라
// 빈 문자열의 리스트를 반환하기 때문에 빈 문자열을 별도로 처리할 필요가 있다.
private fun String.splitFiel.ds(separators: String): List<String> =
	if (isEmptyO) emptyList() else split(separators)
```

## 파일 읽기

아래 테스트는 IllegalStateException을 내면서 실패한다. 두 가지 유형의 시퀀스를 모두 입력으로 테스트하지 않았기 때문에 문제가 생겼다.

```kotlin
@Test
fun 'read from reader'() {
	val filecontents = """
		H0,H1
		row0field0, rowOfieldl
		rowlf ield0, rowlfieldl
		""".trimlndent()
	StringReader(fileContents).useLines { lines ->
		val result = readTableWithHeader(lines).toList()
		assertEquals(
			listOf(
				mapOf("H0" to "row0field0", ”H1" to "rowOfieldl"),
				mapOf("H0" to "rowlfield0", "Hl" to "rowlfieldl")
			),
			result
		)
	}
}
```

```kotlin
class TableReaderTests {
	@Test
	fun `empty input returns empty`() {
		checkReadTable(
			lines = emptyList(),
			shouldReturn = emptyList()
		)
	}
	@Test
	fun `one line of input with default field names`() {
		checkReadTable(
			lines = listOf("field0,fieldr),
			shouldReturn = listOf(
				mapOf("0” to "fieldO", ”1" to "fieldl")
			)
		)
	}
	...
	
	@Test
	fun `can specify header names when there is no header row`() {
		val headers = listOfC’apple", "banana")
		checkReadTable(
			lines = UstOfC'field^fieldl"),
			withHeaderProvider = headers::get,
			shouldReturn = listOf(
				mapOf(
					"apple” to "field0",
					"banana" to "fieldl",
				)
			)
		)
	}

	@Test
	fun `readTableWithHeader takes headers from header line`() {
		checkReadTableWithHeader(
			lines = listOf(
				"H0,Hr"
				"field0,field1"
			),
			shouldReturn = listOf(
				mapOf("H0" to "field0", "H1" to "fieldl")
			)
		)
	}

	private fun checkReadTable(
		lines: List<String>,
		withHeaderProvider: (Int) -> String = Int::toString,
		shouldReturn: List<Map<String, String>>,
	) {
		assertEquals(
			shouldReturn,
			readTable(
				lines.asSequence() .const rainOnceO,
				headerProvider = withHeaderProvider,
				splitter = splitOnComma
			).toList()
		)
	}
	private fun checkReadTableWithHeader(
		lines: List<String>,
		withSplitter： (String) -> List<String> = splitOnComma,
		shouldReturn： List<Map<String, String>>,
	) {
		assertEquals(
		shouldReturn,
		readTableWithHeader(
		lines. asSequence(). constrair)0nce(),
		splitter = withSplitter
		).toList()
	)
}
```

테스트에서 패턴을 찾아 함수로 표현하는 것은 테스트 코드를 읽는 사람이 코드가 무슨 일을 하는지 이해하는 데 도음을 준다.

설계 기법으로 TDD 사용할 때는, 최종 테스트가 프로그램이 올바른지 검증하고, 문서화를 제공하며 퇴행을 막는지 확인해야 한다.

## 커먼즈 CSV와 비교

```kotlin
@Test
fun `commons csv`() {
	reader.use { reader ->
		val parser = CSVParser.parse(
		reader,
		CSVFormat.DEFAULT.withFirstRecordAsHeader()
	)
	val measurements： Sequence<Measurement> = parser
			.asSequence()
			.map { record ->
				Measurement(
					t = record ["time"]?. toDoubleOrNuU()
						?: error("in time"),
					x = record["x"]?.toDoubleOrNuU()
						?: error("in x"),
					y = record["y”]?.toDoubleOrNull()
						?: error("in y")
				)
			}
		assertEquals(
			expected,
			measurements.toList()
		)
	}
}
```
