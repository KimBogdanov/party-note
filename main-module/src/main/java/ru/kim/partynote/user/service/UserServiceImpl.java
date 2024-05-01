package ru.kim.partynote.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kim.partynote.user.dto.UserDto;
import ru.kim.partynote.user.dto.UserShortDto;
import ru.kim.partynote.user.mapper.UserDtoMapper;
import ru.kim.partynote.user.mapper.UserShortDtoMapper;
import ru.kim.partynote.user.model.User;
import ru.kim.partynote.user.repository.UserRepository;
import ru.kim.partynote.exception.NotFoundException;
import ru.kim.partynote.util.PageRequestFrom;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserShortDtoMapper userShortDtoMapper;
    private final UserDtoMapper userDtoMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto saveUser(UserShortDto userShortDto) {
        return Optional.of(userShortDto)
                .map(userShortDtoMapper::mapToUser)
                .map(userRepository::save)
                .map(userDtoMapper::userToUserDto)
                .get();
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Page<User> result;
        if (ids != null) {
            result = userRepository.findAllByIdIn(ids, new PageRequestFrom(from, size, null));
        } else {
            result = userRepository.findAll(new PageRequestFrom(from, size, null));
        }
        return result.getContent().stream()
                .map(userDtoMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId));
        }
        userRepository.deleteById(userId);
    }
}
