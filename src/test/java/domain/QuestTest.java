package domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class QuestTest {
    @Test
    public void testQuest() {
        Quest quest = new Quest(1, 2, 3, LocalDateTime.MAX, 40, QuestStatus.posted, "word");
        assertEquals(1, quest.getId());
        assertEquals(2, quest.getGiverId());
        assertEquals(3, quest.getPlayerId());
        assertEquals(LocalDateTime.MAX, quest.getDateOfPosting());
        assertEquals(40, quest.getReward());
        assertEquals(QuestStatus.posted, quest.getStatus());
        assertEquals("word", quest.getWord());

        quest.setId(2);
        quest.setGiverId(3);
        quest.setPlayerId(4);
        quest.setDateOfPosting(LocalDateTime.MIN);
        quest.setReward(70);
        quest.setStatus(QuestStatus.completed);
        quest.setWord("word1");
        assertEquals(2, quest.getId());
        assertEquals(3, quest.getGiverId());
        assertEquals(4, quest.getPlayerId());
        assertEquals(LocalDateTime.MIN, quest.getDateOfPosting());
        assertEquals(70, quest.getReward());
        assertEquals(QuestStatus.completed, quest.getStatus());
        assertEquals("word1", quest.getWord());
    }
}
