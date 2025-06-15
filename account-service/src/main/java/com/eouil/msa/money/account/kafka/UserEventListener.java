package com.eouil.msa.money.account.kafka;

import com.eouil.msa.money.account.dtos.CreateAccountRequest;
import com.eouil.msa.money.account.services.AccountService;
import com.eouil.msa.shared.kafka.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventListener {
    private final AccountService accountService;

    @KafkaListener(
            topics = "user.created",
            groupId = "account-service",
            containerFactory = "kafkaListenerContainerFactory" // 컨테이너 팩토리 명시
    )
    public void handleUserCreated(
            @Payload UserCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        try {
            log.info("[Kafka] 사용자 생성 이벤트 수신 - topic: {}, userId: {}",
                    topic, event.getUserId());

            CreateAccountRequest req = new CreateAccountRequest(BigDecimal.ZERO);
            accountService.createAccount(req, event.getUserId());

            log.info("[Kafka] 계좌 생성 완료 - userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("[Kafka] 계좌 생성 실패 - userId: {}, error: {}",
                    event.getUserId(), e.getMessage(), e);
            throw new RuntimeException("계좌 생성 실패", e);
        }
    }
}