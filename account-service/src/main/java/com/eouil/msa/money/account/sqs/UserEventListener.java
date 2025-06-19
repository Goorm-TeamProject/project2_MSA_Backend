package com.eouil.msa.money.account.sqs;

import com.eouil.msa.money.account.dtos.CreateAccountRequest;
import com.eouil.msa.money.account.services.AccountService;
import com.eouil.msa.shared.kafka.UserCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventListener {

    private final AccountService accountService;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.userCreatedQueueUrl}")
    private String queueUrl;

    @PostConstruct
    public void startPolling() {
        Thread thread = new Thread(this::pollMessages);
        thread.setDaemon(true);
        thread.start();
    }

    private void pollMessages() {
        log.info("[SQS] user.created 큐 폴링 시작");

        while (true) {
            try {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .waitTimeSeconds(10)
                        .maxNumberOfMessages(5)
                        .build();

                List<Message> messages = sqsClient.receiveMessage(request).messages();

                for (Message message : messages) {
                    handleMessage(message);
                }

                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("[SQS] Polling 중 오류 발생", e);
            }
        }
    }

    private void handleMessage(Message message) {
        try {
            log.info("[SQS] 메시지 수신 - messageId: {}, body: {}", message.messageId(), message.body());

            UserCreatedEvent event = objectMapper.readValue(message.body(), UserCreatedEvent.class);

            CreateAccountRequest req = new CreateAccountRequest(BigDecimal.ZERO);
            accountService.createAccount(req, event.getUserId());

            log.info("[SQS] 계좌 생성 완료 - userId: {}", event.getUserId());

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());

        } catch (Exception e) {
            log.error("[SQS] 메시지 처리 실패 - body: {}, error: {}", message.body(), e.getMessage(), e);
        }
    }
}
