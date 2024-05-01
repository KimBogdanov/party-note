package ru.kim.partynote.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kim.partynote.request.dto.ParticipationRequestDto;
import ru.kim.partynote.request.model.Request;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {
    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    ParticipationRequestDto toDto(Request request);
}
