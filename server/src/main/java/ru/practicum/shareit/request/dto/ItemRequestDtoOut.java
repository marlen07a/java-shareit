package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestDtoOut {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemDtoForRequest> items;

    @Data
    @Builder
    public static class ItemDtoForRequest {
        private Long id;
        private String name;
        private Long ownerId;
    }
}
