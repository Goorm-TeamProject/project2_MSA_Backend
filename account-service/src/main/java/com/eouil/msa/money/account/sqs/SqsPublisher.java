package com.eouil.msa.money.account.sqs;

import com.eouil.msa.money.transaction.dtos.TransferResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsPublisher {
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String QUEUE_URL = "https://sqs.ap-northeast-2.amazonaws.com/640168458081/transaction";

    public void publishTransactionLog(TransferResponseDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(QUEUE_URL)
                    .messageBody(json)
                    .build();
            sqsClient.sendMessage(request);
            log.info("SQS 전송 완료: {}", dto.getTransactionId());
        } catch (Exception e) {
            log.error("SQS 전송 실패: {}", e.getMessage());
        }
    }
}
