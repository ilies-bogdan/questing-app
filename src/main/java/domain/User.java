package domain;

public class User implements Entity<Integer> {
    private int id;
    private String username;
    private String email;
    private int passwordCode;
    private String salt;
    private Rank rank;
    private int tokenCount;

    public User() {
        this.id = 0;
        this.username = "";
        this.email = "";
        this.passwordCode = 0;
        this.salt = "";
        this.rank = null;
        this.tokenCount = 0;
    }

    public User(int id, String username, String email, int passwordCode, String salt, Rank rank, int tokenCount) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordCode = passwordCode;
        this.salt = salt;
        this.rank = rank;
        this.tokenCount = tokenCount;
    }

    public User(String username, String email, int passwordCode, String salt, Rank rank, int tokenCount) {
        this.username = username;
        this.email = email;
        this.passwordCode = passwordCode;
        this.salt = salt;
        this.rank = rank;
        this.tokenCount = tokenCount;
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPasswordCode() {
        return passwordCode;
    }

    public void setPasswordCode(int passwordCode) {
        this.passwordCode = passwordCode;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }
}
