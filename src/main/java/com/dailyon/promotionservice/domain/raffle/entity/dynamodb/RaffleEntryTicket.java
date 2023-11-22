package com.dailyon.promotionservice.domain.raffle.entity.dynamodb;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@DynamoDbBean
public class RaffleEntryTicket {

    @Setter(AccessLevel.NONE) // 생성제한
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private Boolean isUsed;

    public RaffleEntryTicket() { // 생성자로 묶어뒀음.
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    @DynamoDbSortKey
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
