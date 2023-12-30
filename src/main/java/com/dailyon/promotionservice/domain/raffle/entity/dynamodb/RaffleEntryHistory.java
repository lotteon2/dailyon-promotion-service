package com.dailyon.promotionservice.domain.raffle.entity.dynamodb;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDateTime;

@Data
@DynamoDbBean // v2
public class RaffleEntryHistory {
    private Long raffleId;
    private String raffleEntryTicketId;
    private LocalDateTime entryTimestamp;
    private Long raffleProductId;
    private LocalDateTime expireAt;

    @DynamoDbPartitionKey
    public Long getRaffleId() {
        return raffleId;
    }

    @DynamoDbSortKey
    public LocalDateTime getEntryTimestamp() {
        return entryTimestamp;
    }

}
