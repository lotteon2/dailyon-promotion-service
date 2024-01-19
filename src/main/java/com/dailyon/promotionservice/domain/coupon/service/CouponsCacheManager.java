package com.dailyon.promotionservice.domain.coupon.service;

import com.dailyon.promotionservice.domain.coupon.entity.enums.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.service.response.MultipleProductCouponsResponse;
import com.dailyon.promotionservice.domain.coupon.api.request.MultipleProductsCouponRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponsCacheManager {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private static final String COUPONS_CACHE_PREFIX = "multiple-products-coupons::";
    private static final long COUPONS_CACHE_TTL_MINUTES = 3;



    public MultipleProductCouponsResponse getCachedCoupons(MultipleProductsCouponRequest request) {
        String cacheKey = buildCacheKey(request.getProducts());
        String jsonData = stringRedisTemplate.opsForValue().get(cacheKey);
        if (jsonData != null) {
            try {
                return objectMapper.readValue(jsonData, MultipleProductCouponsResponse.class);
            } catch (JsonProcessingException e) {
                log.error(e.toString());
                return null;
            }
        }
        return null;
    }

    public void cacheCoupons(MultipleProductsCouponRequest request, MultipleProductCouponsResponse response) {
        String cacheKey = buildCacheKey(request.getProducts());
        try {
            String jsonData = objectMapper.writeValueAsString(response);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, COUPONS_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error(e.toString());
        }
    }

    public void evictCouponsRelatedToProduct(Long productId) {
        String pattern = COUPONS_CACHE_PREFIX + "*" + productId + ";*";
        Set<String> keys = new HashSet<>();

        // Redis `SCAN` command 사용해서 패턴 찾음
        Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory().getConnection()
                .scan(ScanOptions.scanOptions().match(pattern).count(1000).build());

        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
        }

        // 패턴 매칭되면 모두 삭제
        stringRedisTemplate.delete(keys);
    }

    public void evictCouponsRelatedToCategory(Long categoryId) {
        String pattern = COUPONS_CACHE_PREFIX + "*;" + categoryId + "*";
        Set<String> keys = new HashSet<>();

        // Redis `SCAN` command 사용해서 패턴 찾음
        Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory().getConnection()
                .scan(ScanOptions.scanOptions().match(pattern).count(1000).build());

        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
        }

        // 패턴 매칭되면 모두 삭제
        stringRedisTemplate.delete(keys);
    }

    public void evictCacheForAppliesTo(CouponTargetType appliesToType, Long appliesToId) {
        if (appliesToType == CouponTargetType.PRODUCT) {
            evictCouponsRelatedToProduct(appliesToId);
        } else if (appliesToType == CouponTargetType.CATEGORY) {
            evictCouponsRelatedToCategory(appliesToId);
        }
    }


    // 예시로 들어가는 key -> multiple-products-coupons::4;16-8;23-15;42
    // [{productId:4, categoryId:16}, {productId:8, categoryId:23} ...]
    private String buildCacheKey(List<MultipleProductsCouponRequest.ProductCategoryPair> products) {
        return COUPONS_CACHE_PREFIX + products.stream()
                .map(p -> p.getProductId() + ";" + p.getCategoryId())
                .collect(Collectors.joining("-"));
    }
}