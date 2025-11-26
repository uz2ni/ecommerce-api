package com.example.ecommerceapi.product.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCacheService 단위 테스트")
class ProductCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ProductCacheService cacheService;

    @Nested
    @DisplayName("조회수 증가 테스트")
    class IncrementViewCountTest {

        @BeforeEach
        void setUp() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
        }

        @Test
        @DisplayName("Redis INCR로 조회수를 증가시킨다")
        void incrementViewCount_ShouldIncrementInRedis() {
            // given
            Integer productId = 1;
            String expectedKey = "product:viewcount:1";
            given(valueOperations.increment(expectedKey)).willReturn(1L);

            // when
            Long result = cacheService.incrementViewCount(productId);

            // then
            assertThat(result).isEqualTo(1L);
            verify(valueOperations).increment(expectedKey);
        }

        @Test
        @DisplayName("여러 번 증가 시 증가된 조회수를 반환한다")
        void incrementViewCount_ShouldReturnIncrementedValue() {
            // given
            Integer productId = 1;
            String expectedKey = "product:viewcount:1";
            given(valueOperations.increment(expectedKey))
                    .willReturn(1L)
                    .willReturn(2L)
                    .willReturn(3L);

            // when
            Long result1 = cacheService.incrementViewCount(productId);
            Long result2 = cacheService.incrementViewCount(productId);
            Long result3 = cacheService.incrementViewCount(productId);

            // then
            assertThat(result1).isEqualTo(1L);
            assertThat(result2).isEqualTo(2L);
            assertThat(result3).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("조회수 조회 테스트")
    class GetViewCountTest {

        @BeforeEach
        void setUp() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
        }

        @Test
        @DisplayName("Redis에서 조회수를 조회한다")
        void getViewCount_ShouldReturnViewCountFromRedis() {
            // given
            Integer productId = 1;
            String expectedKey = "product:viewcount:1";
            given(valueOperations.get(expectedKey)).willReturn(100L);

            // when
            Long result = cacheService.getViewCount(productId);

            // then
            assertThat(result).isEqualTo(100L);
            verify(valueOperations).get(expectedKey);
        }

        @Test
        @DisplayName("조회수가 없으면 0을 반환한다")
        void getViewCount_ShouldReturnZero_WhenNoData() {
            // given
            Integer productId = 1;
            String expectedKey = "product:viewcount:1";
            given(valueOperations.get(expectedKey)).willReturn(null);

            // when
            Long result = cacheService.getViewCount(productId);

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("String 타입으로 저장된 조회수를 Long으로 변환한다")
        void getViewCount_ShouldConvertStringToLong() {
            // given
            Integer productId = 1;
            String expectedKey = "product:viewcount:1";
            given(valueOperations.get(expectedKey)).willReturn("50");

            // when
            Long result = cacheService.getViewCount(productId);

            // then
            assertThat(result).isEqualTo(50L);
        }
    }

    @Nested
    @DisplayName("조회수 삭제 테스트")
    class DeleteViewCountTest {

        @Test
        @DisplayName("Redis에서 조회수를 삭제한다")
        void deleteViewCount_ShouldDeleteFromRedis() {
            // given
            Integer productId = 1;
            String expectedKey = "product:viewcount:1";
            given(redisTemplate.delete(expectedKey)).willReturn(true);

            // when
            cacheService.deleteViewCount(productId);

            // then
            verify(redisTemplate).delete(expectedKey);
        }
    }

    @Nested
    @DisplayName("모든 조회수 키 조회 테스트")
    class GetAllViewCountKeysTest {

        @Test
        @DisplayName("패턴에 맞는 모든 키를 조회한다")
        void getAllViewCountKeys_ShouldReturnAllKeys() {
            // given
            String pattern = "product:viewcount:*";
            Set<String> keys = new HashSet<>(Arrays.asList(
                    "product:viewcount:1",
                    "product:viewcount:2",
                    "product:viewcount:3"
            ));
            given(redisTemplate.keys(pattern)).willReturn(keys);

            // when
            List<String> result = cacheService.getAllViewCountKeys();

            // then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyInAnyOrder(
                    "product:viewcount:1",
                    "product:viewcount:2",
                    "product:viewcount:3"
            );
            verify(redisTemplate).keys(pattern);
        }

        @Test
        @DisplayName("키가 없으면 빈 리스트를 반환한다")
        void getAllViewCountKeys_ShouldReturnEmptyList_WhenNoKeys() {
            // given
            String pattern = "product:viewcount:*";
            given(redisTemplate.keys(pattern)).willReturn(new HashSet<>());

            // when
            List<String> result = cacheService.getAllViewCountKeys();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null이 반환되면 빈 리스트를 반환한다")
        void getAllViewCountKeys_ShouldReturnEmptyList_WhenNull() {
            // given
            String pattern = "product:viewcount:*";
            given(redisTemplate.keys(pattern)).willReturn(null);

            // when
            List<String> result = cacheService.getAllViewCountKeys();

            // then
            assertThat(result).isEmpty();
        }
    }
}