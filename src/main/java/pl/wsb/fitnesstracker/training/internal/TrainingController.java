package pl.wsb.fitnesstracker.training.internal;

// Spring Framework imports
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Application imports
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.training.api.TrainingService;

// Java standard imports
import java.util.Date;
import java.util.List;

// Lombok imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for managing training-related operations.
 * 
 * This controller provides endpoints for retrieving, creating, and updating
 * training records in the fitness tracking system. It follows RESTful principles
 * and uses JSON for data exchange.
 * 
 * @author Fitness Tracker Team
 * @version 2.0
 */
@RestController
@RequestMapping("/v1/trainings")
@RequiredArgsConstructor
@Slf4j
public class TrainingController {

    // ========== Dependencies ==========

    /** Service for training operations */
    private final TrainingService trainingService;

    /** Mapper for converting between entity and DTO objects */
    private final TrainingMapper trainingMapper;

    // ========== Command Endpoints (POST, PUT) ==========

    /**
     * Creates a new training record in the system.
     * 
     * @param trainingDto The training data transfer object containing all necessary information
     * @return ResponseEntity containing the created training with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<TrainingDto> registerNewTraining(@RequestBody CreateTrainingDTO trainingDto) {
        log.info("Received request to create new training for user ID: {}", trainingDto.getUserId());

        // Convert DTO to entity and persist
        Training trainingEntity = trainingMapper.toEntity(trainingDto);
        Training persistedTraining = trainingService.createTraining(trainingEntity, trainingDto.getUserId());

        // Convert persisted entity back to DTO
        TrainingDto responseDto = trainingMapper.toDto(persistedTraining);
        log.info("Successfully created training with ID: {}", responseDto.getId());

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    /**
     * Updates an existing training record in the system.
     * 
     * @param trainingId The ID of the training to update
     * @param trainingDto The training data transfer object containing updated information
     * @return The updated training data
     */
    @PutMapping("/{trainingId}")
    public TrainingDto modifyExistingTraining(@PathVariable Long trainingId, @RequestBody CreateTrainingDTO trainingDto) {
        log.info("Received request to update training ID: {} for user ID: {}", trainingId, trainingDto.getUserId());

        // Convert DTO to entity and update
        Training trainingEntity = trainingMapper.toEntity(trainingDto);
        Training updatedTraining = trainingService.updateTraining(trainingEntity, trainingId, trainingDto.getUserId());

        // Convert updated entity back to DTO
        TrainingDto responseDto = trainingMapper.toDto(updatedTraining);
        log.info("Successfully updated training with ID: {}", responseDto.getId());

        return responseDto;
    }

    // ========== Query Endpoints (GET) ==========

    /**
     * Retrieves all training records from the system.
     * 
     * @return A list of all training records
     */
    @GetMapping
    public List<TrainingDto> retrieveAllTrainings() {
        log.info("Received request to retrieve all trainings");

        List<TrainingDto> trainings = trainingService.findAllTrainings()
                .stream()
                .map(trainingMapper::toDto)
                .toList();

        log.info("Returning {} trainings", trainings.size());
        return trainings;
    }

    /**
     * Retrieves all training records for a specific user.
     * 
     * @param userId The ID of the user whose trainings to retrieve
     * @return A list of training records for the specified user
     */
    @GetMapping("/{userId}")
    public List<TrainingDto> retrieveTrainingsByUser(@PathVariable Long userId) {
        log.info("Received request to retrieve trainings for user ID: {}", userId);

        List<TrainingDto> trainings = trainingService.findTrainingsByUserId(userId)
                .stream()
                .map(trainingMapper::toDto)
                .toList();

        log.info("Returning {} trainings for user ID: {}", trainings.size(), userId);
        return trainings;
    }

    /**
     * Retrieves all training records that were completed after a specified date.
     * 
     * @param afterTime The date after which trainings should have been completed (format: yyyy-MM-dd)
     * @return A list of training records completed after the specified date
     */
    @GetMapping("/finished/{afterTime}")
    public List<TrainingDto> retrieveTrainingsCompletedAfter(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date afterTime) {
        log.info("Received request to retrieve trainings completed after: {}", afterTime);

        List<TrainingDto> trainings = trainingService.findTrainingsWithEndDateAfter(afterTime)
                .stream()
                .map(trainingMapper::toDto)
                .toList();

        log.info("Returning {} trainings completed after: {}", trainings.size(), afterTime);
        return trainings;
    }

    /**
     * Retrieves all training records of a specific activity type.
     * 
     * @param activityType The type of activity (e.g., RUNNING, CYCLING)
     * @return A list of training records of the specified activity type
     */
    @GetMapping("/activityType")
    public List<TrainingDto> retrieveTrainingsByActivityType(@RequestParam ActivityType activityType) {
        log.info("Received request to retrieve trainings with activity type: {}", activityType);

        List<TrainingDto> trainings = trainingService.findTrainingsByActivityType(activityType)
                .stream()
                .map(trainingMapper::toDto)
                .toList();

        log.info("Returning {} trainings with activity type: {}", trainings.size(), activityType);
        return trainings;
    }
}
