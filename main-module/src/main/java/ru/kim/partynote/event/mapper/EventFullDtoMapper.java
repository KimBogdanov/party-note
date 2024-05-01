package ru.kim.partynote.event.mapper;

import org.mapstruct.Mapper;
import ru.kim.partynote.location.mapper.LocationDtoMapper;
import ru.kim.partynote.category.mapper.CategoryDtoMapper;
import ru.kim.partynote.event.dto.EventFullDto;
import ru.kim.partynote.event.model.Event;
import ru.kim.partynote.user.mapper.UserShortDtoMapper;

@Mapper(componentModel = "spring", uses = {CategoryDtoMapper.class, LocationDtoMapper.class, UserShortDtoMapper.class})
public interface EventFullDtoMapper {
    EventFullDto toDto(Event event, Integer confirmedRequests, Long views);
}
