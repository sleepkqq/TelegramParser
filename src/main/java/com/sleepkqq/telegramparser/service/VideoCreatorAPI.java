package com.sleepkqq.telegramparser.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sleepkqq.telegramparser.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Random;


@Component
@Slf4j
@PropertySource("application.properties")
public class VideoCreatorAPI {

    private static final String VIDEO_RENDER = "https://api.shotstack.io/edit/stage/render";
    @Value("${video.api.key.first}")
    private String API_KEY_FIRST;
    @Value("${video.api.key.second}")
    private String API_KEY_SECOND;
    @Autowired
    private VideoRepository videoRepository;

    public String createVideo(String srcMeme, double imageRatio) {

        try {
            HttpClient httpClient = HttpClients.createDefault();

            // первый запрос (получение токена для видео)
            HttpPost httpPost = new HttpPost(VIDEO_RENDER);

            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("x-api-key", API_KEY_SECOND);

            String randomVideo = getRandomVideoUrl();

            // JSON запрос с параметрами для видео
            String jsonBody = "{" +
                    "\"timeline\": {" +
                    "    \"background\": \"#000000\"," +
                    "    \"tracks\": [" +
                    "        {" +
                    "            \"clips\": [" +
                    "                {" +
                    "                    \"asset\": {" +
                    "                        \"type\": \"image\"," +
                    "                        \"src\": \"" + srcMeme + "\"" +
                    "                    }," +
                    "                    \"start\": 0," +
                    "                    \"length\": 9," +
                    "                    \"offset\": {" +
                    "                        \"x\": 0.004," +
                    "                        \"y\": 0.075" +
                    "                    }," +
                    "                    \"position\": \"center\"," +
                    "                    \"scale\": " + imageRatio +
                    "                }" +
                    "            ]" +
                    "        }," +
                    "        {" +
                    "            \"clips\": [" +
                    "                {" +
                    "                    \"asset\": {" +
                    "                        \"type\": \"video\"," +
                    "                        \"src\": \"" + randomVideo + "\"," +
                    "                        \"volume\": 0" +
                    "                    }," +
                    "                    \"start\": 0," +
                    "                    \"length\": 9," +
                    "                    \"offset\": {" +
                    "                        \"x\": 0," +
                    "                        \"y\": 0" +
                    "                    }," +
                    "                    \"position\": \"center\"" +
                    "                }" +
                    "            ]" +
                    "        }" +
                    "    ]" +
                    "}," +
                    "\"output\": {" +
                    "    \"format\": \"mp4\"," +
                    "    \"fps\": 30," +
                    "    \"scaleTo\": \"mobile\"," +
                    "    \"size\": {" +
                    "        \"width\": 1080," +
                    "        \"height\": 1920" +
                    "    }," +
                    "    \"destinations\": []" +
                    "}" +
                    "}";

            StringEntity entity = new StringEntity(jsonBody);
            httpPost.setEntity(entity);

            log.info("Отправлен запрос на токен для видео");
            HttpResponse response = httpClient.execute(httpPost);
            log.info("Успешно получен токен");

            String responseBody = EntityUtils.toString(response.getEntity());

            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            String renderId = jsonObject.getAsJsonObject("response").get("id").getAsString();

            // второй запрос (генерация видео)
            HttpGet httpGet = new HttpGet(VIDEO_RENDER + "/" + renderId);

            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("x-api-key", API_KEY_SECOND);

            log.info("Отправка данных для генерации видео");
            httpClient.execute(httpGet);

            Thread.sleep(60000);
            log.info("Видео успешно сгенерировано");

            return "https://cdn.shotstack.io/au/stage/" + API_KEY_FIRST + "/" + renderId + ".mp4";

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getRandomVideoUrl() {
        try {
            return videoRepository.findById(new Random().nextLong(videoRepository.count()) + 1).get().getUrl();
        } catch (Exception e) {
            return getRandomVideoUrl();
        }
    }

}
