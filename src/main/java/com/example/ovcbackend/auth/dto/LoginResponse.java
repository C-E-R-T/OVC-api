package com.example.ovcbackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    @Schema(name = "accessToken", example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ1c2VyMkBleGFtcGxlLmNvbSIsInJvbGUiOiJST0xFX1VTRVIiLCJpYXQiOjE3NzMxODY2MTEsImV4cCI6MTc3MzE5MDIxMX0.8BptJD5IJpQ9MGqDeHpon6YazGbCKM9c6p-XklHbOM_odWabrGxqaDaU6l6mrg1f")
    private String accessToken;
    @Schema(name = "refreshToken", example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ1c2VyMkBleGFtcGxlLmNvbSIsImlhdCI6MTc3MzE4NjYxMSwiZXhwIjoxNzc0Mzk2MjExfQ.GMvO304r6uKE793oATB6jbXFX4lgdvcyteQN3gefRG2Gu2dLxsSHcSzjI2Gzoqij")
    private String refreshToken;
}
