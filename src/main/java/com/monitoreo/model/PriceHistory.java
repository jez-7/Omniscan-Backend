package com.monitoreo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "price_history")
@Data
public class PriceHistory {
    @Id
    private String id;
    private String productId;
    private Double price;
    private Date timestamp;
}
