package com.mall.search.listener;

import com.mall.search.document.ProductDocument;
import com.mall.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "product-events", consumerGroup = "search-consumer-group")
public class ProductSyncListener implements RocketMQListener<ProductDocument> {

    private final SearchService searchService;

    @Override
    public void onMessage(ProductDocument message) {
        log.info("Received product event for product id: {}", message.getId());
        if (message.getStatus() != null && message.getStatus() == 1) {
            searchService.indexProduct(message);
        } else {
            searchService.deleteProduct(message.getId());
        }
    }
}
