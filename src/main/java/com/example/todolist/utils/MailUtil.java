package com.example.todolist.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class MailUtil {

	public static void sendEmail(String to, String subject, String messageText) {
		// Defensive check: prevent sending if recipient is empty
		if (to == null || to.trim().isEmpty()) {
			System.err.println("❌ Email not sent: recipient email is missing.");
			return;
		}

		final String from = ConfigUtil.get("email.sender");
		final String password = ConfigUtil.get("email.password");

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, password);
			}
		});

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			msg.setSubject(subject);
			msg.setText(messageText);
			Transport.send(msg);
			System.out.println("✅ Email sent successfully to: " + to);
		} catch (MessagingException e) {
			System.err.println("❌ Failed to send email to: " + to);
			e.printStackTrace();
		}
	}
}
