# 목에서 맵으로
- 목은 객체지향 코드와 프로덕션 의존 관계의 결합을 떼어놓는 일반적인 기법입니다.
- 시스템을 더 효율적으로 테스트하기 위한 결합도를 낮추는 과정에서 보통 시스템이 개선된다는 점을 발견했습니다.
- 목 프레임워크가 인터페이스 구현뿐만 아니라, 예상되는 메서드 호출과 호출시 반환해야 하는 내용을 기술하게 해준다는 점입니다.
    - 목으로 기술한 메서드 호출과 예상 반환 값을 이해하기 어려울 때가 자주 있습니다.
- 목의 사용 방법에 대해서는 의견이 분분하지만 코틀린 코드 기반에서는 목이 없는 경우가 나은 경우가 많기 때문에 목을 사용하지 않는 방향이 좋습니다.
## 목을 맵으로 대체하기
- 목 프레임워크(Mockito)를 제거하기 위해 Map을 이용하여 가짜 객체를 만들 수 있습니다.
- 확장 함수를 통해 통일성 있는 테스트를 작성할 수 있습니다.

    ```kotlin
    // 도입전
    private val recommendations = Recommendations(
        { l1, l2 -> distanceInMetersBetween.getValue(l1 to l2) },
        featuredDestinations::getValue
    )
    
    // 도입후
    private fun <K1, K2, V> Map<Pair<K1, K2>, V>.getValue(k1: K1, k2: K2) 
    		= getValue(k1 to k2)
    private val recommendations = Recommendations(
            distanceInMetersBetween::getValue,
            featuredDestinations::getValue
        )
    ```

- 도우미 메서드를 통해 테스트 코드의 가독성을 향상시킬 수 있습니다.

## 그렇지만 실제 목에서 벗어났는가?

- 공통된 구조를 가지는 부분을 메서드로 추출하면 훨씬 깔끔한 코드를 작성할 수 있습니다.

    ```kotlin
        @Test
        fun returns_no_recommendations_when_no_locations() {
            check(
                featuredDestinations = emptyMap(),
                distances = distances,
                recommendations = emptySet(),
                shouldReturn = emptyList()
            )
        }
    
        @Test
        fun returns_no_recommendations_when_no_featured() {
            check(
                featuredDestinations = emptyMap(),
                distances = distances,
                recommendations = setOf(paris),
                shouldReturn = emptyList()
            )
        }
    
        @Test
        fun returns_recommendations_for_single_location() {
            check(
                featuredDestinations = mapOf(
                    paris to listOf(eiffelTower, louvre),
                ),
                distances = distances,
                recommendations = setOf(paris),
                shouldReturn = listOf(
                    FeaturedDestinationSuggestion(paris, louvre, 1000),
                    FeaturedDestinationSuggestion(paris, eiffelTower, 5000)
                )
            )
        }
    
        @Test
        fun returns_recommendations_for_multi_location() {
            check(
                featuredDestinations = mapOf(
                    paris to listOf(eiffelTower, louvre),
                    alton to listOf(flowerFarm, watercressLine),
                ),
                distances = distances,
                recommendations = setOf(paris, alton),
                shouldReturn = listOf(
                    FeaturedDestinationSuggestion(alton, watercressLine, 320),
                    FeaturedDestinationSuggestion(paris, louvre, 1000),
                    FeaturedDestinationSuggestion(paris, eiffelTower, 5000),
                    FeaturedDestinationSuggestion(alton, flowerFarm, 5300)
                )
            )
        }
    
        @Test
        fun deduplicates_using_smallest_distance() {
            check(
                featuredDestinations = mapOf(
                    alton to listOf(flowerFarm, watercressLine),
                    froyle to listOf(flowerFarm, watercressLine)
                ),
                distances = distances,
                recommendations = setOf(alton, froyle),
                shouldReturn = listOf(
                    FeaturedDestinationSuggestion(froyle, flowerFarm, 0),
                    FeaturedDestinationSuggestion(alton, watercressLine, 320)
                )
            )
        }
    }
    ```