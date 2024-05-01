package ru.kim.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kim.mapper.StatisticMapper;
import ru.kim.partynote.dto.CreateStatisticDto;
import ru.kim.partynote.dto.ReadStatisticDto;
import ru.kim.repository.StatisticRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final StatisticRepository statisticRepository;
    private final StatisticMapper statisticMapper;


    @Transactional
    @Override
    public CreateStatisticDto saveHit(CreateStatisticDto statisticDto) {
        Optional.of(statisticDto)
                .map(statisticMapper::toModel)
                .map(statisticRepository::save);
        return statisticDto;
    }

    @Override
    public List<ReadStatisticDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            if (uris == null) {
                return statisticRepository.findAllByTimestampBetweenStartAndEndUniqueIp(start, end);
            } else {
                return statisticRepository.findAllByTimestampBetweenStartAndEndWithUrisUniqueIp(start, end, uris);
            }
        } else {
            if (uris == null) {
                return statisticRepository.findAllByTimestampBetweenStartAndEnd(start, end);
            } else {
                return statisticRepository.findAllByTimestampBetweenStartAndEndWithUris(start, end, uris);
            }
        }
    }
}
