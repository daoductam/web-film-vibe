package com.tamdao.web_film_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SyncCommand {
    
    @NotBlank(message = "Hành động không được để trống (PLAY/PAUSE/SEEK/HEARTBEAT)")
    private String action;

    @NotNull(message = "Thời gian video không được để trống")
    private Double timestamp;
}
