package pl.wsb.fitnesstracker.training.api;

public interface TrainingService extends TrainingProvider{
    Training createTraining(Training training, Long userId);

    Training updateTraining(Training training, Long trainingId, Long userId);
}


/*
Stworozen przez service
model providera i serwisu
- [ ] utworzenie nowego treningu ??
- [ ] aktualizacja treningu (dowolnie wybrane pole np. dystans) ??
 */