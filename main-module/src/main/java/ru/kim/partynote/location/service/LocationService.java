package ru.kim.partynote.location.service;

import ru.kim.partynote.location.dto.LocationDto;
import ru.kim.partynote.location.dto.LocationFullDto;
import ru.kim.partynote.location.dto.NewLocationDto;
import ru.kim.partynote.location.dto.UpdateLocationDto;
import ru.kim.partynote.location.model.Location;

import java.util.List;

public interface LocationService {
    Location save(LocationDto locationDto);

    LocationFullDto saveLocation(NewLocationDto newLocationDto);

    List<LocationFullDto> getAllLocationForAdmin(Integer from, Integer size);

    LocationFullDto updateLocation(Long locationId, UpdateLocationDto updateLocationDto);

    List<LocationFullDto> getLocationsByCoordinatesAndRadius(
            Double lat, Double lon, float radius, Integer from, Integer size);

    LocationFullDto getLocationById(Long locationId);
}
