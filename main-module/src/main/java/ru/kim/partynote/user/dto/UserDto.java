package ru.kim.partynote.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
    private final Long id;
    private final String name;
    private final String email;
}
