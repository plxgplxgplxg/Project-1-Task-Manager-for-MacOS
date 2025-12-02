package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;

public class StudentTable {
    
    private TableView<Student> table;
    private ObservableList<Student> data;

    public StudentTable() {
        table = new TableView<>();
        table.setEditable(true);

        //name:
        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> {
            e.getRowValue().setName(e.getNewValue());
        });

        //age:
        TableColumn<Student, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        ageCol.setOnEditCommit(e -> {
            e.getRowValue().setAge(e.getNewValue());
        });

        table.getColumns().addAll(nameCol, ageCol);

        data = FXCollections.observableArrayList(
            new Student("Linh", 20),
            new Student("Ha", 1),
            new Student("Hoa", 22)
        );
        table.setItems(data);
    }

    public VBox getView() {

        TextField nameField = new TextField();
        nameField.setPromptText("Enter name");

        TextField ageField = new TextField();
        ageField.setPromptText("Enter age");

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> {
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());

            data.add(new Student(name, age));
            nameField.clear();
            ageField.clear();
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            Student s = table.getSelectionModel().getSelectedItem();
            if(s != null) data.remove(s);
        });

        return new VBox(10, table, nameField, ageField, addBtn, deleteBtn);

    }
}
