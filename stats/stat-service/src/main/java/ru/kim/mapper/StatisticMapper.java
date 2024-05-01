package ru.kim.mapper;

import org.mapstruct.Mapper;
import ru.kim.model.Statistic;
import ru.kim.partynote.dto.CreateStatisticDto;

@Mapper(componentModel = "spring")
public interface StatisticMapper {
    Statistic toModel(CreateStatisticDto dto);
}
