package com.example.xz;

import java.io.Serializable;

public class Article implements Serializable {
    private static final long serialVersionUID = 1L; // Рекомендуется добавлять serialVersionUID
    private final String author;
    private final String title;
    private final String keywords;
    private final String summary;
    private final String year;
    private final String udc;

    public Article(String author, String title, String keywords, String summary, String year, String udc) {
        this.author = author;
        this.title = title;
        this.keywords = keywords;
        this.summary = summary;
        this.year = year;
        this.udc = udc;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getSummary() {
        return summary;
    }

    public String getYear() {
        return year;
    }

    public String getUdc() {
        return udc;
    }

    @Override
    public String toString() {
        return "Автор: " + author + ", Название: " + title + ", Ключевые слова: " + keywords + ", Резюме: " + summary +
                ", Год: " + year + ", УДК: " + udc;
    }
}
