package pl.wsb.fitnesstracker.statistics.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.wsb.fitnesstracker.statistics.api.Statistics;
import pl.wsb.fitnesstracker.user.api.UserMapper;

/**
 * Mapper for converting between Statistics entities and DTOs.
 */
@Component
@RequiredArgsConstructor
public class StatisticsMapper {

    private final UserMapper userMapper;

    /**
     * Converts a Statistics entity to a StatisticsDto.
     *
     * @param statistics The Statistics entity to convert
     * @return The converted StatisticsDto
     */
    StatisticsDto toDto(Statistics statistics) {
        return new StatisticsDto(
                statistics.getId(),
                userMapper.toDto(statistics.getUser()),
                statistics.getTotalTrainings(),
                statistics.getTotalDistance(),
                statistics.getTotalCaloriesBurned()
        );
    }

    /**
     * Converts a CreateStatisticsDTO to a Statistics entity.
     *
     * @param dto The CreateStatisticsDTO to convert
     * @return The converted Statistics entity
     */
    Statistics toEntity(CreateStatisticsDTO dto) {
        return new Statistics(
                null, // User will be set in the service
                dto.getTotalTrainings(),
                dto.getTotalDistance(),
                dto.getTotalCaloriesBurned()
        );
    }
}
