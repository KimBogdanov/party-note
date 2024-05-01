package ru.kim.partynote.event.mapper;

import org.mapstruct.Mapper;
import ru.kim.partynote.location.mapper.LocationDtoMapper;
import ru.kim.partynote.category.mapper.CategoryDtoMapper;
import ru.kim.partynote.event.dto.EventShortDto;
import ru.kim.partynote.event.model.Event;
import ru.kim.partynote.user.mapper.UserShortDtoMapper;

@Mapper(componentModel = "spring", uses = {CategoryDtoMapper.class, LocationDtoMapper.class, UserShortDtoMapper.class})
public interface EventShortMapper {
    EventShortDto toDto(Event event, Integer confirmedRequests, Long views);
}
