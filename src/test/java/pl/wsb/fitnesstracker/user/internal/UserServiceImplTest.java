//package pl.wsb.fitnesstracker.user.internal;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import pl.wsb.fitnesstracker.user.api.User;
//
//import java.time.LocalDate;
//
//import static org.mockito.Mockito.when;
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//@AutoConfigureMockMvc(addFilters = false)
//class UserServiceImplTest {
//
//    @Mock
//    UserRepository userRepository;
//
//    private User user;
//
//    @BeforeEach
//    void setUp() {
//        user = new User("John", "Doe", LocalDate.of(1990, 1, 1), "<EMAIL>");
//    }
//
//    @Test
//    void shouldCreateUser() {
//        UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository);
//
//        when(userRepository.save(user)).thenReturn(user);
//
//        User createdUser = userServiceImpl.createUser(user);
//
//        assertEquals(user, createdUser);
//    }
//}

package pl.wsb.fitnesstracker.user.internal;

import org.apache.tomcat.util.bcel.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import pl.wsb.fitnesstracker.user.api.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    private User user;
    private UserServiceImpl userServiceImpl;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", LocalDate.of(1990, 1, 1), "<EMAIL>");
        userServiceImpl = new UserServiceImpl(userRepository);
    }

    @Test
    void shouldCreateUser() {
        when(userRepository.save(user)).thenReturn(user);
        User createdUser = userServiceImpl.createUser(user);
        createdUser.setId(1L);

        assertEquals(user, createdUser);
        assertEquals(user.getId(), createdUser.getId());
    }

    @Test
    void shouldUpdateUser() {
        Long userId = 1L;

        User updatedInfo = new User("Jane", "Smith", LocalDate.of(1991, 2, 2), "jane@example.com");

        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        User updatedUser = userServiceImpl.updateUser(userId, updatedInfo);

        assertEquals("Jane", updatedUser.getFirstName());
        assertEquals("Smith", updatedUser.getLastName());
        assertEquals(LocalDate.of(1991, 2, 2), updatedUser.getBirthdate());
        assertEquals("jane@example.com", updatedUser.getEmail());
    }

    @Test
    void shouldRemoveUser() {
        Long userId = 1L;

        userServiceImpl.removeUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void shouldReturnUserById() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userServiceImpl.getUser(userId);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void shouldReturnUserByEmail() {
        String email = "<EMAIL>";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> result = userServiceImpl.getUserByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void shouldReturnListOfUsersByEmail() {
        String email = "<EMAIL>";
        List<User> users = List.of(user);
        when(userRepository.findUsersByEmail(email)).thenReturn(users);

        List<User> result = userServiceImpl.findUsersByEmail(email);

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userServiceImpl.findAllUsers();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    void shouldReturnUsersOlderThanGivenDate() {
        LocalDate date = LocalDate.of(2000, 1, 1);
        List<User> users = List.of(user);
        when(userRepository.findByBirthdateOlderThan(date)).thenReturn(users);

        List<User> result = userServiceImpl.findUsersOlderThan(date);

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }


}
