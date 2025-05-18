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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import pl.wsb.fitnesstracker.user.api.User;

import java.time.LocalDate;

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

        assertEquals(user, createdUser);
    }
}
