package androidev.thegreenroom;

public class AboutSection {
    private String id;
    private String userId; // user has one about section each
    private String description;
    private String teaserUrl;


    public AboutSection() { }

    /**
     * About Section
     * A model class for About Section objects
     * @param id (String)
     * @param userId (String)
     * @param description (String)
     * @param teaserUrl (String)
     */
    public AboutSection(String id, String userId,
                        String description, String teaserUrl) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.teaserUrl = teaserUrl;
    }

    // getter methods
    public String getId() { return id; }
    public String getUserId() {
        return userId;
    }
    public String getDescription() {
        return description;
    }
    public String getTeaserUrl() { return teaserUrl; }

    // setter methods
    public void setId(String id) {
        this.id = id;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setTeaserUrl(String teaserUrl) {
        this.teaserUrl = teaserUrl;
    }
}
