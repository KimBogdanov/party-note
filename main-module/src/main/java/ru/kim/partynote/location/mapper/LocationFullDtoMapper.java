package ru.kim.partynote.location.mapper;

import org.mapstruct.Mapper;
import ru.kim.partynote.location.dto.LocationFullDto;
import ru.kim.partynote.location.model.Location;

@Mapper(componentModel = "spring")
public interface LocationFullDtoMapper {
    LocationFullDto toDto(Location location);
}
