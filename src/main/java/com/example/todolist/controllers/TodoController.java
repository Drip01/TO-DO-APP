package com.example.todolist.controllers;

// Importing helper classes for database access, email sending, and JavaFX UI elements
import com.example.todolist.database.DBUtil;
import com.example.todolist.utils.MailUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class TodoController {

	// These are links to UI elements on the screen (like the text field and buttons)
	@FXML private DatePicker datePicker;
	@FXML private TextField taskField;
	@FXML private ListView<String> taskListView;            // List for pending tasks
	@FXML private ListView<String> completedTaskListView;   // List for completed tasks
	@FXML private Button clearCompletedButton;
	@FXML private ComboBox<String> dateFilterComboBox;

	// These are lists that store the actual task data (in memory)
	private final ObservableList<String> tasks = FXCollections.observableArrayList();
	private final ObservableList<String> completedTasks = FXCollections.observableArrayList();

	// These store the logged-in user's information
	private int userId;
	private String userEmail;

	// Called after user logs in to set their ID and email, then load their tasks
	public void setUserDetails(int id, String email) {
		this.userId = id;
		this.userEmail = email;
		loadTasks();           // Load tasks from the database
		setupDateFilter();     // Setup the date filter options
	}

	@FXML
	public void initialize() {
		taskListView.setItems(tasks);
		completedTaskListView.setItems(completedTasks);

		taskListView.setCellFactory(listView -> new ListCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
				} else {
					Label taskLabel = new Label(item);
					Button completeBtn = new Button("✔");
					completeBtn.setOnAction(e -> markAsCompleted(item));

					// HBox layout to align button at the end
					HBox hbox = new HBox();
					hbox.setSpacing(10);
					hbox.setStyle("-fx-alignment: center-left; -fx-padding: 5 10 5 10;"); // optional styling
					HBox spacer = new HBox(); // expands to push the button to the right
					HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

					hbox.getChildren().addAll(taskLabel, spacer, completeBtn);
					setGraphic(hbox);
				}
			}
		});
	}

	private void setupDateFilter() {
		// Dropdown with filtering options like "Today", "Yesterday", etc.
		if (dateFilterComboBox != null) {
			dateFilterComboBox.getItems().addAll("All", "Today", "Yesterday", "Last 5 Days");
			dateFilterComboBox.setValue("All");
			dateFilterComboBox.setOnAction(e -> handleDateFilter());
		}
	}

	// When user clicks "Add Task" button
	public void handleAddTask(ActionEvent event) {
		String taskText = taskField.getText();  // Get the entered task
		if (!taskText.isEmpty()) {
			try (Connection conn = DBUtil.connect()) {
				// Save the task in the database
				String sql = "INSERT INTO tasks (user_id, task_text, completed) VALUES (?, ?, false)";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setInt(1, userId);
				stmt.setString(2, taskText);
				stmt.executeUpdate();

				// Add task to the UI and clear input field
				tasks.add(taskText);
				taskField.clear();
			} catch (Exception e) {
				e.printStackTrace();
				showAlert("Database Error", "Could not add task.");
			}
		}
	}

	// Mark a task as completed (called when ✔ button is clicked)
	private void markAsCompleted(String taskText) {
		tasks.remove(taskText);            // Remove from pending list
		completedTasks.add(taskText);      // Add to completed list

		// Update the task in the database as completed
		try (Connection conn = DBUtil.connect()) {
			String sql = "UPDATE tasks SET completed = true WHERE user_id = ? AND task_text = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userId);
			stmt.setString(2, taskText);
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Database Error", "Could not update task.");
		}
	}

	// Load all tasks from the database for the current user
	private void loadTasks() {
		tasks.clear();
		completedTasks.clear();

		try (Connection conn = DBUtil.connect()) {
			String sql = "SELECT task_text, completed FROM tasks WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userId);
			ResultSet rs = stmt.executeQuery();

			// Separate tasks into "completed" and "not completed"
			while (rs.next()) {
				String task = rs.getString("task_text");
				if (rs.getBoolean("completed")) completedTasks.add(task);
				else tasks.add(task);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Error", "Could not load tasks.");
		}
	}

	// Clear all completed tasks when user clicks "Clear Completed"
	public void handleClearCompletedTasks(ActionEvent event) {
		try (Connection conn = DBUtil.connect()) {
			String sql = "DELETE FROM tasks WHERE user_id = ? AND completed = true";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userId);
			stmt.executeUpdate();
			completedTasks.clear();  // Remove from the screen too
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Database Error", "Could not clear completed tasks.");
		}
	}

	// Filter tasks based on selected option from the ComboBox (All, Today, etc.)
	public void handleDateFilter() {
		String filter = dateFilterComboBox.getValue();
		tasks.clear();
		completedTasks.clear();

		String sql = "SELECT task_text, completed FROM tasks WHERE user_id = ?";
		if ("Today".equals(filter)) {
			sql += " AND DATE(created_at) = CURDATE()";
		} else if ("Yesterday".equals(filter)) {
			sql += " AND DATE(created_at) = CURDATE() - INTERVAL 1 DAY";
		} else if ("Last 5 Days".equals(filter)) {
			sql += " AND DATE(created_at) >= CURDATE() - INTERVAL 5 DAY";
		}

		try (Connection conn = DBUtil.connect()) {
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				String task = rs.getString("task_text");
				if (rs.getBoolean("completed")) completedTasks.add(task);
				else tasks.add(task);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Error", "Could not filter tasks.");
		}
	}

	// Filter tasks by a specific date selected from the DatePicker
	public void handleSpecificDateFilter() {
		LocalDate selectedDate = datePicker.getValue();
		if (selectedDate == null) return;

		tasks.clear();
		completedTasks.clear();

		String sql = "SELECT task_text, completed FROM tasks WHERE user_id = ? AND DATE(created_at) = ?";
		try (Connection conn = DBUtil.connect()) {
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userId);
			stmt.setString(2, selectedDate.toString());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				String task = rs.getString("task_text");
				if (rs.getBoolean("completed")) completedTasks.add(task);
				else tasks.add(task);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Error", "Could not load tasks by date.");
		}
	}

	// Logs out the user and sends them an email notification
	public void handleLogout(ActionEvent event) {
		try {
			MailUtil.sendEmail(userEmail, "Logout from TO-DO-APP", "You have successfully logged out of your account.");

			// Go back to login screen
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
			Stage stage = (Stage) taskField.getScene().getWindow();
			stage.setScene(new Scene(loader.load()));
			stage.setTitle("Login");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Show an error message box on screen
	private void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
