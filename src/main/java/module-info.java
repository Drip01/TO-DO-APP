module com.example.todolist {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires jakarta.mail;


	opens com.example.todolist.controllers to javafx.fxml;
	exports com.example.todolist.controllers;
}