package pl.wsb.fitnesstracker.training.internal;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.wsb.fitnesstracker.training.api.*;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserProvider;
import pl.wsb.fitnesstracker.user.api.UserNotFoundException;

import java.lang.reflect.Constructor;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingServiceImplTest {

    private TrainingRepository trainingRepository;
    private UserProvider userProvider;
    private TrainingServiceImpl trainingService;

    @BeforeEach
    void setUp() {
        trainingRepository = mock(TrainingRepository.class);
        userProvider = mock(UserProvider.class);
        trainingService = new TrainingServiceImpl(trainingRepository, userProvider);
    }

    @Test
    void getTraining_shouldReturnTraining() {
        Training training = mock(Training.class);
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.getTraining(1L);

        assertThat(result).isPresent().contains(training);
    }

    @Test
    void createTraining_shouldThrowWhenIdIsSet() {
        Training training = mock(Training.class);
        when(training.getId()).thenReturn(10L);

        assertThatThrownBy(() -> trainingService.createTraining(training, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already DB ID");
    }

    @Test
    void createTraining_shouldSaveWhenUserExists() {
        Training training = new Training(null, new Date(), new Date(), ActivityType.RUNNING, 5.0, 10.0);
        User user = mock(User.class);
        when(training.getId()).thenReturn(null);
        when(userProvider.getUser(1L)).thenReturn(Optional.of(user));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Training result = trainingService.createTraining(training, 1L);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getActivityType()).isEqualTo(ActivityType.RUNNING);
    }

    @Test
    void createTraining_shouldThrowWhenUserNotFound() {
        Training training = mock(Training.class);
        when(training.getId()).thenReturn(null);
        when(userProvider.getUser(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(training, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    private Training createEmptyTraining() { // reflection
        try {
            Constructor<Training> constructor = Training.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Training instance", e);
        }
    }

    @Test
    void updateTraining_shouldUpdateTrainingSuccessfully() {
        Training existing = createEmptyTraining();
        Training update = new Training(null, new Date(), new Date(), ActivityType.SWIMMING, 1.0, 1.0);
        User user = mock(User.class);

        when(trainingRepository.getReferenceById(1L)).thenReturn(existing);
        when(userProvider.getUser(2L)).thenReturn(Optional.of(user));
        when(trainingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Training result = trainingService.updateTraining(update, 1L, 2L);

        assertThat(result.getActivityType()).isEqualTo(ActivityType.SWIMMING);
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    void updateTraining_shouldThrowWhenTrainingNotFound() {
        when(trainingRepository.getReferenceById(1L)).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> trainingService.updateTraining(mock(Training.class), 1L, 2L))
                .isInstanceOf(TrainingNotFoundException.class);
    }

    @Test
    void updateTraining_shouldThrowWhenUserNotFound() {
        Training training = mock(Training.class);
        when(trainingRepository.getReferenceById(1L)).thenReturn(training);
        when(userProvider.getUser(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.updateTraining(training, 1L, 2L))
                .isInstanceOf(UserNotFoundException.class);
    }
}
