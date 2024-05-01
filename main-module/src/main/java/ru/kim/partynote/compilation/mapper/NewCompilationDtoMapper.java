package ru.kim.partynote.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kim.partynote.compilation.dto.NewCompilationDto;
import ru.kim.partynote.compilation.model.Compilation;
import ru.kim.partynote.event.model.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NewCompilationDtoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    @Mapping(target = "pinned", defaultValue = "false")
    Compilation toModel(NewCompilationDto newCompilationDto, List<Event> events);
}
