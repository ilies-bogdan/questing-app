package domain;

public class Badge implements Entity<Integer> {
    private int id;
    private String title;
    private String description;
    private BadgeType type;
    private int requirement;

    public Badge() {
        this.id = 0;
        this.title = "";
        this.description = "";
        this.type = null;
        this.requirement = 0;
    }

    public Badge(String title, String description, BadgeType type, int requirement) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.requirement = requirement;
    }

    public Badge(int id, String title, String description, BadgeType type, int requirement) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.requirement = requirement;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BadgeType getType() {
        return type;
    }

    public void setType(BadgeType type) {
        this.type = type;
    }

    public int getRequirement() {
        return requirement;
    }

    public void setRequirement(int requirement) {
        this.requirement = requirement;
    }
}
