package com.example.ecommerceapi.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Stream 관련 설정 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamManager {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Consumer Group 생성
     * - 각 도메인에서 이 메서드를 호출하여 Stream 초기화
     *
     * @param streamKey Stream 키
     * @param consumerGroup Consumer Group 이름
     */
    public void createConsumerGroup(String streamKey, String consumerGroup) {
        try {
            boolean groupExists = checkConsumerGroupExists(streamKey, consumerGroup);

            if (!groupExists) {
                redisTemplate.opsForStream().createGroup(
                        streamKey,
                        ReadOffset.from("0"),  // 처음부터 읽기
                        consumerGroup
                );
                log.info("Redis Stream Consumer Group created: stream={}, group={}",
                        streamKey, consumerGroup);
            } else {
                log.info("Redis Stream Consumer Group already exists: stream={}, group={}",
                        streamKey, consumerGroup);
            }
        } catch (Exception e) {
            log.warn("Failed to create Redis Stream Consumer Group: {}", e.getMessage());
            // Stream이 없으면 첫 메시지 발행 시 자동 생성되므로 에러 무시
        }
    }

    /**
     * Consumer Group이 이미 존재하는지 확인
     */
    private boolean checkConsumerGroupExists(String streamKey, String consumerGroup) {
        try {
            var groups = redisTemplate.opsForStream().groups(streamKey);
            return groups.stream()
                    .anyMatch(info -> consumerGroup.equals(info.groupName()));
        } catch (Exception e) {
            return false;
        }
    }
}
