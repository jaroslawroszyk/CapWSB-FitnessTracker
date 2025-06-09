package pl.wsb.fitnesstracker.user.internal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserProvider;
import pl.wsb.fitnesstracker.user.api.UserService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class UserServiceImpl implements UserService, UserProvider {

    private final UserRepository userRepository;

    @Override
    public User createUser(final User user) {
        log.info("Starting user creation process");

        // Validate user object
        if (user == null) {
            log.error("Cannot create user: user object is null");
            throw new IllegalArgumentException("User object cannot be null");
        }

        // Check if user already has an ID
        if (user.getId() != null) {
            log.error("Cannot create user: user already has ID {}", user.getId());
            throw new IllegalArgumentException("User has already DB ID, update is not permitted!");
        }

        // Validate required fields
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            log.error("Cannot create user: first name is missing");
            throw new IllegalArgumentException("First name is required");
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            log.error("Cannot create user: last name is missing");
            throw new IllegalArgumentException("Last name is required");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.error("Cannot create user: email is missing");
            throw new IllegalArgumentException("Email is required");
        }

        if (user.getBirthdate() == null) {
            log.error("Cannot create user: birthdate is missing");
            throw new IllegalArgumentException("Birthdate is required");
        }

        // Check if email is already in use
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            log.error("Cannot create user: email {} is already in use", user.getEmail());
            throw new IllegalArgumentException("Email is already in use");
        }

        try {
            log.info("Saving new user: {} {}, email: {}", user.getFirstName(), user.getLastName(), user.getEmail());
            User savedUser = userRepository.save(user);
            log.info("User created successfully with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            log.error("Error occurred while saving user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public User updateUser(final Long userId, User userInfo) {
        log.info("Starting user update process for user ID: {}", userId);

        // Validate input parameters
        if (userId == null) {
            log.error("Cannot update user: user ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (userInfo == null) {
            log.error("Cannot update user: user info object is null");
            throw new IllegalArgumentException("User info object cannot be null");
        }

        // Validate required fields in userInfo
        if (userInfo.getFirstName() == null || userInfo.getFirstName().trim().isEmpty()) {
            log.error("Cannot update user: first name is missing");
            throw new IllegalArgumentException("First name is required");
        }

        if (userInfo.getLastName() == null || userInfo.getLastName().trim().isEmpty()) {
            log.error("Cannot update user: last name is missing");
            throw new IllegalArgumentException("Last name is required");
        }

        if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
            log.error("Cannot update user: email is missing");
            throw new IllegalArgumentException("Email is required");
        }

        if (userInfo.getBirthdate() == null) {
            log.error("Cannot update user: birthdate is missing");
            throw new IllegalArgumentException("Birthdate is required");
        }

        // Find the user to update
        User userToUpdate;
        try {
            log.debug("Retrieving user with ID: {}", userId);
            userToUpdate = userRepository.getReferenceById(userId);
            log.debug("Found user to update: {}", userToUpdate);
        } catch (EntityNotFoundException e) {
            log.error("User with ID {} not found", userId);
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }

        // Check if email is already in use by another user
        if (!userInfo.getEmail().equals(userToUpdate.getEmail())) {
            Optional<User> existingUserWithEmail = userRepository.findByEmail(userInfo.getEmail());
            if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(userId)) {
                log.error("Cannot update user: email {} is already in use by another user", userInfo.getEmail());
                throw new IllegalArgumentException("Email is already in use by another user");
            }
        }

        // Track changes for logging
        StringBuilder changes = new StringBuilder();
        if (!userToUpdate.getFirstName().equals(userInfo.getFirstName())) {
            changes.append(String.format("First name: '%s' -> '%s', ", userToUpdate.getFirstName(), userInfo.getFirstName()));
        }
        if (!userToUpdate.getLastName().equals(userInfo.getLastName())) {
            changes.append(String.format("Last name: '%s' -> '%s', ", userToUpdate.getLastName(), userInfo.getLastName()));
        }
        if (!userToUpdate.getEmail().equals(userInfo.getEmail())) {
            changes.append(String.format("Email: '%s' -> '%s', ", userToUpdate.getEmail(), userInfo.getEmail()));
        }
        if (!userToUpdate.getBirthdate().equals(userInfo.getBirthdate())) {
            changes.append(String.format("Birthdate: '%s' -> '%s', ", userToUpdate.getBirthdate(), userInfo.getBirthdate()));
        }

        // Update user fields
        userToUpdate.setFirstName(userInfo.getFirstName());
        userToUpdate.setLastName(userInfo.getLastName());
        userToUpdate.setBirthdate(userInfo.getBirthdate());
        userToUpdate.setEmail(userInfo.getEmail());

        try {
            log.info("Updating user with ID {}: {}", userId, changes.length() > 0 ? changes.toString() : "No changes");
            User updatedUser = userRepository.save(userToUpdate);
            log.info("User updated successfully: {}", updatedUser);
            return updatedUser;
        } catch (Exception e) {
            log.error("Error occurred while updating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void removeUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> getUser(final Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> getUserByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findUsersByEmail(String email) {
        return userRepository.findUsersByEmail(email);
    }

    @Override
    public List<User> findAllUsers() {
        log.info("Retrieving all users from the database");

        try {
            List<User> users = userRepository.findAll();

            // Sort users by ID for consistent ordering
            List<User> sortedUsers = users.stream()
                .sorted((u1, u2) -> {
                    if (u1.getId() == null && u2.getId() == null) return 0;
                    if (u1.getId() == null) return -1;
                    if (u2.getId() == null) return 1;
                    return u1.getId().compareTo(u2.getId());
                })
                .toList();

            int userCount = sortedUsers.size();
            log.info("Retrieved {} users from the database", userCount);

            // Log some statistics about the users
            if (userCount > 0) {
                Map<String, Long> emailDomainStats = sortedUsers.stream()
                    .map(user -> user.getEmail())
                    .filter(email -> email != null && email.contains("@"))
                    .map(email -> email.substring(email.indexOf('@') + 1))
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                log.debug("Email domain statistics: {}", emailDomainStats);

                // Calculate average age
                double averageAge = sortedUsers.stream()
                    .map(User::getBirthdate)
                    .filter(Objects::nonNull)
                    .mapToLong(birthdate -> ChronoUnit.YEARS.between(birthdate, LocalDate.now()))
                    .average()
                    .orElse(0);

                log.debug("Average user age: {}", averageAge);
            }

            return sortedUsers;
        } catch (Exception e) {
            log.error("Error occurred while retrieving all users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve all users", e);
        }
    }

    @Override
    public List<User> findUsersOlderThan(LocalDate time) {
        return userRepository.findByBirthdateOlderThan(time);
    }
}
