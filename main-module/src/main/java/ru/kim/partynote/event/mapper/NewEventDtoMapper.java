package ru.kim.partynote.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kim.partynote.location.model.Location;
import ru.kim.partynote.user.model.User;
import ru.kim.partynote.category.model.Category;
import ru.kim.partynote.event.dto.NewEventDto;
import ru.kim.partynote.event.model.Event;
import ru.kim.partynote.event.model.enums.EventState;

@Mapper(componentModel = "spring")
public interface NewEventDtoMapper {
    default EventState mapToPendingState(Object source) {
        return EventState.PENDING;
    }

    @Mapping(target = "state", expression = "java(mapToPendingState(newEventDto))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "description", source = "newEventDto.description")
    @Mapping(target = "paid", source = "newEventDto.paid", defaultValue = "false")
    @Mapping(target = "participantLimit", source = "newEventDto.participantLimit", defaultValue = "0")
    @Mapping(target = "requestModeration", source = "newEventDto.requestModeration", defaultValue = "true")
    @Mapping(target = "publishedOn", ignore = true)
    Event toEvent(NewEventDto newEventDto, Category category, Location location, User initiator);
}
