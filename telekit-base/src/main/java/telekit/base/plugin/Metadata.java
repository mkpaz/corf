package telekit.base.plugin;

public class Metadata {

    private String name;
    private String author;
    private String version;
    private String description;
    private String homePage;
    private String platformVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", homePage='" + homePage + '\'' +
                ", platformVersion='" + platformVersion + '\'' +
                '}';
    }
}
