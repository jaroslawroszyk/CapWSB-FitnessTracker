package pl.wsb.fitnesstracker.training.internal;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
/*

Nie ma podpiętych tych powiązanych encji typu User itd tylko odnosi się do samych idkow

Po idkach to dziala mordeczko
 */
@Getter
@Setter
public class CreateTrainingDTO {
    private final Long id;

    private final Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final Date endTime;

    private final ActivityType activityType;

    private final double distance;

    private final double averageSpeed;

    CreateTrainingDTO(Long id, Long userId, Date startTime, Date endTime, ActivityType activityType, double distance, double averageSpeed) {
        this.id = id;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activityType = activityType;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
    }
}
