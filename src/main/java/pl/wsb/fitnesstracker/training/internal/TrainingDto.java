package pl.wsb.fitnesstracker.training.internal;

import lombok.Getter;
import lombok.Setter;
import pl.wsb.fitnesstracker.user.api.UserDto;

import java.util.Date;

@Getter
@Setter
public class TrainingDto {
    private final Long id;

    private final UserDto user;

    private final Date startTime;

    private final Date endTime;

    private final ActivityType activityType;

    private final double distance;

    private final double averageSpeed;

    TrainingDto(Long id, UserDto user, Date startTime, Date endTime, ActivityType activityType, double distance, double averageSpeed) {
        this.id = id;
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activityType = activityType;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
    }

}
