package ru.kim.partynote.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kim.partynote.event.model.Event;
import ru.kim.partynote.event.model.enums.EventState;
import ru.kim.partynote.event.repository.EventRepository;
import ru.kim.partynote.exception.ConflictException;
import ru.kim.partynote.exception.NotFoundException;
import ru.kim.partynote.request.dto.EventRequestStatusUpdateRequestDto;
import ru.kim.partynote.request.dto.EventRequestStatusUpdateResultDto;
import ru.kim.partynote.request.dto.ParticipationRequestDto;
import ru.kim.partynote.request.model.enums.RequestStatus;
import ru.kim.partynote.request.mapper.ParticipationRequestMapper;
import ru.kim.partynote.request.model.Request;
import ru.kim.partynote.request.repository.RequestRepository;
import ru.kim.partynote.user.model.User;
import ru.kim.partynote.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto saveRequest(Long userId, Long eventId) {
        User user = getUserOrThrowNotFoundException(userId);
        Event event = getEventOrThrowNotFoundException(eventId);

        checkIfUserIsEventInitiatorAndThrowException(userId, event.getInitiator().getId());
        checkIfEventIsPublishedAndThrowException(eventId, event.getState());
        checkIfParticipantLimitFullAndThrowException(event);

        return Optional.of(createRequest(user, event))
                .map(requestRepository::save)
                .map(participationRequestMapper::toDto)
                .get();
    }

    @Override
    public List<ParticipationRequestDto> getAllRequestsForRequester(Long userId) {
        getUserOrThrowNotFoundException(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestsId) {
        getUserOrThrowNotFoundException(userId);
        Request request = getRequestOrThrowNotFoundException(requestsId);

        checkIfRequesterIsOwnerAndThrowException(userId, request.getRequester().getId(), requestsId);

        request.setStatus(RequestStatus.CANCELED);
        return participationRequestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getAllRequestsByEventId(Long userId, Long eventId) {
        getUserOrThrowNotFoundException(userId);
        Event event = getEventOrThrowNotFoundException(eventId);
        checkIfUserIsEventOwnerAndThrowException(event, userId);

        return requestRepository.findAllByEventId(eventId).stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto updateStatusRequest(
            EventRequestStatusUpdateRequestDto statusUpdateRequestDto, Long userId, Long eventId) {

        getUserOrThrowNotFoundException(userId);
        Event event = getEventOrThrowNotFoundException(eventId);
        checkIfUserIsEventOwnerAndThrowException(event, userId);

        if (statusUpdateRequestDto.getStatus().equals(RequestStatus.CONFIRMED)) {
            if (event.getParticipantLimit() != 0) {
                Integer countRequestsLimit = requestRepository.countAllByEventIdAndStatus(event.getId(),
                        RequestStatus.CONFIRMED) + statusUpdateRequestDto.getRequestIds().size();

                if (countRequestsLimit > event.getParticipantLimit()) {
                    throw new ConflictException("The participant limit has been reached");
                }
                List<Request> requests = updateStatusRequest(statusUpdateRequestDto, eventId);

                EventRequestStatusUpdateResultDto result = EventRequestStatusUpdateResultDto.builder()
                        .confirmedRequests(requests.stream()
                                .map(participationRequestMapper::toDto)
                                .collect(Collectors.toList())).build();

                if (countRequestsLimit.equals(event.getParticipantLimit())) {
                    List<Request> otherRequests = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
                    otherRequests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
                    requestRepository.saveAll(otherRequests);

                    result.setRejectedRequests(otherRequests.stream()
                            .map(participationRequestMapper::toDto)
                            .collect(Collectors.toList()));

                }
                return result;
            } else {
                List<Request> requests = updateStatusRequest(statusUpdateRequestDto, eventId);
                return EventRequestStatusUpdateResultDto.builder()
                        .confirmedRequests(requests.stream()
                                .map(participationRequestMapper::toDto)
                                .collect(Collectors.toList())).build();
            }
        } else {
            List<Request> rejectedRequest = updateStatusRequest(statusUpdateRequestDto, eventId);

            return EventRequestStatusUpdateResultDto.builder()
                    .rejectedRequests(rejectedRequest.stream()
                            .map(participationRequestMapper::toDto)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Override
    public Map<Long, Integer> countConfirmedRequestByEventId(List<Long> eventIds) {
        List<Request> eventsByIdInAndStatus = requestRepository.findAllByEventIdInAndStatus(eventIds,
                RequestStatus.CONFIRMED);

        return eventsByIdInAndStatus.stream()
                .collect(Collectors.groupingBy(
                        request -> request.getEvent().getId(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue))
                );
    }

    private List<Request> updateStatusRequest(EventRequestStatusUpdateRequestDto statusUpdateRequestDto, Long eventId) {
        List<Request> requests = requestRepository.findAllByIdInAndEventId(statusUpdateRequestDto.getRequestIds(),
                eventId);
        requests.forEach(request -> {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Request must have status PENDING");
            }
            request.setStatus(statusUpdateRequestDto.getStatus());
        });
        requestRepository.saveAll(requests);
        return requests;
    }

    private void checkIfUserIsEventOwnerAndThrowException(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(
                    String.format("User with id=%d not owner of event id=%d", userId, event.getId())
            );
        }
    }

    private void checkIfRequesterIsOwnerAndThrowException(Long userId, Long requesterId, Long requestsId) {
        if (!requesterId.equals(userId)) {
            throw new ConflictException(
                    String.format("User with id=%d not owner of request id=%d", userId, requestsId)
            );
        }
    }

    private Request getRequestOrThrowNotFoundException(Long requestsId) {
        return requestRepository.findById(requestsId)
                .orElseThrow(
                        () -> new NotFoundException(String.format("Request with id=%d was not found", requestsId))
                );
    }

    private void checkIfParticipantLimitFullAndThrowException(Event event) {
        if (event.getParticipantLimit() != 0 &&
                requestRepository.countAllByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED)
                        .equals(event.getParticipantLimit())) {
            throw new ConflictException(
                    String.format("The limit of participants has been reached for the event with id=%d", event.getId())
            );
        }
    }

    private static void checkIfEventIsPublishedAndThrowException(Long eventId, EventState state) {
        if (!state.equals(EventState.PUBLISHED)) {
            throw new ConflictException(
                    String.format("Event with id=%d is not published", eventId)
            );
        }
    }

    private void checkIfUserIsEventInitiatorAndThrowException(Long userId, Long eventId) {
        if (eventId.equals(userId)) {
            throw new ConflictException(
                    String.format("User with id=%d is initiator event id=%d", userId, eventId)
            );
        }
    }

    private Request createRequest(User user, Event event) {
        return Request.builder()
                .created(LocalDateTime.now().withNano(0))
                .event(event)
                .requester(user)
                .status((event.getRequestModeration() && event.getParticipantLimit() != 0) ?
                        RequestStatus.PENDING : RequestStatus.CONFIRMED).build();
    }

    private Event getEventOrThrowNotFoundException(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }

    private User getUserOrThrowNotFoundException(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("User with id=%d was not found", userId))
        );
    }
}
