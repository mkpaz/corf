package corf.base.plugin;

import org.jetbrains.annotations.Nullable;

/**
 * Plugin metadata. This merely a data carrier but not that name,
 * version and platform version are mandatory fields.
 */
public class Metadata {

    private @Nullable String name;
    private @Nullable String version;
    private @Nullable String platformVersion;
    private @Nullable String author;
    private @Nullable String description;
    private @Nullable String homePage;

    public Metadata() { }

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public @Nullable String getVersion() {
        return version;
    }

    public void setVersion(@Nullable String version) {
        this.version = version;
    }

    public @Nullable String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(@Nullable String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public @Nullable String getAuthor() {
        return author;
    }

    public void setAuthor(@Nullable String author) {
        this.author = author;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public @Nullable String getHomePage() {
        return homePage;
    }

    public void setHomePage(@Nullable String homePage) {
        this.homePage = homePage;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", platformVersion='" + platformVersion + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", homePage='" + homePage + '\'' +
                '}';
    }
}
