package repository;

import domain.User;

public interface UserRepository extends Repository<User, Integer> {
    /**
     * Finds a user by username.
     * @param username - the username to find by
     * @return the user with the given username
     */
    User findByUsername(String username);

    /**
     * Finds a user by email.
     * @param email - the email to find by
     * @return the user with the given email
     */
    User findByEmail(String email);
}
