package ru.kim.service;

import ru.kim.partynote.dto.CreateStatisticDto;
import ru.kim.partynote.dto.ReadStatisticDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticService {
    CreateStatisticDto saveHit(CreateStatisticDto statisticDto);

    List<ReadStatisticDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
