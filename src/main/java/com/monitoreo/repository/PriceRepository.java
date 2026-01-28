package com.monitoreo.repository;

import com.monitoreo.model.entity.PriceHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends MongoRepository<PriceHistory, String> {
    List<PriceHistory> findByProductIdOrderByTimestampDesc(String productId);
}
