package ru.kim.partynote.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kim.partynote.location.dto.LocationDto;
import ru.kim.partynote.location.dto.LocationFullDto;
import ru.kim.partynote.location.dto.NewLocationDto;
import ru.kim.partynote.location.dto.UpdateLocationDto;
import ru.kim.partynote.location.mapper.LocationDtoMapper;
import ru.kim.partynote.location.mapper.LocationFullDtoMapper;
import ru.kim.partynote.location.mapper.NewLocationDtoMapper;
import ru.kim.partynote.location.model.Location;
import ru.kim.partynote.location.repository.LocationRepository;
import ru.kim.partynote.exception.NotFoundException;
import ru.kim.partynote.util.PageRequestFrom;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;
    private final LocationDtoMapper locationDtoMapper;
    private final NewLocationDtoMapper newLocationDtoMapper;
    private final LocationFullDtoMapper locationFullDtoMapper;

    @Override
    @Transactional
    public Location save(LocationDto locationDto) {
        Location location = locationRepository.findLocationByLatAndLon(locationDto.getLat(), locationDto.getLon());
        if (location == null) {
            return locationRepository.save(locationDtoMapper.toModel(locationDto));
        }
        return location;
    }

    @Override
    @Transactional
    public LocationFullDto saveLocation(NewLocationDto newLocationDto) {
        return Optional.of(newLocationDto)
                .map(newLocationDtoMapper::toModel)
                .map(locationRepository::save)
                .map(locationFullDtoMapper::toDto)
                .get();
    }

    @Override
    public LocationFullDto getLocationById(Long locationId) {
        Location location = getLocationOrThrowNotFoundException(locationId);
        return locationFullDtoMapper.toDto(location);
    }

    @Override
    public List<LocationFullDto> getAllLocationForAdmin(Integer from, Integer size) {
        return locationRepository.findAll(new PageRequestFrom(from, size, null)).stream()
                .map(locationFullDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LocationFullDto updateLocation(Long locationId, UpdateLocationDto updateLocationDto) {
        Location location = getLocationOrThrowNotFoundException(locationId);
        if (updateLocationDto.getName() != null) {
            location.setName(updateLocationDto.getName());
        }
        if (updateLocationDto.getDescription() != null) {
            location.setDescription(updateLocationDto.getDescription());
        }
        return locationFullDtoMapper.toDto(location);
    }

    @Override
    public List<LocationFullDto> getLocationsByCoordinatesAndRadius(
            Double lat, Double lon, float radius, Integer from, Integer size) {
        return locationRepository.findLocationsInRadius(lat, lon, radius, new PageRequestFrom(from, size, null))
                .stream()
                .map(locationFullDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    private Location getLocationOrThrowNotFoundException(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException(String.format("Location with id=%d was not found", locationId)));
    }
}
