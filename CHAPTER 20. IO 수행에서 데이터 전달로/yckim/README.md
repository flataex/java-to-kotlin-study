# IO 수행에서 데이터 전달으로

## 테스트에 귀 기울이기

- 반환되는 값 대신에 I/O에 의존하기 때문에 generate 함수는 계산이 아닙니다.

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
    	assertEqauls(expectedLines.joinToString("\n"), output.toString())
    }
    ```

- 다음과 같이 변환한다면 generate 함수는 계산으로 변하게 됩니다.

    ```kotlin
    private fun check(
    	inputLines: List<String>,
    	expectedLines: List<String>
    ) {
    	val output = StringWriter()
    	val reader = StringReader(inputLines.joinToString("\n"))
    	generate(
    		reader,
    		output
    	)
    	val outputLines = output.toString().lines()
    	assertEqauls(expectedLines, outputLines)
    }
    
    ```

    - generate 함수 밖에서 모든 I/O가 일어나게 변경되었기 때문에 계산으로 변화하게 됩니다.
- 다른 동작과 마찬가지로 I/O 구문도 함수 근처에 옮기면 계산으로 처리할 수 있게 됩니다.

## I/O에서 데이터로

- 함수를 동작으로 만드는 I/O 값은 데이터로 받도록 변경하면 함수를 계산으로 만드는데 도움을 줄 수 있습니다.

## 효율적인 쓰기

- 출력 데이터를 처리할 때 모든 데이터를 메모리에 올리지 않고 시퀀스를 사용하여 이터레이션될 때마다 한 번에 한 줄씩 처리하면 메모리를 효율적으로 사용할 수 있습니다.

## 효과적인 읽기
- 읽기 작업도 시퀀스를 이용하여 한줄씩 읽으면 효율적으로 데이터를 처리할 수 있습니다.
