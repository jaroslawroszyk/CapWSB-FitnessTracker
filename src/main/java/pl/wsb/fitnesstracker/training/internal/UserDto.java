package pl.wsb.fitnesstracker.training.internal;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
class UserDto { // todo: user mapper from user/internal package

    private final Long id;

    private final String firstName;

    private final String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate birthdate;

    private final String email;

    public UserDto(Long id, String firstName, String lastName, LocalDate birthdate, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.email = email;
    }
}