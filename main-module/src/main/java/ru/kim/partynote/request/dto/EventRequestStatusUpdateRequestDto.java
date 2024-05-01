package ru.kim.partynote.request.dto;

import lombok.Builder;
import lombok.Getter;
import ru.kim.partynote.request.model.enums.RequestStatus;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Builder
public class EventRequestStatusUpdateRequestDto {
    @NotNull
    private final List<Long> requestIds;
    @NotNull
    private final RequestStatus status;
}
