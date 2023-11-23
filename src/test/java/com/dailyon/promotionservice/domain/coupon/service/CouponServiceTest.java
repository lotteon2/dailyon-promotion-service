package com.dailyon.promotionservice.domain.coupon.service;

import com.dailyon.promotionservice.domain.coupon.repository.CouponAppliesToRepository;
import com.dailyon.promotionservice.domain.coupon.repository.CouponInfoRepository;
import com.dailyon.promotionservice.domain.coupon.repository.MemberCouponRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;

@SpringBootTest
public class CouponServiceTest {
    @Autowired EntityManager em;
    @Autowired CouponService couponService;
    @Autowired CouponInfoRepository couponInfoRepository;
    @Autowired CouponAppliesToRepository couponAppliesToRepository;
    @Autowired MemberCouponRepository memberCouponRepository;

//    @Autowired RedisTemplate<String, String> redisTemplate;
    @Autowired ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        couponInfoRepository.deleteAllInBatch();
        couponAppliesToRepository.deleteAllInBatch();
        memberCouponRepository.deleteAllInBatch();
    }


    @DisplayName("쿠폰 정보를 입력받아 쿠폰을 생성한다.")
    @Test
    void createCouponInfoWithAppliesTo() throws Exception {
        // given
//        Category category1 = createCategory(null, "냉장");
//        Category category2 = createCategory(category1, "과일");
//        categoryRepository.saveAll(List.of(category1, category2));
//        Long categoryId = category2.getId();
//        ProductCreateRequest request =
//                new ProductCreateRequest(categoryId, "충주사과", "thumbnail.jpeg", "detail", 5000, "P001");

        // when
//        Long productId = productService.createProduct(request);

        // then
//        assertThat(productId).isNotNull();
    }
}
