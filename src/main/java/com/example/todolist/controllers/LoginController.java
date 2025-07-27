package com.example.todolist.controllers;

// Tools used for database access, email sending, and controlling the login screen
import com.example.todolist.database.DBUtil;
import com.example.todolist.utils.MailUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
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
		String email = emailField.getText().trim();
		String password = passwordField.getText().trim();

		if (email.isEmpty() || password.isEmpty()) {
			showAlert("Login Failed", "Please enter both email and password.");
			return;
		}

		Connection conn = DBUtil.connect();
		if (conn == null) {
			showAlert("Database Error", "Could not connect to the database.");
			return;
		}

		try {
			// Step 1: Check if the email exists
			String checkEmailQuery = "SELECT * FROM users WHERE email = ?";
			PreparedStatement checkStmt = conn.prepareStatement(checkEmailQuery);
			checkStmt.setString(1, email);
			ResultSet rs = checkStmt.executeQuery();

			if (rs.next()) {
				// Email exists, now check the password
				String dbPassword = rs.getString("password");

				if (password.equals(dbPassword)) {
					int userId = rs.getInt("id");
					String name = rs.getString("name");

					// ✅ Send login email
					MailUtil.sendEmail(
							email,
							"Login to TO-DO-APP",
							"Hello " + name + ", you have successfully logged into your account."
					);

					loadTodoScene(userId, email);
				} else {
					showAlert("Login Failed", "Incorrect password.");
				}
			} else {
				showAlert("Login Failed", "No account found with that email.");
			}

			rs.close();
			checkStmt.close();
			conn.close();

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

	@FXML
	private void handleMouseEnter(javafx.scene.input.MouseEvent event) {
		Hyperlink link = (Hyperlink) event.getSource();
		link.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db; -fx-underline: false;");
	}

	@FXML
	private void handleMouseExit(javafx.scene.input.MouseEvent event) {
		Hyperlink link = (Hyperlink) event.getSource();
		link.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db; -fx-underline: false;");
	}
}
