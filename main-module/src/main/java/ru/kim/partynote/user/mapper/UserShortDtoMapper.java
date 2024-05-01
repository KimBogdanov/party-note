package ru.kim.partynote.user.mapper;

import org.mapstruct.Mapper;
import ru.kim.partynote.user.dto.UserShortDto;
import ru.kim.partynote.user.model.User;

@Mapper(componentModel = "Spring")
public interface UserShortDtoMapper {

    User mapToUser(UserShortDto userShortDto);
}
