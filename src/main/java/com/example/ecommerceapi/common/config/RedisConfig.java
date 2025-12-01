package com.example.ecommerceapi.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 및 Redisson 설정 클래스입니다.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    /**
     * Redis Serialization을 위한 ObjectMapper 설정
     * LocalDateTime 등의 Java 8 시간 타입을 직렬화하기 위해 JavaTimeModule을 등록합니다.
     * 타입 정보를 포함하여 직렬화하여 역직렬화 시 올바른 타입으로 복원합니다.
     */
    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // 안전한 타입 검증기 설정: 애플리케이션 패키지와 Java 표준 패키지만 허용
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.example.ecommerceapi")
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .build();

        // 모든 타입에 대해 타입 정보를 포함 (record 타입 포함)
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);

        return mapper;
    }

    /**
     * Redisson 클라이언트 설정
     * 분산 락에 사용됩니다.
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }

    /**
     * Redis Connection Factory 설정
     * Spring Data Redis에서 사용됩니다.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    /**
     * RedisTemplate 설정
     * 캐싱 및 일반적인 Redis 작업에 사용됩니다.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(createRedisObjectMapper());

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }

    /**
     * CacheManager 설정
     * Spring Cache 추상화에 사용됩니다.
     *
     * TTL 전략:
     * - 기본: 10분 (일반적인 캐시)
     * - 인기 상품 (판매량): 5분 (주문 데이터는 상대적으로 느리게 변경)
     * - 인기 상품 (조회수): 3분 (조회수는 빈번하게 변경되므로 짧게 설정)
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(createRedisObjectMapper());

        // 기본 캐시 설정 - TTL 10분
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues(); // null 값은 캐싱하지 않음

        // 캐시별 개별 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 인기 상품 - 판매량 기준 (TTL: 5분)
        // 주문 데이터는 상대적으로 느리게 변경되므로 5분 유지
        cacheConfigurations.put("popularProducts:SALES", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 인기 상품 - 조회수 기준 (TTL: 3분)
        // 조회수는 빈번하게 변경되므로 짧게 설정
        cacheConfigurations.put("popularProducts:VIEWS", defaultConfig.entryTtl(Duration.ofMinutes(3)));

        // 전체 상품 목록 (TTL: 30분)
        // 상품 데이터는 자주 변경되지 않으므로 30분 유지
        cacheConfigurations.put("allProducts", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 단일 상품 조회 (TTL: 30분)
        // 개별 상품 정보는 자주 변경되지 않으므로 30분 유지
        cacheConfigurations.put("product", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 전체 쿠폰 목록 (TTL: 60분)
        // 쿠폰 정보는 거의 변경되지 않으므로 1시간 유지
        cacheConfigurations.put("allCoupons", defaultConfig.entryTtl(Duration.ofMinutes(60)));

        // 주문 조회 (TTL: 60분)
        // 주문은 완료 후 불변 데이터이므로 1시간 유지
        cacheConfigurations.put("order", defaultConfig.entryTtl(Duration.ofMinutes(60)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
