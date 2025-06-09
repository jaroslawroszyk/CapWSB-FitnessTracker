package pl.wsb.fitnesstracker.training.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.user.api.UserMapper;

/**
 * Mapper class responsible for converting between Training entities and DTOs.
 * 
 * This class provides bidirectional conversion between domain entities and data transfer objects,
 * ensuring proper separation of concerns between the API layer and the domain layer.
 * 
 * @author Fitness Tracker Team
 * @version 2.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrainingMapper {

    /** Mapper for user entities within training objects */
    private final UserMapper userMapper;

    /**
     * Converts a Training entity to a TrainingDto for API responses.
     * 
     * @param trainingEntity The Training entity to convert
     * @return A TrainingDto containing the data from the entity
     * @throws IllegalArgumentException if the training entity is null
     */
    public TrainingDto convertToDto(Training trainingEntity) {
        if (trainingEntity == null) {
            log.error("Cannot convert null training entity to DTO");
            throw new IllegalArgumentException("Training entity cannot be null");
        }

        log.debug("Converting training entity with ID {} to DTO", trainingEntity.getId());

        return new TrainingDto(
                trainingEntity.getId(),
                userMapper.toDto(trainingEntity.getUser()),
                trainingEntity.getStartTime(),
                trainingEntity.getEndTime(),
                trainingEntity.getActivityType(),
                trainingEntity.getDistance(),
                trainingEntity.getAverageSpeed()
        );
    }

    /**
     * Converts a CreateTrainingDTO to a Training entity for persistence.
     * 
     * @param trainingDto The CreateTrainingDTO containing the data to convert
     * @return A Training entity initialized with the data from the DTO
     * @throws IllegalArgumentException if the training DTO is null
     */
    public Training convertToEntity(CreateTrainingDTO trainingDto) {
        if (trainingDto == null) {
            log.error("Cannot convert null training DTO to entity");
            throw new IllegalArgumentException("Training DTO cannot be null");
        }

        log.debug("Converting training DTO to entity");

        return new Training(
                null, // ID is null for new entities
                trainingDto.getStartTime(),
                trainingDto.getEndTime(),
                trainingDto.getActivityType(),
                trainingDto.getDistance(),
                trainingDto.getAverageSpeed()
        );
    }
}
