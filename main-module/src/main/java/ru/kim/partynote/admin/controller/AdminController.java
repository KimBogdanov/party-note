package ru.kim.partynote.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kim.partynote.location.dto.LocationFullDto;
import ru.kim.partynote.location.dto.NewLocationDto;
import ru.kim.partynote.location.dto.UpdateLocationDto;
import ru.kim.partynote.location.service.LocationService;
import ru.kim.partynote.category.dto.CategoryUpdateDto;
import ru.kim.partynote.compilation.dto.CompilationDto;
import ru.kim.partynote.compilation.dto.NewCompilationDto;
import ru.kim.partynote.compilation.dto.UpdateCompilationRequest;
import ru.kim.partynote.compilation.service.CompilationService;
import ru.kim.partynote.event.dto.EventFullDto;
import ru.kim.partynote.event.dto.UpdateEventAdminRequestDto;
import ru.kim.partynote.event.model.enums.EventState;
import ru.kim.partynote.event.service.EventService;
import ru.kim.partynote.user.dto.UserDto;
import ru.kim.partynote.user.dto.UserShortDto;
import ru.kim.partynote.user.service.UserService;
import ru.kim.partynote.category.dto.CategoryDto;
import ru.kim.partynote.category.dto.CategoryShortDto;
import ru.kim.partynote.category.service.CategoryService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;
    private final LocationService locationService;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto saveUser(@Valid @RequestBody UserShortDto userShortDto) {
        log.info("Save user name: {}, email: {}", userShortDto.getName(), userShortDto.getEmail());
        return userService.saveUser(userShortDto);
    }

    @GetMapping("/users")
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                  @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {
        log.info("Get user id in: {}, from: {}, to: {}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Delete user id: {}", userId);
        userService.deleteUser(userId);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto saveCategory(@Valid @RequestBody CategoryShortDto categoryShortDto) {
        log.info("Save category name: {}", categoryShortDto.getName());
        return categoryService.saveCategory(categoryShortDto);
    }

    @PatchMapping("/categories/{categoryId}")
    public CategoryDto patchCategory(@Valid @RequestBody CategoryUpdateDto categoryUpdateDto,
                                     @PathVariable Long categoryId) {
        log.info("Patch category id: {}, name: {}", categoryId, categoryUpdateDto.getName());
        return categoryService.patchCategory(categoryId, categoryUpdateDto);
    }

    @DeleteMapping("/categories/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long categoryId) {
        log.info("Delete category id: {}", categoryId);
        categoryService.deleteCategory(categoryId);
    }

    @GetMapping("/events")
    public List<EventFullDto> getEventsForAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = PATTERN) LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {
        log.info("getEventsForAdmin for users ids: {} states: {} categories ids: {}", users, states, categories);
        return eventService.getAllEventsForAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("events/{eventId}")
    public EventFullDto adminPatchEvent(@RequestBody @Valid UpdateEventAdminRequestDto updateEventDto,
                                        @PathVariable Long eventId) {
        log.info("adminPatchEvent id: {}", eventId);
        return eventService.adminPatchEvent(eventId, updateEventDto);
    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto saveCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("saveCompilation events ids: {} pinned: {} title {}",
                newCompilationDto.getEvents(),
                newCompilationDto.getPinned(),
                newCompilationDto.getTitle());
        return compilationService.saveCompilation(newCompilationDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("deleteCompilation id: {}", compId);
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto patchCompilation(@PathVariable Long compId,
                                           @Valid @RequestBody UpdateCompilationRequest updateCompilation) {
        log.info("patchCompilation compilation id: {}, pinned: {} title: {}",
                compId,
                updateCompilation.getPinned(),
                updateCompilation.getTitle());
        return compilationService.updateCompilation(compId, updateCompilation);
    }

    @PostMapping("/locations")
    @ResponseStatus(HttpStatus.CREATED)
    public LocationFullDto saveLocation(@RequestBody @Valid NewLocationDto newLocationDto) {
        log.info("saveLocations name: {} lat: {} lon: {}",
                newLocationDto.getName(),
                newLocationDto.getLat(),
                newLocationDto.getLon());
        return locationService.saveLocation(newLocationDto);
    }

    @GetMapping("/locations/{locationId}")
    public LocationFullDto getLocationById(@PathVariable Long locationId) {
        log.info("getLocationById location id: {}", locationId);
        return locationService.getLocationById(locationId);
    }

    @PatchMapping("/locations/{locationId}")
    public LocationFullDto patchLocation(@PathVariable Long locationId,
                                         @Valid @RequestBody UpdateLocationDto updateLocationDto) {
        log.info("patchLocation location id: {}, name: {}",
                locationId,
                updateLocationDto.getName());
        return locationService.updateLocation(locationId, updateLocationDto);
    }

    @GetMapping("/locations/radius")
    public List<LocationFullDto> getLocationsByCoordinatesAndRadius(
            @RequestParam() Double lat,
            @RequestParam() Double lon,
            @RequestParam(required = false, defaultValue = "10") float radius,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {
        log.info("getLocationsByCoordinatesAndRadius where lat: {} lon: {} radius: {}", lat, lon, radius);
        return locationService.getLocationsByCoordinatesAndRadius(lat, lon, radius, from, size);
    }
}
