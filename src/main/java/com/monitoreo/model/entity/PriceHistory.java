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
@Schema(description = "Model representing a historical record of a detected deal")
public class PriceHistory {

    @Id
    @Schema(description = "Auto-generated ID by MongoDB", example = "64b8f0c2e1d3f2a5b6c7d8e9")
    private String id;

    @Schema(description = "Product ID", example = "1")
    private String productId;

    @Schema(description = "Detected price", example = "99.99")
    private Double price;

    @Schema(description = "Date and time the deal was detected", example = "2024-06-01T12:00:00Z")
    private Date timestamp;

    @Schema(description = "Link to the product in the store", example = "https://dummyjson.com/products/1")
    private String permalink;

    @Schema(description = "URL of the product image", example = "https://dummyjson.com/image/i/products/1/thumbnail.jpg")
    private String thumbnail;

    @Schema(description = "Product name", example = "Asus VivoBook 15")
    private String productName;
}
