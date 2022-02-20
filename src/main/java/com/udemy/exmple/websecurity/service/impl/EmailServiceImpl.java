package com.udemy.exmple.websecurity.service.impl;

import com.sun.mail.smtp.SMTPTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static com.udemy.exmple.websecurity.constant.EmailConstant.*;

@Slf4j
@Service
public class EmailServiceImpl {

    public void sendNewPasswordEmail(String FirstName, String LastName, String password, String email) throws MessagingException {
        Message message = createEmail(FirstName, LastName, password, email);
        SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(SMTP_PROTOCOL);
        smtpTransport.connect(HOST, USERNAME, PASSWORD);
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
    }

    private Message createEmail(String FirstName, String LastName, String password, String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);
        message.setText("Hello" + FirstName + "Your account password is " + password);
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }


    private Session getEmailSession() {
        Properties properties = System.getProperties();
        properties.put(SMTP_HOST, HOST);
        properties.put(SMTP_PORT, PORT);
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_STARTTLS_ENABLED, true);
        properties.put(SMTP_STARTTLS_REQUIRED, true);
        return Session.getInstance(properties, null);
    }

}
