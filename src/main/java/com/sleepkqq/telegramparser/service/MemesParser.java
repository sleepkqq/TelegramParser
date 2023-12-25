package com.sleepkqq.telegramparser.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MemesParser {

    private static final String URL = "https://www.anekdot.ru/last/mem/";

    public List<String> downloadNewMemes() {

        List<String> srcMemes = new ArrayList<>();

        try {
            Document document = Jsoup.connect(URL).get();

            Elements elements = document.select("div.col-left.col-left-margin > *");
            Elements imgElements = elements.select("img");

            for (var element : imgElements) {
                String src = element.attr("src");
                if (src.endsWith("png") || src.endsWith("jpg"))
                    srcMemes.add(src);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return srcMemes;
    }

}
