package com.dailyon.promotionservice.common.util;

import com.dailyon.promotionservice.common.exceptions.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
public class RedisDistributedLockManager {

    private final RedissonClient redissonClient;

    public interface LockCallback<T> {
        T doInLock();
    }

    public <T> T lock(String lockKey, LockCallback<T> callback) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(10, 2, TimeUnit.SECONDS);
            if (!isLocked) {
                // 다른 곳에서 락이 이미 사용 중일 때 발생 ( 2초동안 pub/sub으로 시도하다가 실패하면 예외 던짐 )
                throw new ErrorResponseException("락 획득 실패. 다른 요청이 락을 이미 보유하고 있거나 시스템에 지연이 있을 수 있음. - key: " + lockKey);
            }
            return callback.doInLock(); // 락 획득 후 진행하는 로직 추상화
        } catch (InterruptedException e) {
            // 다른 쓰레드가 대기 중인 현재 쓰레드를 방해할 때 발생
            Thread.currentThread().interrupt();
            throw new IllegalStateException("대기 중인 락이 인터럽트 됨. 서비스가 종료되거나 재시작되는 등의 상황에서 발생할 수 있음. - key: " + lockKey);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}