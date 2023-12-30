package com.dailyon.promotionservice.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DynamoDbConfig {

    @Value("${spring.data.dynamodb.endpoint}")
    private String amazonDynamoDbEndpoint;

    @Value("${spring.data.dynamodb.region}")
    private String amazonDynamoDbRegion;

    @Value("${spring.data.dynamodb.access-key}")
    private String amazonAwsAccessKey;

    @Value("${spring.data.dynamodb.secret-key}")
    private String amazonAwsSecretKey;

    @Bean
    public DynamoDbClient dynamoDbClient() throws URISyntaxException {
        return DynamoDbClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(amazonAwsAccessKey, amazonAwsSecretKey)))
                .region(Region.of(amazonDynamoDbRegion))
                .endpointOverride(new URI(amazonDynamoDbEndpoint))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}