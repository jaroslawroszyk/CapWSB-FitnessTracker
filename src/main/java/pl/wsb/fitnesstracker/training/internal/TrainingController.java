package pl.wsb.fitnesstracker.training.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.training.api.TrainingService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    private final TrainingMapper trainingMapper;

    /**
     * Return all trainings.
     */
    @GetMapping
    List<TrainingDto> findAllTrainings() { // todo rename to  getAllTrainings
        return trainingService.findAllTrainings()
                .stream()
                .map(trainingMapper::toDto)
                .toList();
    }

    /**
     * Returns workouts for the user with the given ID.
     * @param userId user ID
     */
    @GetMapping("/{userId}")
    /*
    @PathVariable Long id oznacza: pobierz fragment {id} z URL-a i przypisz go do zmiennej id.
     */
    List<TrainingDto> findTrainingsByUserId(@PathVariable Long userId) { // todo: should be getTrainingsByUserId
        return trainingService.findTrainingsByUserId(userId)
                .stream()
                .map(trainingMapper::toDto)
                .toList();
    }

    /**
     * Returns workouts completed after the specified date.
     * @param afterTime date in yyyy-MM-dd format
     */
    @GetMapping("/finished/{afterTime}")
    List<TrainingDto> findTrainingsFinishedAfter(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date afterTime) {
        return trainingService.findTrainingsWithEndDateAfter(afterTime)
                .stream()
                .map(trainingMapper::toDto)
                .toList();
    }

    /**
     * Returns workouts with the given activity type.
     * @param activityType activity type (e.g. RUNNING, CYCLING)
     */
    @GetMapping("/activityType")
    List<TrainingDto> findTrainingsByActivityType(@RequestParam ActivityType activityType) {
        return trainingService.findTrainingsByActivityType(activityType)
                .stream()
                .map(trainingMapper::toDto)
                .toList();
    }

    /**
     * Create new training.
     * @param trainingDto training data
     */
    @PostMapping
    ResponseEntity<TrainingDto> createTraining(@RequestBody CreateTrainingDTO trainingDto) {
        Training persisted = trainingService.createTraining(trainingMapper.toEntity(trainingDto), trainingDto.getUserId());
        TrainingDto dto = trainingMapper.toDto(persisted);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /**
     * Updates an existing training.
     * @param trainingId The training ID to update
     * @param trainingDto the new training data
     */
    @PutMapping("/{trainingId}")
    TrainingDto updateTraining(@PathVariable Long trainingId, @RequestBody CreateTrainingDTO trainingDto) {
        Training updated = trainingService.updateTraining(trainingMapper.toEntity(trainingDto), trainingId, trainingDto.getUserId());
        return trainingMapper.toDto(updated);
    }
}
