package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto dto, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestDtoOut toItemRequestDtoOut(ItemRequest request, List<Item> items) {
        return ItemRequestDtoOut.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items.stream()
                        .map(ItemRequestMapper::toItemDtoForRequest)
                        .toList())
                .build();
    }

    public static ItemRequestDtoOut toItemRequestDtoOut(ItemRequest request) {
        return toItemRequestDtoOut(request, List.of());
    }

    private static ItemRequestDtoOut.ItemDtoForRequest toItemDtoForRequest(Item item) {
        return ItemRequestDtoOut.ItemDtoForRequest.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner().getId())
                .build();
    }
}
