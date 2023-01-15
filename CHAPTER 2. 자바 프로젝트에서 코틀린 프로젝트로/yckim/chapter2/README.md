# 전략

## 자바의 함수형 접근 방법의 한계
자바 1.8의 함수형 접근 방법에 대해서는 다음과 같이 한계가 존재합니다.
- 불변 값 타입을 구현하기 위해 필요한 장황한 코드
- 원시 타입과 참조 타입의 분리
- 널 참조
- 일반적인 고차함수 지원이 부족한 스트림

## 자바의 모델 클래스를 코틀린의 데이터 클래스로
코틀린에서는 자바(1.8)처럼 불변 값 타입을 구현하기 위해 장황한 코드를 작성할 필요가 없습니다.

코틀린의 데이터 클래스를 통해 간단하게 불변 값을 생성할 수 있습니다.

## build.gradle.kts 설정
```kotlin
import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

// 컴파일 시점에서 사용하는 JDK 버전, 소스 코드에서 사용할 수 있는 Java 버전을 해당 값으로 제한
java.sourceCompatibility = JavaVersion.VERSION_17
// 생성된 클래스 파일의 버전을 제어, 프로그램에서 실행할 수 있는 가장 낮은 Java Version
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}
```

# 정리
- 자바 1.8 버전의 함수형 접근 방식에 대해서는 다음과 같은 한계가 존재합니다.
- 자바의 모델 클래스를 데이터 클래스로 변환하면 코드의 양을 상당히 줄일 수 있습니다.