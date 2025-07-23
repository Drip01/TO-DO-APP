package com.example.todolist.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {
	private static final Properties properties = new Properties();

	static {
		try {
			FileInputStream fis = new FileInputStream("config.properties");
			properties.load(fis);
		} catch (IOException e) {
			System.err.println("❌ Could not load config.properties: " + e.getMessage());
		}
	}

	public static String get(String key) {
		return properties.getProperty(key);
	}
}
