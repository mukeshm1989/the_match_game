package com.real.dto;

import java.util.List;

public class ExternalMovieDetails {

    String mediaId;
    String Title;
    String originalReleaseDate;
    String mediaType;
    List<String> actors;
    String director;
    String xboxLiveURL;

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getOriginalReleaseDate() {
        return originalReleaseDate;
    }

    public void setOriginalReleaseDate(String originalReleaseDate) {
        this.originalReleaseDate = originalReleaseDate;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public List<String> getActors() {
        return actors;
    }

    public void setActors(List<String> actors) {
        this.actors = actors;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getXboxLiveURL() {
        return xboxLiveURL;
    }

    public void setXboxLiveURL(String xboxLiveURL) {
        this.xboxLiveURL = xboxLiveURL;
    }

    @Override
    public String toString() {
        return "ExternalMovieDetails{" +
                "mediaId='" + mediaId + '\'' +
                ", Title='" + Title + '\'' +
                ", originalReleaseDate='" + originalReleaseDate + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", actors=" + actors +
                ", director='" + director + '\'' +
                ", xboxLiveURL='" + xboxLiveURL + '\'' +
                '}';
    }

    public ExternalMovieDetails() {
    }
}
