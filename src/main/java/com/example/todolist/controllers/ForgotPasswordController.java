package com.example.todolist.controllers;

// These are tools for working with the app's database, screen switching, and alerts
import com.example.todolist.database.DBUtil;
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

public class ForgotPasswordController {

	// These connect to the text fields on the password reset screen
	@FXML private TextField emailField;
	@FXML private PasswordField newPasswordField;
	@FXML private PasswordField confirmPasswordField;

	// This runs when the user clicks the "Reset" button
	public void handleReset(ActionEvent event) {
		String email = emailField.getText();                     // Get the email entered
		String newPassword = newPasswordField.getText();         // Get new password
		String confirmPassword = confirmPasswordField.getText(); // Get confirmation

		// Check if any field is empty
		if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
			showAlert("All fields are required.");
			return;
		}

		// Check if new password matches confirmation
		if (!newPassword.equals(confirmPassword)) {
			showAlert("Passwords do not match.");
			return;
		}

		// Connect to the database
		try (Connection conn = DBUtil.connect()) {
			// Check if the entered email exists in the database
			String checkSql = "SELECT * FROM users WHERE email = ?";
			PreparedStatement checkStmt = conn.prepareStatement(checkSql);
			checkStmt.setString(1, email);
			ResultSet rs = checkStmt.executeQuery();

			if (!rs.next()) {
				// If email is not found, show an error
				showAlert("Email not found.");
				return;
			}

			// If email exists, update the user's password
			String updateSql = "UPDATE users SET password = ? WHERE email = ?";
			PreparedStatement updateStmt = conn.prepareStatement(updateSql);
			updateStmt.setString(1, newPassword); // ⚠️ Password should be encrypted in real apps
			updateStmt.setString(2, email);
			updateStmt.executeUpdate();

			// Show success message
			showAlert("Password updated successfully!");

			// Go back to the login screen
			loadScene("/fxml/login.fxml", "Login");

		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Something went wrong.");
		}
	}

	// This is run when the user clicks "Back to Login"
	public void handleBackToLogin(ActionEvent event) {
		loadScene("/fxml/login.fxml", "Login");
	}

	// A helper method to switch screens (scenes)
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

	// A helper method to show pop-up messages (like alerts)
	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Reset Password");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
