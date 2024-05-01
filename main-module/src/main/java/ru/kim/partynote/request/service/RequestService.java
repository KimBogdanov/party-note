package ru.kim.partynote.request.service;

import ru.kim.partynote.request.dto.EventRequestStatusUpdateRequestDto;
import ru.kim.partynote.request.dto.EventRequestStatusUpdateResultDto;
import ru.kim.partynote.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface RequestService {
    ParticipationRequestDto saveRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getAllRequestsForRequester(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestsId);

    List<ParticipationRequestDto> getAllRequestsByEventId(Long userId, Long eventId);

    EventRequestStatusUpdateResultDto updateStatusRequest(
            EventRequestStatusUpdateRequestDto statusUpdateRequestDto, Long userId, Long eventId);

    Map<Long, Integer> countConfirmedRequestByEventId(List<Long> eventIds);
}
