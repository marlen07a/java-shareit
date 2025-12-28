package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDtoOut create(Long userId, ItemRequestDto itemRequestDto);
    
    List<ItemRequestDtoOut> getOwn(Long userId);
    
    List<ItemRequestDtoOut> getAll(Long userId);
    
    ItemRequestDtoOut getById(Long userId, Long requestId);
}
