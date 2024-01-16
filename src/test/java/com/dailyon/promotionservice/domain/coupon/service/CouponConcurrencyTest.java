// package com.dailyon.promotionservice.domain.coupon.service;

// import static org.junit.jupiter.api.Assertions.assertEquals;

// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.time.LocalDateTime;

// import javax.persistence.EntityManager;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.MethodOrderer;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.TestMethodOrder;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.redis.core.RedisTemplate;

// import org.springframework.test.context.junit.jupiter.SpringExtension;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;

// import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
// import com.dailyon.promotionservice.domain.coupon.entity.enums.DiscountType;
// import com.dailyon.promotionservice.domain.coupon.repository.CouponAppliesToRepository;
// import com.dailyon.promotionservice.domain.coupon.repository.CouponInfoRepository;
// import com.dailyon.promotionservice.domain.coupon.repository.MemberCouponRepository;
// import com.fasterxml.jackson.databind.ObjectMapper;


// @ExtendWith(SpringExtension.class)
// @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// @SpringBootTest
// @ActiveProfiles(value = {"test"})
// public class CouponConcurrencyTest {

//     @Autowired EntityManager em;

//     @Autowired CouponService couponService;

//     @Autowired MemberCouponRepository memberCouponRepository;
//     @Autowired CouponInfoRepository couponInfoRepository;
//     @Autowired CouponAppliesToRepository couponAppliesToRepository;

//     @Autowired RedisTemplate<String, String> redisTemplate;
//     @Autowired ObjectMapper objectMapper;

//     private CouponInfo savedCoupon;

//     @BeforeEach
//     void setUp() {
//         CouponInfo newCoupon = CouponInfo.builder()
//                 .name("테스트 쿠폰")
//                 .discountType(DiscountType.FIXED_AMOUNT)
//                 .discountValue(1000L)
//                 .startAt(LocalDateTime.now())
//                 .endAt(LocalDateTime.now().plusDays(30))
//                 .issuedQuantity(10000)
//                 .remainingQuantity(10000)
//                 .requiresConcurrencyControl(true)
//                 .minPurchaseAmount(5000L)
//                 .build();
//         savedCoupon = couponInfoRepository.save(newCoupon);
//     }

//     @AfterEach
//     void tearDown() {
//         memberCouponRepository.deleteAllInBatch();
//         couponInfoRepository.deleteAllInBatch();
//     }



//     @Test
//     @DisplayName("쿠폰 다운로드 동시성 - 같은 멤버 id로 100개 thread가 각 100번, 총 10000회 요청")
//     void couponDownloadConcurrencyControl() throws InterruptedException {
//         Long couponId = savedCoupon.getId();
//         Long memberId = 1L;
//         int threadCount = 10;
//         int downloadAttemptsPerThread = 30;
//         int totalAttempts = threadCount * downloadAttemptsPerThread;

//         ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//         CountDownLatch latch = new CountDownLatch(totalAttempts);

//         // CouponInfo 생성 -> id -> 1로 issuedQuantity는 10000, remainingQuantity 10000개로 insert
//         for (int i = 0; i < totalAttempts; i++) {
//             executorService.submit(() -> {
//                 try {
//                     couponService.downloadCoupon(memberId, couponId);
//                 } catch (Exception e) {
//                     System.out.println("테스트 중 에러 예외 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//                 } finally {
//                     latch.countDown();
//                 }
//             });
//         }
//         latch.await();
//         executorService.shutdown();

//         Integer issuedQ = couponInfoRepository.findById(couponId).get().getIssuedQuantity();
//         Integer remainingQ = couponInfoRepository.findById(couponId).get().getRemainingQuantity();
//         Integer issuedListSize = memberCouponRepository.findByMemberId(1L).size();
//         System.out.println("1번 동시성 테스트 종료 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//         System.out.println(issuedListSize);
//         System.out.println(issuedQ);
//         System.out.println(remainingQ);
//         assertEquals(1, issuedListSize);
//         assertEquals(1, issuedQ - remainingQ);
//     }
    
// }
