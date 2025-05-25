package pl.wsb.fitnesstracker.user.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wsb.fitnesstracker.user.api.User;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.List;

import static java.util.stream.Collectors.toList;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Query searching users by email address. It matches by exact match.
     *
     * @param email email of the user to search
     * @return {@link Optional} containing found user or {@link Optional#empty()} if none matched
     */
    default Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(user -> Objects.equals(user.getEmail(), email))
                .findFirst();
    }

    /**
     * Query searching users by email address. It matches by partial match.
     *
     * @param emailPart part of the email to search
     * @return {@link List} containing found users
     */
    default List<User> findUsersByEmail(String emailPart) {
        return findAll().stream()
                .filter(user -> user.getEmail().toLowerCase().contains(emailPart.toLowerCase()))
                .collect(toList());
    }

    /**
     * Query searching users who were born before specified date.
     *
     * @param time reference date to compare user's birth date against
     * @return {@link List} containing users born before the specified date
     */
    default List<User> findByBirthdateOlderThan(LocalDate time) {
        return findAll().stream()
                .filter(user -> user.getBirthdate().isBefore(time))
                .collect(toList());
    }
}
