package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BadgeTest {
    @Test
    public void testBadge() {
        Badge badge = new Badge(1, "Title", "Description", BadgeType.post, 10);
        assertEquals(1, badge.getId());
        assertEquals("Title", badge.getTitle());
        assertEquals("Description", badge.getDescription());
        assertEquals(BadgeType.post, badge.getType());
        assertEquals(10, badge.getRequirement());

        badge.setId(2);
        badge.setTitle("Title1");
        badge.setDescription("Description1");
        badge.setType(BadgeType.complete);
        badge.setRequirement(20);
        assertEquals(2, badge.getId());
        assertEquals("Title1", badge.getTitle());
        assertEquals("Description1", badge.getDescription());
        assertEquals(BadgeType.complete, badge.getType());
        assertEquals(20, badge.getRequirement());
    }
}
