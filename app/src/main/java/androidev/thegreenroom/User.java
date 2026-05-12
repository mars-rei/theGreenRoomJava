package androidev.thegreenroom;

public class User {
    private String id;
    private String userType;
    private String username;
    private String bio;
    private String location;
    private String profilePictureUrl;
    private String headerPhotoUrl;

    // private String email;
    // private int numEvents;

    public User() { }
    public User(String id, String userType, String username,
                String bio, String location, String profilePictureUrl,
                String headerPhotoUrl) {
        this.id = id;
        this.userType = userType;
        this.username = username;
        this.bio = bio;
        this.location = location;
        this.profilePictureUrl = profilePictureUrl;
        this.headerPhotoUrl = headerPhotoUrl;
    }

    // getter methods
    public String getId() {
        return id;
    }
    public String getUserType() {
        return userType;
    }
    public String getUsername() {
        return username;
    }
    public String getBio() {
        return bio;
    }
    public String getLocation() {
        return location;
    }
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    public String getHeaderPhotoUrl() {
        return headerPhotoUrl;
    }

    // setter methods
    public void setId(String id) {
        this.id = id;
    }
    public void setUserType(String userType) {
        this.userType = userType;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    public void setHeaderPhotoUrl(String headerPhotoUrl) {
        this.headerPhotoUrl = headerPhotoUrl;
    }
}
