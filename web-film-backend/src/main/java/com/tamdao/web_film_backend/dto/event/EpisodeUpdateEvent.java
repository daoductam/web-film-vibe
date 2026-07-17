package com.tamdao.web_film_backend.dto.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EpisodeUpdateEvent extends ApplicationEvent {
    
    private final String movieSlug;
    private final String movieTitle;
    private final String episodeName;
    private final String thumbUrl;

    public EpisodeUpdateEvent(Object source, String movieSlug, String movieTitle, String episodeName, String thumbUrl) {
        super(source);
        this.movieSlug = movieSlug;
        this.movieTitle = movieTitle;
        this.episodeName = episodeName;
        this.thumbUrl = thumbUrl;
    }
}
