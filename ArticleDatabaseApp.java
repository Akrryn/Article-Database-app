package com.example.xz;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArticleDatabaseApp extends Application {

    private final ObservableList<Article> articles = FXCollections.observableArrayList();
    private TableView<Article> tableView;
    private Stage primaryStage;
    private TextField authorSearchField;
    private TextField titleSearchField;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Загружаем данные из файла при старте программы
        List<Article> loadedArticles = DataStorage.loadData("articles.dat");
        if (loadedArticles != null) {
            articles.addAll(loadedArticles);
        }

        // Таблица
        tableView = new TableView<>(articles);

        // Создание колонок с номерами под заголовками
        TableColumn<Article, String> authorColumn = createNumberedColumn("Автор(ы)", "1", "author", 150);
        TableColumn<Article, String> titleColumn = createNumberedColumn("Название статьи", "2", "title", 200);
        TableColumn<Article, String> keywordsColumn = createNumberedColumn("Ключевые слова", "3", "keywords", 150);
        TableColumn<Article, String> summaryColumn = createNumberedColumn("Резюме статьи", "4", "summary", 200);
        TableColumn<Article, String> yearColumn = createNumberedColumn("Год", "5", "year", 100);
        TableColumn<Article, String> udcColumn = createNumberedColumn("УДК", "6", "udc", 200);

        tableView.getColumns().addAll(authorColumn, titleColumn, keywordsColumn, summaryColumn, yearColumn, udcColumn);

        // Скрыть все столбцы при запуске
        tableView.getColumns().forEach(column -> column.setVisible(false));

        // Поля ввода для поиска и кнопка "Поиск"
        authorSearchField = new TextField();
        authorSearchField.setPromptText("Автор(ы)");
        titleSearchField = new TextField();
        titleSearchField.setPromptText("Название статьи");
        Button searchButton = new Button("Поиск");
        Button resetButton = new Button("Сбросить поиск");

        searchButton.setOnAction(e -> searchArticles(authorSearchField.getText(), titleSearchField.getText()));
        resetButton.setOnAction(e -> resetSearch());

        HBox searchBox = new HBox(10, authorSearchField, titleSearchField, searchButton, resetButton);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(10));

        // Кнопки управления
        Button addButton = new Button("Добавить");
        Button editButton = new Button("Редактировать");
        Button deleteButton = new Button("Удалить");
        Button exportButton = new Button("Экспорт");
        Button printButton = new Button("Печать");
        Button toggleColumnsButton = new Button("Показать статьи");

        addButton.setOnAction(e -> showAddDialog());
        editButton.setOnAction(e -> showEditDialog());
        deleteButton.setOnAction(e -> deleteSelectedArticle());
        exportButton.setOnAction(e -> exportSelectedArticles(primaryStage));
        printButton.setOnAction(e -> printSelectedArticles());
        toggleColumnsButton.setOnAction(e -> toggleColumnsVisibility(toggleColumnsButton));

        HBox buttonsBox = new HBox(10, addButton, editButton, deleteButton, exportButton, printButton, toggleColumnsButton);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10));
        buttonsBox.setStyle("-fx-background-color: #F8F8F8; -fx-border-color: #C0C0C0; -fx-border-radius: 5px;");

        // Заголовок программы
        Label titleLabel = new Label("Электронный фонд статей");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        VBox layout = new VBox(10, titleLabel, searchBox, tableView, buttonsBox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #FAFAFA;");

        Scene mainScene = new Scene(layout, 1053, 550);

        // Подключение CSS-стиля
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Электронный фонд статей");
        primaryStage.show();
    }

    private TableColumn<Article, String> createNumberedColumn(String title, String number, String property, int width) {
        TableColumn<Article, String> column = new TableColumn<>(title + "\n" + number);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        column.setStyle("-fx-alignment: CENTER;");
        return column;
    }

    private void searchArticles(String authorQuery, String titleQuery) {
        if ((authorQuery == null || authorQuery.trim().isEmpty()) && (titleQuery == null || titleQuery.trim().isEmpty())) {
            showAlert("Ошибка", "Введите хотя бы одно ключевое слово для поиска.");
            return;
        }

        String lowerCaseAuthorQuery = authorQuery.toLowerCase();
        String lowerCaseTitleQuery = titleQuery.toLowerCase();
        List<Article> filteredArticles = articles.stream()
                .filter(article -> article.getAuthor().toLowerCase().contains(lowerCaseAuthorQuery) ||
                        article.getTitle().toLowerCase().contains(lowerCaseTitleQuery))
                .collect(Collectors.toList());

        tableView.setItems(FXCollections.observableArrayList(filteredArticles));
    }

    private void resetSearch() {
        authorSearchField.clear();
        titleSearchField.clear();
        tableView.setItems(articles);
    }

    private void showAddDialog() {
        Article newArticle = showArticleDialog(null);
        if (newArticle != null) {
            articles.add(newArticle);
        }
    }

    private void showEditDialog() {
        Article selectedArticle = tableView.getSelectionModel().getSelectedItem();
        if (selectedArticle != null) {
            Article updatedArticle = showArticleDialog(selectedArticle);
            if (updatedArticle != null) {
                // Найдем индекс выбранной статьи и заменим её в основной коллекции
                int selectedIndex = articles.indexOf(selectedArticle);
                if (selectedIndex >= 0) {
                    articles.set(selectedIndex, updatedArticle);  // Заменяем статью в основной коллекции

                    // Если есть активный поиск, нужно обновить отображение в соответствии с фильтром
                    if (!authorSearchField.getText().isEmpty() || !titleSearchField.getText().isEmpty()) {
                        searchArticles(authorSearchField.getText(), titleSearchField.getText());
                    } else {
                        tableView.setItems(articles); // Если поиск не активен, просто отображаем все статьи
                    }
                }
            }
        } else {
            showAlert("Ошибка", "Выберите статью для редактирования.");
        }
    }

    private void deleteSelectedArticle() {
        Article selectedArticle = tableView.getSelectionModel().getSelectedItem();
        if (selectedArticle != null) {
            articles.remove(selectedArticle);
        } else {
            showAlert("Ошибка", "Выберите статью для удаления.");
        }
    }

    private void exportSelectedArticles(Stage stage) {
        ObservableList<Article> selectedArticles = tableView.getSelectionModel().getSelectedItems();
        if (selectedArticles.isEmpty()) {
            showAlert("Ошибка", "Выберите хотя бы одну статью для экспорта.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Article article : selectedArticles) {
                    writer.write("Автор: " + article.getAuthor() + "\n");
                    writer.write("Название: " + article.getTitle() + "\n");
                    writer.write("Ключевые слова: " + article.getKeywords() + "\n");
                    writer.write("Резюме: " + article.getSummary() + "\n");
                    writer.write("Дата: " + article.getYear() + "\n");
                    writer.write("УДК: " + article.getUdc() + "\n");
                    writer.write("\n");
                }
                showAlert("Успех", "Статьи успешно экспортированы.");
            } catch (IOException e) {
                showAlert("Ошибка", "Не удалось сохранить файл.");
            }
        }
    }

    private void printSelectedArticles() {
        ObservableList<Article> selectedArticles = tableView.getSelectionModel().getSelectedItems();
        if (selectedArticles.isEmpty()) {
            showAlert("Ошибка", "Выберите хотя бы одну статью для печати.");
            return;
        }

        // Создаем диалоговое окно
        Dialog<Void> printDialog = new Dialog<>();
        printDialog.setTitle("Печать статей");
        printDialog.setHeaderText("Выберите тип печати и нажмите 'Сохранить'.");

        // Создаем RadioButton для выбора типа печати
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton pdfButton = new RadioButton("PDF");
        RadioButton txtButton = new RadioButton("Текстовый файл");
        RadioButton csvButton = new RadioButton("CSV");

        pdfButton.setToggleGroup(toggleGroup);
        txtButton.setToggleGroup(toggleGroup);
        csvButton.setToggleGroup(toggleGroup);

        // По умолчанию выбран текстовый файл
        txtButton.setSelected(true);

        // Кнопка "Сохранить"
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        printDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Добавляем RadioButton в диалог
        VBox vbox = new VBox(10, pdfButton, txtButton, csvButton);
        vbox.setPadding(new Insets(20));
        printDialog.getDialogPane().setContent(vbox);

        // Обработка нажатия на кнопку "Сохранить"
        printDialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
                String selectedFormat = selectedRadioButton.getText();

                // Здесь можно добавить логику для сохранения в выбранном формате
                showAlert("Успех", "Выбран формат: " );
            }
            return null;
        });

        // Показываем диалог
        printDialog.showAndWait();
    }

    private Article showArticleDialog(Article article) {
        TextField authorField = new TextField(article != null ? article.getAuthor() : "");
        TextField titleField = new TextField(article != null ? article.getTitle() : "");
        TextField keywordsField = new TextField(article != null ? article.getKeywords() : "");
        TextField summaryField = new TextField(article != null ? article.getSummary() : "");

        // Use DatePicker for full date selection
        DatePicker datePicker = new DatePicker(article != null && article.getYear() != null ?
                LocalDate.parse(article.getYear()) : LocalDate.now());

        TextField udcField = new TextField(article != null ? article.getUdc() : "");

        Dialog<Article> dialog = new Dialog<>();
        dialog.setTitle(article == null ? "Добавить статью" : "Редактировать статью");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox fieldsBox = new VBox(10,
                new Label("Автор:"), authorField,
                new Label("Название статьи:"), titleField,
                new Label("Ключевые слова:"), keywordsField,
                new Label("Резюме:"), summaryField,
                new Label("Дата:"), datePicker,
                new Label("УДК:"), udcField
        );
        fieldsBox.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(fieldsBox);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Article(authorField.getText(), titleField.getText(), keywordsField.getText(),
                        summaryField.getText(), datePicker.getValue() != null ? datePicker.getValue().toString() : "",
                        udcField.getText());
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void toggleColumnsVisibility(Button toggleColumnsButton) {
        // Проверяем, видны ли столбцы
        boolean areColumnsVisible = tableView.getColumns().get(0).isVisible();

        // Переключаем видимость всех столбцов
        tableView.getColumns().forEach(column -> column.setVisible(!areColumnsVisible));

        // Меняем текст кнопки в зависимости от состояния
        if (areColumnsVisible) {
            toggleColumnsButton.setText("Показать статьи");
        } else {
            toggleColumnsButton.setText("Скрыть статьи");
        }
    }

    @Override
    public void stop() {
        DataStorage.saveData(new ArrayList<>(articles), "articles.dat");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
