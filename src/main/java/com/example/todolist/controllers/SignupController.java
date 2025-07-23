package com.example.todolist.controllers;

// These are tools the app uses: database connection, email sending, and UI controls
import com.example.todolist.database.DBUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.example.todolist.utils.MailUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignupController {

	// These refer to the text fields in the sign-up form screen
	@FXML private TextField nameField;        // For user's name
	@FXML private TextField emailField;       // For user's email
	@FXML private PasswordField passwordField; // For user's password (hidden input)

	// This runs when user clicks "Sign Up"
	public void handleSignup(ActionEvent event) {
		String name = nameField.getText().trim();         // Get name entered by user
		String email = emailField.getText().trim();       // Get email entered
		String password = passwordField.getText().trim(); // Get password

		try (Connection conn = DBUtil.connect()) {
			// Save the user’s info into the database
			String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setString(2, email);
			stmt.setString(3, password); // ⚠️ In real apps, passwords should be encrypted!
			stmt.executeUpdate();

			System.out.println("User saved successfully.");

			// ✅ Send a welcome email to the user
			MailUtil.sendEmail(
					email,
					"Welcome to To-Do App",
					"Hello " + name + ",\n\nYour account has been successfully created.\n\nHappy planning!"
			);

			// Let user know account was created
			showAlert("Success", "Account created successfully. Please check your email.");

			// Switch to the login screen
			loadScene("/fxml/login.fxml", "Login");

		} catch (SQLException e) {
			// If something goes wrong (e.g., email already used), show error
			e.printStackTrace();
			showAlert("Sign Up Error", "Email already exists or input is invalid.");
		}
	}

	// This takes user back to login screen when "Back to Login" is clicked
	public void handleBackToLogin(ActionEvent event) {
		loadScene("/fxml/login.fxml", "Login");
	}

	// This method changes the screen (scene) to another FXML file (page)
	private void loadScene(String fxml, String title) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Stage stage = (Stage) nameField.getScene().getWindow();
			stage.setScene(new Scene(loader.load()));
			stage.setTitle(title);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// This shows a pop-up message (alert) to the user
	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
