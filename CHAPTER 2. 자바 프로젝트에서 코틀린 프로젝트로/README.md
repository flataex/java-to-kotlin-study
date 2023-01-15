# ch2. 자바 프로젝트에서 코틀린 프로젝트로

## 전략
저자 속했던 팀은 기존 자바 코드를 코틀린으로 변환하기로 결정했다. 코틀린의 데이터 클래스는 코드의 양을 상당히 줄여주었고 다른 클래스와 의존관계가 없는 값 클래스들을 변환하였을 때 다른 자바 코드 기반 클래스들에 어떠한 영향도 주지 않았다. 그리고 나서, 자바 도메인 모델 클래스에서 새 기능을 추가하거나 변경할 때마다 그 클래스를 코틀린 데이터 클래스로 변환하고 변환결과를 커밋한 후 새 기능을 구현하였다.

## 코틀린지원을 자바 빌드에 추가하기 

그레이들이나 IDE가 코틀린을 컴파일하도록 하기 위해서는 그레이들 빌드 설정에 코드 몇 줄을 추가하면 된다. 돠한 사용하려는 바이트 코드 최소 버전에 맞는 JVM에 대한 코틀린 표준 라이브러리를 의존 관계에 추가해야 한다.(책에서는 JDK 11 선택)
```groovy
plugins {
    id'org.jetbrains.kotlin.jvm' version "1.5.0"
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

...
dependencies {
    imeplementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    ...
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile) {
    kotlinOptions {
        jvmTarget = "11"
        javaParameters = true
        freeCompileArgs= ["-Xjvm-default=all"]
    }
}
```

코틀린을 사용할 수 있는지 검증을 위한 간단한 코드(HelloWorld.kt)
```kotlin
fun main() {
    println("hello, world")
}
```

## 다음으로 나아가기
책 출간 시점과 현재의 빌드 파일도 차이가 날 수 있지만, 자바 빌드에 코틀린 지원을 추가하는 것은 단순할 것이다.
