package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.Base64;

@Service
public class SmtpClientService {

    @Value("${mail.smtp.host}")
    private String host;

    @Value("${mail.smtp.port}")
    private int port;

    @Value("${mail.smtp.user}")
    private String user;

    @Value("${mail.smtp.password}")
    private String password;

    @Value("${mail.smtp.ssl:false}")
    private boolean useSsl;

    public void sendEmail(String to, String subject, String body) throws IOException {
        Socket rawSocket = new Socket(host, port);
        BufferedReader reader = new BufferedReader(new InputStreamReader(rawSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(rawSocket.getOutputStream(), true);

        try {
            readResponse(reader, "220");
            sendCommand(writer, reader, "EHLO " + host, "250");

            if (useSsl) {
                // Iniciar STARTTLS si el puerto es 587 o similar
                sendCommand(writer, reader, "STARTTLS", "220");
                
                SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(rawSocket, host, port, true);
                sslSocket.startHandshake();

                reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                writer = new PrintWriter(sslSocket.getOutputStream(), true);

                // Después de STARTTLS hay que volver a saludar
                sendCommand(writer, reader, "EHLO " + host, "250");
            }

            // Autenticación obligatoria en Gmail
            if (useSsl || !password.isEmpty()) {
                authenticate(writer, reader);
            }

            sendCommand(writer, reader, "MAIL FROM:<" + user + ">", "250");
            sendCommand(writer, reader, "RCPT TO:<" + to + ">", "250");
            sendCommand(writer, reader, "DATA", "354");

            writer.println("From: " + user);
            writer.println("To: " + to);
            writer.println("Subject: " + subject);
            writer.println("");
            writer.println(body);

            sendCommand(writer, reader, ".", "250");
            sendCommand(writer, reader, "QUIT", "221");

        } finally {
            writer.close();
            reader.close();
            rawSocket.close();
        }
    }

    private void authenticate(PrintWriter writer, BufferedReader reader) throws IOException {
        sendCommand(writer, reader, "AUTH LOGIN", "334");
        sendCommand(writer, reader, Base64.getEncoder().encodeToString(user.getBytes()), "334");
        sendCommand(writer, reader, Base64.getEncoder().encodeToString(password.getBytes()), "235");
    }

    private void sendCommand(PrintWriter writer, BufferedReader reader, String command, String expectedCode) throws IOException {
        System.out.println("C: " + command);
        writer.println(command);
        readResponse(reader, expectedCode);
    }

    private void readResponse(BufferedReader reader, String expectedCode) throws IOException {
        String line;
        // Leer líneas de respuesta (pueden ser múltiples para EHLO)
        do {
            line = reader.readLine();
            System.out.println("S: " + line);
        } while (line != null && line.length() >= 4 && line.charAt(3) == '-');

        if (line == null || !line.startsWith(expectedCode)) {
            throw new IOException("Error SMTP. Esperado: " + expectedCode + ", Recibido: " + line);
        }
    }
}
