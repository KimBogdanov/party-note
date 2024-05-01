package ru.kim.partynote.user.service;

import ru.kim.partynote.user.dto.UserDto;
import ru.kim.partynote.user.dto.UserShortDto;

import java.util.List;

public interface UserService {
    UserDto saveUser(UserShortDto userShortDto);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);
}
