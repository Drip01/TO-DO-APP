package com.example.todolist.controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
		Scene scene = new Scene(loader.load(), 800, 600);
		stage.setScene(scene);

		stage.setMinWidth(800);
		stage.setMinHeight(600);
		stage.setResizable(false);
		stage.setTitle("To-Do App");
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}
