package pl.wsb.fitnesstracker.user.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserDto;
import pl.wsb.fitnesstracker.user.api.UserEmailDto;
import pl.wsb.fitnesstracker.user.api.UserMapper;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
class UserController {

    private final UserServiceImpl userService;

    private final UserMapper userMapper;

    @GetMapping
    public List<UserDto> getAllUsers() {
        // test: shouldReturnAllUsers_whenGettingAllUsers
        return userService.findAllUsers()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @GetMapping("/simple")
    public List<UserDto> getAllUsersSimple() {
        // test shouldReturnAllSimpleUsers_whenGettingAllUsers
        return userService.findAllUsers()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        // test: shouldReturnDetailsAboutUser_whenGettingUserById
        return userService.getUser(id)
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public List<UserEmailDto> findUserByEmail(@RequestParam String email) {
//        shouldReturnDetailsAboutUser_whenGettingUserByEmail
        return userService.findUsersByEmail(email).stream().map(userMapper::toEmailDto).toList();
    }

    @GetMapping("/older/{time}")
    public List<UserDto> findUsersOlderThan(@PathVariable LocalDate time) {
        // shouldReturnAllUsersOlderThan_whenGettingAllUsersOlderThan
        return userService.findUsersOlderThan(time)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody UserDto userDto) {
        // shouldPersistUser_whenCreatingUser
        return userMapper.toDto(userService.createUser(userMapper.toEntity(userDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        // shouldRemoveUserFromRepository_whenDeletingClient
        userService.removeUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUserById(@PathVariable Long id, @RequestBody UserDto dto) {
        // shouldUpdateUser_whenUpdatingUser
        final User updatedUser = userService.updateUser(id, userMapper.toEntity(dto));
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

}