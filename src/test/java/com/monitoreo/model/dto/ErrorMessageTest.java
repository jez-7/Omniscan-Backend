package com.monitoreo.model.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ErrorMessageTest {

    @Test
    void testErrorMessageStructure() {
        LocalDateTime now = LocalDateTime.now();

        ErrorMessage error = new ErrorMessage("Test Message");
        error.setStatus(404);
        error.setPath("/api/prices");
        error.setTimeStamp(now);

        assertEquals("Test Message", error.getMessage());
        assertEquals(404, error.getStatus());
        assertEquals("/api/prices", error.getPath());
        assertEquals(now, error.getTimeStamp());


        ErrorMessage built = ErrorMessage.builder()
                .message("Builder Message")
                .status(500)
                .path("/error")
                .timeStamp(now)
                .build();

        assertNotNull(built);
        assertEquals(500, built.getStatus());
    }
}