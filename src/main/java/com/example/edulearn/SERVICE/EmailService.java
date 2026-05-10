package com.example.edulearn.SERVICE;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    // Email Avec PDF

    public void sendEmailWithPdf(String to, String subject,
                                 String htmlBody, String pdfPath) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        // true = multipart (nécessaire pour les pièces jointes)
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = HTML

        // Ajout du PDF
        File pdfFile = new File(pdfPath);
        helper.addAttachment(pdfFile.getName(), pdfFile);

        mailSender.send(message);
        System.out.println("Email avec PDF envoyé !");
    }
    // Email HTML avec pièce jointe
    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = HTML
        // helper.addAttachment("fichier.pdf", new File("/path/to/file.pdf"));

        mailSender.send(message);
    }

    //
}