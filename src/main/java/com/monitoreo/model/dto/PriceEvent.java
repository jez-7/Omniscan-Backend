package com.monitoreo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Modelo que representa un evento de precio recibido desde el microservicio de scraping")
public class PriceEvent {
    private String productId;
    private String productName;
    private Double price;
    private String permalink;
    private String thumbnail;
    private Long timestamp;


}
