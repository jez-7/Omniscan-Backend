package com.monitoreo.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "price_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo que representa un registro historico de una oferta")
public class PriceHistory {

    @Id
    @Schema(description = "ID autogenerador por MongoDB", example = "64b8f0c2e1d3f2a5b6c7d8e9")
    private String id;

    @Schema(description = "ID del producto", example = "1")
    private String productId;

    @Schema(description = "Precio detectado", example = "99.99")
    private Double price;

    @Schema(description = "Fecha y hora en que se detectó la oferta", example = "2024-06-01T12:00:00Z")
    private Date timestamp;

    @Schema(description = "Enlace al producto en la tienda", example = "https://dummyjson.com/products/1")
    private String permalink;

    @Schema(description = "URL de la imagen del producto", example = "https://dummyjson.com/image/i/products/1/thumbnail.jpg")
    private String thumbnail;

    @Schema(description = "Nombre del producto", example = "Asus VivoBook 15")
    private String productName;
}
