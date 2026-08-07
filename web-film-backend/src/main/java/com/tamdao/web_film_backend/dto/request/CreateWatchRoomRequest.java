package com.tamdao.web_film_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateWatchRoomRequest {
    
    @NotBlank(message = "Tên phòng không được để trống")
    private String name;

    @NotNull(message = "Mã phim không được để trống")
    private Long movieId;

    private Long episodeId;

    @NotBlank(message = "Loại phòng không được để trống (PUBLIC/PRIVATE)")
    private String roomType;

    @Min(value = 2, message = "Số lượng thành viên tối thiểu là 2")
    @Max(value = 50, message = "Số lượng thành viên tối đa là 50")
    private Integer maxMembers = 50;
}
