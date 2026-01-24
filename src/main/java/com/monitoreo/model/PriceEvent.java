package com.monitoreo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceEvent {
    private String productId;
    private String productName;
    private Double price;
    private String permalink;
    private String thumbnail;
    private Long timestamp;


}
