package domain;

import java.time.LocalDateTime;

public class Quest implements Entity<Integer> {
    private int id;
    private int giverId;
    private int playerId;
    private LocalDateTime dateOfPosting;
    private int reward;
    private QuestStatus status;
    private String word;

    public Quest() {
        this.id = 0;
        this.giverId = 0;
        this.playerId = 0;
        this.dateOfPosting = null;
        this.reward = 0;
        this.status = null;
        this.word = "";
    }

    public Quest(int giverId, int playerId, LocalDateTime dateOfPosting, int reward, QuestStatus status, String word) {
        this.giverId = giverId;
        this.playerId = playerId;
        this.dateOfPosting = dateOfPosting;
        this.reward = reward;
        this.status = status;
        this.word = word;
    }

    public Quest(int id, int giverId, int playerId, LocalDateTime dateOfPosting, int reward, QuestStatus status, String word) {
        this.id = id;
        this.giverId = giverId;
        this.playerId = playerId;
        this.dateOfPosting = dateOfPosting;
        this.reward = reward;
        this.status = status;
        this.word = word;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public int getGiverId() {
        return giverId;
    }

    public void setGiverId(int giverId) {
        this.giverId = giverId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public LocalDateTime getDateOfPosting() {
        return dateOfPosting;
    }

    public void setDateOfPosting(LocalDateTime dateOfPosting) {
        this.dateOfPosting = dateOfPosting;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
