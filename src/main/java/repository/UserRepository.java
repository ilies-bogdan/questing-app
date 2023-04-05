package repository;

import domain.User;

public interface UserRepository extends Repository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
}
