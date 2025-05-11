package pl.wsb.fitnesstracker.user.api;

/**
 * Interface (API) for modifying operations on {@link User} entities through the API.
 * Implementing classes are responsible for executing changes within a database transaction, whether by continuing an existing transaction or creating a new one if required.
 */
public interface UserService {

    /**
     * Creates a new user in the repository with the provided user details.
     * The user must have firstName, lastName, birthdate and email specified.
     * The operation returns the created user with assigned ID.
     *
     * @param user The user entity to be created containing required user details
     * @return The persisted user entity with assigned ID
     */
    User createUser(User user);

    /**
     * Removes a user from the repository based on the provided user ID.
     * If the user is successfully deleted, the operation returns without any content.
     *
     * @param id The unique identifier of the user to be removed
     */
    void removeUser(Long id);

    /**
     * Updates an existing user in the repository with the provided user details.
     * All user fields (firstName, lastName, birthdate, email) can be modified.
     * The operation returns the updated user entity.
     *
     * @param id   The unique identifier of the user to be updated
     * @param user The user entity containing updated user details
     * @return The updated user entity
     */
    User updateUser(Long id, User user);
}
