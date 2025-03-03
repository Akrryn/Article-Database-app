package com.example.xz;

import java.io.*;
import java.util.List;

public class DataStorage {

    // Метод для сохранения списка в файл
    public static void saveData(List<Article> articles, String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(articles); // Сохраняем список статей
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для загрузки списка из файла
    public static List<Article> loadData(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (List<Article>) in.readObject(); // Загружаем список статей
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null; // Если файл не найден или ошибка, возвращаем null
    }
}
