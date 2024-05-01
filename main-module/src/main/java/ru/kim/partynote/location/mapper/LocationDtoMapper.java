package ru.kim.partynote.location.mapper;

import org.mapstruct.Mapper;
import ru.kim.partynote.location.dto.LocationDto;
import ru.kim.partynote.location.model.Location;

@Mapper(componentModel = "spring")
public interface LocationDtoMapper {
    Location toModel(LocationDto locationDto);
}
