package ru.kim.partynote.user.mapper;

import org.mapstruct.Mapper;
import ru.kim.partynote.user.dto.UserDto;
import ru.kim.partynote.user.model.User;

@Mapper(componentModel = "Spring")
public interface UserDtoMapper {
    UserDto userToUserDto(User user);
}
