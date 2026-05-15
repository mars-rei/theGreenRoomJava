package androidev.thegreenroom;

public class ShowcasePost {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String pictureUrl;

    public ShowcasePost() {}

    /**
     * Showcase Post
     * A model class for Showcase Post objects
     * @param id (String)
     * @param userId (String)
     * @param title (String)
     * @param description (String)
     * @param pictureUrl (String)
     */
    public ShowcasePost(String id, String userId, String title,
                        String description, String pictureUrl) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.pictureUrl = pictureUrl;
    }

    // getter methods
    public String getId() { return id; }
    public String getUserId() {
        return userId;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getPictureUrl() { return pictureUrl; }

    // setter methods
    public void setId(String id) {
        this.id = id;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
