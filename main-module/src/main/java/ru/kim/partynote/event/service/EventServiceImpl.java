package ru.kim.partynote.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kim.partynote.dto.CreateStatisticDto;
import ru.kim.partynote.dto.ReadStatisticDto;
import ru.kim.partynote.location.model.Location;
import ru.kim.partynote.location.repository.LocationRepository;
import ru.kim.partynote.location.service.LocationService;
import ru.kim.partynote.event.dto.*;
import ru.kim.partynote.event.mapper.EventShortMapper;
import ru.kim.partynote.event.model.Event;
import ru.kim.partynote.event.model.enums.EventState;
import ru.kim.partynote.event.model.enums.SearchEventValues;
import ru.kim.partynote.exception.ConflictException;
import ru.kim.partynote.request.model.enums.RequestStatus;
import ru.kim.partynote.request.repository.RequestRepository;
import ru.kim.partynote.request.service.RequestService;
import ru.kim.partynote.user.model.User;
import ru.kim.partynote.category.model.Category;
import ru.kim.partynote.category.repository.CategoryRepository;
import ru.kim.partynote.event.mapper.EventFullDtoMapper;
import ru.kim.partynote.event.mapper.NewEventDtoMapper;
import ru.kim.partynote.event.repository.EventRepository;
import ru.kim.partynote.exception.ConditionsNotMetException;
import ru.kim.partynote.exception.NotFoundException;
import ru.kim.partynote.user.repository.UserRepository;
import ru.kim.partynote.util.DateTimeUtil;
import ru.kim.partynote.util.PageRequestFrom;
import ru.kim.service.StatisticClient;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final RequestService requestService;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final NewEventDtoMapper newEventDtoMapper;
    private final EventFullDtoMapper eventFullDtoMapper;
    private final EventShortMapper eventShortMapper;
    private final StatisticClient statisticClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public EventFullDto saveEvent(Long userId, NewEventDto newEventDto) {
        User user = getUserOrThrowNotFoundException(userId);
        checkTimeThrowNotCorrectTimeException(newEventDto.getEventDate(), 2);
        Category category = getCategoryOrThrowNotFoundException(newEventDto.getCategory());
        Location location = locationService.save(newEventDto.getLocation());

        return Optional.of(newEventDto)
                .map(dto -> newEventDtoMapper.toEvent(dto, category, location, user))
                .map(eventRepository::save)
                .map(event -> eventFullDtoMapper.toDto(event, 0, 0L))
                .get();
    }

    @Override
    @Transactional
    public EventFullDto adminPatchEvent(Long eventId, UpdateEventAdminRequestDto updateEventDto) {
        Event event = getEventOrThrowNotFoundException(eventId);

        if (updateEventDto.getAnnotation() != null) {
            event.setAnnotation(updateEventDto.getAnnotation());
        }
        if (updateEventDto.getCategory() != null) {
            event.setCategory(getCategoryOrThrowNotFoundException(updateEventDto.getCategory()));
        }
        if (updateEventDto.getDescription() != null) {
            event.setDescription(updateEventDto.getDescription());
        }
        if (updateEventDto.getEventDate() != null) {
            checkTimeThrowNotCorrectTimeException(updateEventDto.getEventDate(), 2);
            event.setEventDate(updateEventDto.getEventDate());
        }
        if (updateEventDto.getLocation() != null) {
            event.setLocation(locationService.save(updateEventDto.getLocation()));
        }
        if (updateEventDto.getPaid() != null) {
            event.setPaid(updateEventDto.getPaid());
        }
        if (updateEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        }
        if (updateEventDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventDto.getRequestModeration());
        }
        if (updateEventDto.getTitle() != null) {
            event.setTitle(updateEventDto.getTitle());
        }
        if (updateEventDto.getStateAction() != null) {
            if (event.getState().equals(EventState.PENDING)) {
                switch (updateEventDto.getStateAction()) {
                    case PUBLISH_EVENT:
                        checkTimeThrowNotCorrectTimeException(event.getEventDate(), 1);
                        event.setState(EventState.PUBLISHED);
                        break;
                    case REJECT_EVENT:
                        event.setState(EventState.CANCELED);
                        break;
                }
            } else {
                throw new ConflictException(
                        String.format("Cannot publish the event because it's not in the right state: %s",
                                event.getState()));
            }
        }
        event.setPublishedOn(LocalDateTime.now());
        return eventFullDtoMapper.toDto(event, 0, 0L);
    }

    @Override
    public EventFullDto patchEventForUser(Long userId, Long eventId,
                                          UpdateEventUserRequestDto eventUserRequestDto) {
        getUserOrThrowNotFoundException(userId);
        Event event = getEventOrThrowNotFoundException(eventId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (eventUserRequestDto.getAnnotation() != null) {
            event.setAnnotation(eventUserRequestDto.getAnnotation());
        }
        if (eventUserRequestDto.getCategory() != null) {
            event.setCategory(getCategoryOrThrowNotFoundException(eventUserRequestDto.getCategory()));
        }
        if (eventUserRequestDto.getDescription() != null) {
            event.setDescription(eventUserRequestDto.getDescription());
        }
        if (eventUserRequestDto.getEventDate() != null) {
            checkTimeThrowNotCorrectTimeException(eventUserRequestDto.getEventDate(), 2);
            event.setEventDate(eventUserRequestDto.getEventDate());
        }
        if (eventUserRequestDto.getLocation() != null) {
            event.setLocation(locationService.save(eventUserRequestDto.getLocation()));
        }
        if (eventUserRequestDto.getPaid() != null) {
            event.setPaid(eventUserRequestDto.getPaid());
        }
        if (eventUserRequestDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventUserRequestDto.getParticipantLimit());
        }
        if (eventUserRequestDto.getRequestModeration() != null) {
            event.setRequestModeration(eventUserRequestDto.getRequestModeration());
        }
        if (eventUserRequestDto.getTitle() != null) {
            event.setTitle(eventUserRequestDto.getTitle());
        }
        if (eventUserRequestDto.getStateAction() != null) {
            if (event.getState().equals(EventState.PENDING) || event.getState().equals(EventState.CANCELED)) {
                switch (eventUserRequestDto.getStateAction()) {
                    case SEND_TO_REVIEW:
                        event.setState(EventState.PENDING);
                        break;
                    case CANCEL_REVIEW:
                        event.setState(EventState.CANCELED);
                }
            } else {
                throw new ConditionsNotMetException(
                        String.format("Cannot publish the event because it's not in the right state: %s",
                                event.getState()));
            }
        }

        return eventFullDtoMapper.toDto(event, 0, 0L);
    }

    @Override
    public List<EventFullDto> getAllEventsForAdmin(List<Long> users,
                                                   List<EventState> states,
                                                   List<Long> categories,
                                                   LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd,
                                                   Integer from,
                                                   Integer size) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusYears(100);
        }

        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }
        Page<Event> events = eventRepository.getAllEventsForAdmin(
                rangeStart,
                rangeEnd,
                categories,
                states,
                users,
                new PageRequestFrom(from, size, null)
        );


        List<Long> eventsIds = getEventsId(events);
        Map<Long, Integer> countRequestsByEventId = requestService.countConfirmedRequestByEventId(eventsIds);
        Map<Long, Long> statisticMap = getEventStatisticMap(rangeStart, rangeEnd, eventsIds);

        return events.stream()
                .map(event -> eventFullDtoMapper.toDto(
                        event,
                        countRequestsByEventId.get(event.getId()) == null ? 0 : countRequestsByEventId.get(event.getId()),
                        statisticMap.get(event.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getAllEventsForOwner(Long userId, Integer from, Integer size) {
        getUserOrThrowNotFoundException(userId);
        Page<Event> events = eventRepository.findAllByInitiatorId(userId, new PageRequestFrom(from, size, null));
        List<Long> eventsIds = getEventsId(events);
        Map<Long, Integer> countRequestsByEventId = requestService.countConfirmedRequestByEventId(eventsIds);
        Map<Long, Long> statisticMap = getEventStatisticMap(LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100), eventsIds);
        return events.stream()
                .map(event -> eventShortMapper.toDto(event,
                        countRequestsByEventId.get(event.getId()) == null ? 0 : countRequestsByEventId.get(event.getId()),
                        statisticMap.get(event.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventForOwner(Long userId, Long eventId) {
        getUserOrThrowNotFoundException(userId);
        Event event = getEventOrThrowNotFoundException(eventId);
        checkIfUserIsEventOwnerAndThrowException(event, userId);

        Integer countRequest = requestRepository.countAllRequestByEventIdAndStatus(eventId,
                RequestStatus.CONFIRMED);
        Map<Long, Long> statisticMap = getEventStatisticMap(LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100), List.of(eventId));

        return eventFullDtoMapper.toDto(event, countRequest, statisticMap.get(eventId));
    }

    @Override
    public List<EventFullDto> getAllEventsForPublic(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            SearchEventValues sort,
            Integer from,
            Integer size,
            HttpServletRequest request) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }
        Page<Event> events = eventRepository.getAllEventsForPublic(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                EventState.PUBLISHED,
                new PageRequestFrom(from, size, sort.equals(SearchEventValues.UNSORTED) ?
                        null : Sort.by("eventDate"))
        );

        List<Long> eventsIds = getEventsId(events);
        Map<Long, Integer> countRequestsByEventId = requestService.countConfirmedRequestByEventId(eventsIds);
        Map<Long, Long> statisticMap = getEventStatisticMap(rangeStart, rangeEnd, eventsIds);

        statisticClient.saveHit(CreateStatisticDto.builder()
                .app("main")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now()).build());

        List<EventFullDto> collect = events.stream()
                .map(event -> eventFullDtoMapper.toDto(
                        event,
                        countRequestsByEventId.get(event.getId()) == null ? 0 : countRequestsByEventId.get(event.getId()),
                        statisticMap.get(event.getId())))
                .collect(Collectors.toList());
        if (sort.equals(SearchEventValues.VIEWS)) {
            return collect.stream()
                    .sorted(Comparator.comparingLong(EventFullDto::getViews))
                    .collect(Collectors.toList());
        }
        return collect;
    }

    @Override
    public EventFullDto getEventForPublic(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        Integer countRequest = requestRepository.countAllRequestByEventIdAndStatus(eventId,
                RequestStatus.CONFIRMED);
        Map<Long, Long> statisticMap = getEventStatisticMap(LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100), List.of(eventId));

        statisticClient.saveHit(CreateStatisticDto.builder()
                .app("main")
                .uri("/events/" + eventId)
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now()).build());

        return eventFullDtoMapper.toDto(event, countRequest, statisticMap.get(eventId));
    }

    @Override
    public List<EventShortDto> getAllEventsByLocation(Long locationId, Integer from, Integer size) {
        getLocationOrThrowNotFoundException(locationId);
        Page<Event> events = eventRepository.findByLocationIdAndState(
                locationId,
                EventState.PUBLISHED,
                new PageRequestFrom(from, size, null));

        List<Long> eventsIds = getEventsId(events);
        Map<Long, Integer> countRequestsByEventId = requestService.countConfirmedRequestByEventId(eventsIds);
        Map<Long, Long> statisticMap = getEventStatisticMap(LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100), eventsIds);
        return events.stream()
                .map(event -> eventShortMapper.toDto(event,
                        countRequestsByEventId.get(event.getId()) == null ? 0 : countRequestsByEventId.get(event.getId()),
                        statisticMap.get(event.getId())))
                .collect(Collectors.toList());
    }

    private Location getLocationOrThrowNotFoundException(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException(String.format("Location with id=%d was not found", locationId)));
    }

    private Map<Long, Long> getEventStatisticMap(LocalDateTime rangeStart, LocalDateTime rangeEnd, List<Long> eventsIds) {
        List<String> uris = eventsIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        ResponseEntity<Object> stats = statisticClient.getStats(rangeStart, rangeEnd, uris, true);
        List<ReadStatisticDto> statisticDtos;
        if (stats.getStatusCode().is2xxSuccessful()) {
            statisticDtos = objectMapper.convertValue(stats.getBody(), new TypeReference<>() {
            });
        } else {
            throw new RuntimeException(Objects.requireNonNull(stats.getBody()).toString());
        }

        return statisticDtos.stream()
                .collect(Collectors.toMap(
                        dto -> extractIdFromUri(dto.getUri()),
                        ReadStatisticDto::getHits,
                        Long::sum)
                );
    }

    private static List<Long> getEventsId(Page<Event> events) {
        return events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
    }

    private Long extractIdFromUri(String uri) {
        String[] parts = uri.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }

    private Event getEventOrThrowNotFoundException(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }

    private void checkTimeThrowNotCorrectTimeException(LocalDateTime dateTime, Integer hour) {
        if (dateTime.isBefore(LocalDateTime.now().plusHours(hour))) {
            throw new IllegalArgumentException(
                    String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s",
                            DateTimeUtil.formatLocalDateTime(dateTime)));
        }
    }

    private Category getCategoryOrThrowNotFoundException(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException(String.format("Category with id=%d was not found", categoryId))
        );
    }

    private User getUserOrThrowNotFoundException(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("User with id=%d was not found", userId))
        );
    }

    private void checkIfUserIsEventOwnerAndThrowException(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(
                    String.format("User with id=%d not owner of event id=%d", userId, event.getId())
            );
        }
    }
}
