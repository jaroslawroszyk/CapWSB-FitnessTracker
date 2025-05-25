package pl.wsb.fitnesstracker.training.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.wsb.fitnesstracker.training.api.Training;

@Component
/*
To adnotacja z Springa, która oznacza, że dana klasa jest komponentem Springa, czyli będzie automatycznie wykrywana przez mechanizm skanowania klas i rejestrowana jako bean w kontekście aplikacji Spring.

 */
@RequiredArgsConstructor
/*
To adnotacja z Lomboka, która automatycznie generuje konstruktor dla wszystkich finałowych (final) lub oznaczonych jako @NonNull pól w klasie.
*/
public class TrainingMapper {
    @Autowired
    private final UserMapper trainingUserMapper;

    TrainingDto toDto(Training training) {
        return new TrainingDto(
                training.getId(),
                trainingUserMapper.toDto(training.getUser()),
                training.getStartTime(),
                training.getEndTime(),
                training.getActivityType(),
                training.getDistance(),
                training.getAverageSpeed()
        );
    }

    Training toEntity(CreateTrainingDTO dto) {
        return new Training(
                null,
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getActivityType(),
                dto.getDistance(),
                dto.getAverageSpeed()
        );
    }
}
