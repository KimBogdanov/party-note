package ru.kim.partynote.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kim.partynote.compilation.dto.CompilationDto;
import ru.kim.partynote.compilation.model.Compilation;
import ru.kim.partynote.event.dto.EventShortDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationDtoMapper {
    @Mapping(target = "events", source = "events")
    CompilationDto toDto(Compilation compilation, List<EventShortDto> events);
}
