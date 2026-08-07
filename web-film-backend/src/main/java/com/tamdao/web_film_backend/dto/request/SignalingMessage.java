package com.tamdao.web_film_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignalingMessage {
    
    @NotBlank(message = "Loại tín hiệu không được để trống (OFFER/ANSWER/ICE_CANDIDATE)")
    private String type;

    private Long targetUserId;

    @NotBlank(message = "Nội dung SDP hoặc ICE candidate không được để trống")
    private String payload;
}
