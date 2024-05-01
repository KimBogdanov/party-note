package ru.kim.partynote.location.mapper;

import org.mapstruct.Mapper;
import ru.kim.partynote.location.dto.NewLocationDto;
import ru.kim.partynote.location.model.Location;

@Mapper(componentModel = "spring")
public interface NewLocationDtoMapper {
    Location toModel(NewLocationDto newLocationDto);
}
