package com.majerpro.learning_platform.service.content;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;

@Service
public class JsoupScrapeService {

    public ScrapedPage scrape(String url) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            String title = document.title();
            String rawText = document.text();
            String cleanedText = extractMainContent(document);

            ScrapedPage page = new ScrapedPage();
            page.setUrl(url);
            page.setDomain(URI.create(url).getHost());
            page.setTitle(title);
            page.setStatusCode(200);
            page.setFetchedAt(LocalDateTime.now());
            page.setRawText(rawText);
            page.setCleanedText(cleanedText);
            return page;
        } catch (Exception e) {
            ScrapedPage page = new ScrapedPage();
            page.setUrl(url);
            page.setDomain(safeHost(url));
            page.setTitle("Failed to fetch");
            page.setStatusCode(500);
            page.setFetchedAt(LocalDateTime.now());
            page.setRawText("");
            page.setCleanedText("");
            return page;
        }
    }

    private String extractMainContent(Document document) {
        Element article = document.selectFirst("article");
        if (article != null && !article.text().isBlank()) {
            return article.text();
        }

        Element main = document.selectFirst("main");
        if (main != null && !main.text().isBlank()) {
            return main.text();
        }

        Element body = document.body();
        return body != null ? body.text() : "";
    }

    private String safeHost(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    public static class ScrapedPage {
        private String url;
        private String domain;
        private String title;
        private Integer statusCode;
        private LocalDateTime fetchedAt;
        private String rawText;
        private String cleanedText;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
        }

        public LocalDateTime getFetchedAt() {
            return fetchedAt;
        }

        public void setFetchedAt(LocalDateTime fetchedAt) {
            this.fetchedAt = fetchedAt;
        }

        public String getRawText() {
            return rawText;
        }

        public void setRawText(String rawText) {
            this.rawText = rawText;
        }

        public String getCleanedText() {
            return cleanedText;
        }

        public void setCleanedText(String cleanedText) {
            this.cleanedText = cleanedText;
        }
    }
}