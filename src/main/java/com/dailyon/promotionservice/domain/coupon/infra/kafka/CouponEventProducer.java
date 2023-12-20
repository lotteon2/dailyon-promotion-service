
package com.dailyon.promotionservice.domain.coupon.infra.kafka;


import com.dailyon.promotionservice.common.exceptions.ErrorResponseException;
import com.dailyon.promotionservice.domain.coupon.infra.kafka.dto.OrderDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void produceCouponSuccessMessage(OrderDTO orderDTO) {
        try {
            String data = objectMapper.writeValueAsString(orderDTO);
            kafkaTemplate.send("create-order-use-coupon", data);
        } catch (Exception e) {
            throw new ErrorResponseException("쿠폰 사용 완료 후, kafka 메세지 발송 중 에러");
        }
    }

    public void produceCancelOrderMessage(OrderDTO orderDTO) {
        try {
            String data = objectMapper.writeValueAsString(orderDTO);
            kafkaTemplate.send("cancel-order", data);
        } catch (Exception e) {
            throw new ErrorResponseException("쿠폰 사용 완료 후, kafka 메세지 발송 중 에러");
        }
    }
}