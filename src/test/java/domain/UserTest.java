package domain;

import domain.Rank;
import domain.User;
import domain.validation.UserValidator;
import domain.validation.Validator;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    @Test
    public void testUser() {
        User user = new User(1, "username", "email", 1, "salt", Rank.Bronze, 100);
        assertEquals(1, user.getId());
        assertEquals("username", user.getUsername());
        assertEquals("email", user.getEmail());
        assertEquals(1, user.getPasswordCode());
        assertEquals("salt", user.getSalt());
        assertEquals(Rank.Bronze, user.getRank());
        assertEquals(100, user.getTokenCount());

        user.setId(2);
        user.setUsername("username1");
        user.setEmail("email1");
        user.setPasswordCode(2);
        user.setSalt("salt1");
        user.setRank(Rank.Diamond);
        user.setTokenCount(200);
        assertEquals(2, user.getId());
        assertEquals("username1", user.getUsername());
        assertEquals("email1", user.getEmail());
        assertEquals(2, user.getPasswordCode());
        assertEquals("salt1", user.getSalt());
        assertEquals(Rank.Diamond, user.getRank());
        assertEquals(200, user.getTokenCount());
    }
}
