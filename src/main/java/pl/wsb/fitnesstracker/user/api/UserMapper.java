package pl.wsb.fitnesstracker.user.api;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getBirthdate(),
                user.getEmail());
    }

    public User toEntity(UserDto userDto) {
        return new User(
                userDto.firstName(),
                userDto.lastName(),
                userDto.birthdate(),
                userDto.email());
    }

    public UserEmailDto toEmailDto(User user) {
        return new UserEmailDto(user.getId(), user.getEmail());
    }
}
