package com.example.todolist.controllers;

// Tools used for database access, email sending, and controlling the login screen
import com.example.todolist.database.DBUtil;
import com.example.todolist.utils.MailUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

	// These are connected to the email and password fields on the login screen
	@FXML private TextField emailField;
	@FXML private PasswordField passwordField;

	// This is triggered when the user clicks the "Login" button
	public void handleLogin(ActionEvent event) {
		String email = emailField.getText().trim();       // Get what the user typed in the email box
		String password = passwordField.getText().trim(); // Get the password they typed

		Connection conn = DBUtil.connect();               // Try to connect to the database

		if (conn == null) {
			// If the database connection fails, show an error message
			showAlert("Database Connection Error", "Couldn't connect to the database.");
			return;
		}

		try {
			// Check if there's a user in the database with this email and password
			String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, email);
			stmt.setString(2, password);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				// If user exists, retrieve their info
				int userId = rs.getInt("id");
				String name = rs.getString("name");

				// ✅ Send email notification for login success
				MailUtil.sendEmail(
						email,
						"Login to TO-DO-APP",
						"Hello " + name + ", you have successfully logged into your account."
				);

				// Move to the main To-Do screen
				loadTodoScene(userId, email);
			} else {
				// If no match is found, show login error
				showAlert("Login Failed", "Invalid email or password.");
			}

		} catch (SQLException | IOException e) {
			e.printStackTrace();
			showAlert("Login Error", "Something went wrong during login.");
		}
	}

	// This switches the user to the To-Do list screen if login is successful
	private void loadTodoScene(int userId, String email) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/todo.fxml"));
		Scene scene = new Scene(loader.load());

		// Get the controller for the To-Do screen and pass in the user info
		TodoController controller = loader.getController();
		controller.setUserDetails(userId, email); // So it knows who is logged in

		Stage stage = (Stage) emailField.getScene().getWindow(); // Get the current window
		stage.setScene(scene);                                   // Show the To-Do screen
		stage.setTitle("My To-Do List");
	}

	// When the user clicks "Sign Up", take them to the Sign-Up screen
	public void handleSignup(ActionEvent event) {
		loadScene("/fxml/signup.fxml", "Sign Up");
	}

	// When the user clicks "Forgot Password", take them to the password recovery screen
	public void handleForgotPassword(ActionEvent event) {
		loadScene("/fxml/forgot_password.fxml", "Forgot Password");
	}

	// This is a reusable function to switch between screens
	private void loadScene(String fxml, String title) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Stage stage = (Stage) emailField.getScene().getWindow();
			stage.setScene(new Scene(loader.load()));
			stage.setTitle(title);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Show a popup message box when there's an error
	private void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
