package com.dailyon.promotionservice.domain.coupon.infra.kafka;

import com.dailyon.promotionservice.common.exceptions.ErrorResponseException;
import com.dailyon.promotionservice.domain.coupon.infra.kafka.dto.OrderDTO;
import com.dailyon.promotionservice.domain.coupon.infra.kafka.dto.RefundDTO;
import com.dailyon.promotionservice.domain.coupon.infra.kafka.dto.enums.OrderEvent;
import com.dailyon.promotionservice.domain.coupon.service.CouponService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dailyon.promotionservice.domain.coupon.infra.kafka.dto.enums.OrderEvent.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponEventListener {
    private final ObjectMapper objectMapper;
    private final CouponService couponService;
    private final CouponEventProducer couponEventProducer;

    // product-service로부터 메세지 받아서 쿠폰 사용처리
    @KafkaListener(topics = "create-order-product")
    public void onStockReductionSuccess(String message, Acknowledgment ack) {
        OrderDTO orderDTO = null;
        try {
            orderDTO = objectMapper.readValue(message, OrderDTO.class);
            couponService.processCouponUsageAndPublishEvent(orderDTO);
        } catch (JsonParseException e1) {
            throw new ErrorResponseException("이벤트 메세지 직렬화 실패");
        } catch (Exception e) {
            if (orderDTO != null) {
                couponEventProducer.produceCancelOrderMessage(orderDTO.withOrderEvent(OrderEvent.COUPON_FAIL));
            } else {
                log.error("Order processing 실패, orderDTO가 null, order event 업데이트 불가능", e);
            }
            throw new ErrorResponseException("쿠폰 사용처리 도중 에러");
        } finally {
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "cancel-order")
    public void onCouponCancelEvent(String message, Acknowledgment ack) {
        try {
            OrderDTO orderDTO = objectMapper.readValue(message, OrderDTO.class);

            switch (orderDTO.getOrderEvent()) {
                case PENDING:
                case STOCK_FAIL:
                case COUPON_FAIL: // 위 3개 케이스 ack 후 무시
                    ack.acknowledge();
                    return;
                case POINT_FAIL:
                case PAYMENT_FAIL:
                case COMPLETED:
                    // 보상로직 발동
                    couponService.restoreUsedCoupons(orderDTO.getMemberId(), orderDTO.getCouponInfos());
                    ack.acknowledge();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new ErrorResponseException("쿠폰 보상처리 중 에러");
        }
    }

    @KafkaListener(topics = "create-refund"/*KafkaTopic.CREATE_REFUND*/)  
    public void onRefundEvent(String message, Acknowledgment ack) {  
        try {  
            RefundDTO refundDto = objectMapper.readValue(message, RefundDTO.class);
            if (refundDto.getCouponInfoId() != null) {
                couponService.restoreUsedCoupons(refundDto.getMemberId(), List.of(refundDto.getCouponInfoId()));
            }
            ack.acknowledge();  
        } catch (Exception e) {  
            log.error(e.getMessage());  
        }  
    }
}
