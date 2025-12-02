package com.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        TableView<Student> table = new TableView<>();

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(200);

        TableColumn<Student, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setMinWidth(100);

        table.getColumns().addAll(nameCol, ageCol);

        ObservableList<Student> data = FXCollections.observableArrayList(
            new Student("Linh", 20),
            new Student("An", 21),
            new Student("Binh", 19)

        );
        
        table.setItems(data);

        VBox root = new VBox(table);

        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("Bai 6");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

   
}
