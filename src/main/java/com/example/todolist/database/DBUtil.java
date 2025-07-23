package com.example.todolist.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	private static final String URL = "jdbc:mysql://localhost:3306/todo_db";
	private static final String USER = "root";
	private static final String PASSWORD = "Ifeanyi2003"; // change this

	public static Connection connect() {
		try {
			return DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}

