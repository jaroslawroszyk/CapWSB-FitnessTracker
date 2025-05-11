package pl.wsb.fitnesstracker.user.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserProvider {

    /**
     * Retrieves a user based on their ID.
     * If the user with given ID is not found, then {@link Optional#empty()} will be returned.
     *
     * @param userId id of the user to be searched
     * @return An {@link Optional} containing the located user, or {@link Optional#empty()} if not found
     */
    Optional<User> getUser(Long userId);

    /**
     * Retrieves a user based on their email.
     * If the user with given email is not found, then {@link Optional#empty()} will be returned.
     *
     * @param email The email of the user to be searched
     * @return An {@link Optional} containing the located user, or {@link Optional#empty()} if not found
     */
    Optional<User> getUserByEmail(String email);

    /**
     * Retrieves all users.
     *
     * @return An {@link Optional} containing the all users,
     */
    List<User> findAllUsers();

//    /**
//     Retrieves all users whose email contains the given part.
//     * @param emailPart A part of the user's email to be matched // todo:
//     * @return A {@link List} containing all found users.
//     */

    /**
     * Retrieves all users whose email contains the given part.
     *
     * @param emailPart A part of the user's email to be matched
     * @return A {@link List} containing all users whose email contains the specified part
     */
    List<User> findUsersByEmail(String emailPart);

    /**
     * Retrieves all users whose birth date is before the given date.
     *
     * @param time The date to compare users' birth dates against
     * @return A {@link List} containing all users born before the specified date
     */
    List<User> findUsersOlderThan(LocalDate time);
}
