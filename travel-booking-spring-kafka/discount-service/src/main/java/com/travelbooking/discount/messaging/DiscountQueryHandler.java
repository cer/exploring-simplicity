package com.travelbooking.discount.messaging;

import com.travelbooking.common.Constants;
import com.travelbooking.discount.domain.Discount;
import com.travelbooking.discount.service.DiscountCalculator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DiscountQueryHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(DiscountQueryHandler.class);
    
    private final DiscountCalculator discountCalculator;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public DiscountQueryHandler(DiscountCalculator discountCalculator, 
                               KafkaTemplate<String, Object> kafkaTemplate) {
        this.discountCalculator = discountCalculator;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @KafkaListener(topics = Constants.Topics.DISCOUNT_SERVICE_QUERIES)
    public void handleDiscountQuery(ConsumerRecord<String, Object> record) {
        String correlationId = record.key();
        logger.info("Received discount query with correlationId: {}", correlationId);
        
        if (!(record.value() instanceof DiscountQueryCommand command)) {
            logger.warn("Received unexpected message type: {}", record.value().getClass().getName());
            return;
        }
        
        try {
            Optional<Discount> discount = discountCalculator.calculateDiscount(command.discountCode());
            
            DiscountQueryReply reply;
            if (discount.isPresent()) {
                Discount d = discount.get();
                logger.info("Found valid discount for code '{}': {}%", 
                    command.discountCode(), d.getPercentage());
                    
                reply = new DiscountQueryReply(
                    correlationId,
                    true,
                    d.getPercentage(),
                    d.getCode(),
                    d.getValidUntil(),
                    null
                );
            } else {
                logger.info("No valid discount found for code: {}", command.discountCode());
                
                reply = new DiscountQueryReply(
                    correlationId,
                    false,
                    null,
                    null,
                    null,
                    "Discount code not found or expired: " + command.discountCode()
                );
            }
            
            kafkaTemplate.send(Constants.Topics.DISCOUNT_SERVICE_REPLIES, correlationId, reply);
            logger.debug("Sent discount reply for correlationId: {}", correlationId);
            
        } catch (Exception e) {
            logger.error("Error processing discount query for correlationId: {}", correlationId, e);
            
            DiscountQueryReply errorReply = new DiscountQueryReply(
                correlationId,
                false,
                null,
                null,
                null,
                "Error processing discount query: " + e.getMessage()
            );
            
            kafkaTemplate.send(Constants.Topics.DISCOUNT_SERVICE_REPLIES, correlationId, errorReply);
        }
    }
    
}