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

    public void sendEmail(String to, String subject, String body, String... base64Image) throws IOException {
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

            writer.print("From: " + user + "\r\n");
            writer.print("To: " + to + "\r\n");
            writer.print("Subject: " + subject + "\r\n");
            
            String imageStr = (base64Image != null && base64Image.length > 0) ? base64Image[0] : null;

            if (imageStr != null && !imageStr.trim().isEmpty()) {
                String boundary = "OuterBoundary_" + System.currentTimeMillis();
                writer.print("MIME-Version: 1.0\r\n");
                writer.print("Content-Type: multipart/mixed; boundary=\"" + boundary + "\"\r\n");
                writer.print("\r\n");

                // Cuerpo de texto
                writer.print("--" + boundary + "\r\n");
                writer.print("Content-Type: text/plain; charset=UTF-8\r\n");
                writer.print("Content-Transfer-Encoding: 8bit\r\n");
                writer.print("\r\n");
                String safeBody = body.replace("\r\n", "\n").replace("\n", "\r\n");
                writer.print(safeBody + "\r\n");
                writer.print("\r\n");

                // Imagen adjunta
                String cleanBase64 = imageStr.trim();
                if (cleanBase64.startsWith("data:image")) {
                    int commaIndex = cleanBase64.indexOf(",");
                    if (commaIndex != -1) {
                        cleanBase64 = cleanBase64.substring(commaIndex + 1);
                    }
                }
                writer.print("--" + boundary + "\r\n");
                writer.print("Content-Type: image/png; name=\"qr.png\"\r\n");
                writer.print("Content-Transfer-Encoding: base64\r\n");
                writer.print("Content-Disposition: attachment; filename=\"qr.png\"\r\n");
                writer.print("\r\n");
                writer.print(cleanBase64 + "\r\n");
                writer.print("\r\n");

                writer.print("--" + boundary + "--\r\n");
            } else {
                writer.print("\r\n");
                String safeBody = body.replace("\r\n", "\n").replace("\n", "\r\n");
                writer.print(safeBody + "\r\n");
            }
            writer.flush();

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
        writer.print(command + "\r\n");
        writer.flush();
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
