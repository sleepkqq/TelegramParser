package com.sleepkqq.telegramparser.service.upload;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Component
@PropertySource("application.properties")
public class YoutubeUpload {

    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube.upload");
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    @Value("${application.secrets.json}")
    private String CLIENT_SECRETS;
    @Value("${application.name}")
    private String APPLICATION_NAME;


    public YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential authorize() throws IOException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(CLIENT_SECRETS))
                .createScoped(Arrays.asList(YouTubeScopes.YOUTUBE));

        credential.refreshToken();
        return credential;
    }

    public void uploadVideo(String videoPath)
            throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();

        Video video = new Video();

        VideoSnippet snippet = new VideoSnippet();
        snippet.setCategoryId("22");
        snippet.setDescription("Description of uploaded video.");
        snippet.setTitle("Test video upload.");
        video.setSnippet(snippet);

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("private");
        video.setStatus(status);

        File mediaFile = new File(videoPath);
        InputStreamContent mediaContent =
                new InputStreamContent("application/octet-stream",
                        new BufferedInputStream(new FileInputStream(mediaFile)));
        mediaContent.setLength(mediaFile.length());

        YouTube.Videos.Insert request = youtubeService.videos()
                .insert(Collections.singletonList("snippet,status"), video, mediaContent);
        Video response = request.setNotifySubscribers(true).execute();
        System.out.println(response);
    }
}
