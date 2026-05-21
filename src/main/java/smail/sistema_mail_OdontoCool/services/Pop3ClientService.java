package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class Pop3ClientService {

    @Value("${mail.pop3.host}")
    private String host;

    @Value("${mail.pop3.port}")
    private int port;

    @Value("${mail.pop3.user}")
    private String user;

    @Value("${mail.pop3.password}")
    private String password;

    @Value("${mail.pop3.ssl:false}")
    private boolean useSsl;

    public static class EmailMessage {
        public String from;
        public String subject;
        public String body;
        public int id;

        @Override
        public String toString() {
            return "From: " + from + " | Subject: " + subject;
        }
    }

    private Socket createSocket() throws IOException {
        if (useSsl) {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            return factory.createSocket(host, port);
        } else {
            return new Socket(host, port);
        }
    }

    public List<EmailMessage> getEmails() throws IOException {
        List<EmailMessage> messages = new ArrayList<>();

        try (Socket socket = createSocket();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            readResponse(reader);

            sendCommand(writer, reader, "USER " + user);
            sendCommand(writer, reader, "PASS " + password);

            String stat = sendCommand(writer, reader, "STAT");
            int count = Integer.parseInt(stat.split(" ")[1]);

            for (int i = 1; i <= count; i++) {
                EmailMessage email = retrieveMessage(writer, reader, i);
                email.id = i;
                messages.add(email);
            }

            sendCommand(writer, reader, "QUIT");
        }
        return messages;
    }

    private EmailMessage retrieveMessage(PrintWriter writer, BufferedReader reader, int msgNum) throws IOException {
        writer.print("RETR " + msgNum + "\r\n");
        writer.flush();

        String response = reader.readLine();

        if (response == null || !response.startsWith("+OK")) {
            throw new IOException("Error al recuperar mensaje: " + response);
        }

        EmailMessage email = new EmailMessage();

        List<String> rawHeaderLines = new ArrayList<>();
        StringBuilder bodyBuilder = new StringBuilder();

        boolean isBody = false;
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.equals(".")) {
                break;
            }

            // POP3 puede enviar líneas escapadas con doble punto
            if (line.startsWith("..")) {
                line = line.substring(1);
            }

            if (!isBody) {
                if (line.isEmpty()) {
                    isBody = true;
                } else {
                    rawHeaderLines.add(line);
                }
            } else {
                bodyBuilder.append(line).append("\n");
            }
        }

        Map<String, String> headers = parseHeaders(rawHeaderLines);

        String rawFrom = headers.getOrDefault("from", "");
        email.from = extractEmail(rawFrom);

        email.subject = headers.getOrDefault("subject", "");
        email.body = bodyBuilder.toString();

        return email;
    }

    private Map<String, String> parseHeaders(List<String> rawHeaderLines) {
        Map<String, String> headers = new HashMap<>();

        String currentHeaderName = null;
        StringBuilder currentHeaderValue = new StringBuilder();

        for (String line : rawHeaderLines) {

            // Si empieza con espacio o tab, es continuación del header anterior
            if (line.startsWith(" ") || line.startsWith("\t")) {
                if (currentHeaderName != null) {
                    currentHeaderValue.append(" ").append(line.trim());
                }
            } else {
                // Guardar el header anterior
                if (currentHeaderName != null) {
                    headers.put(
                            currentHeaderName.toLowerCase(),
                            currentHeaderValue.toString().trim());
                }

                int separatorIndex = line.indexOf(":");

                if (separatorIndex != -1) {
                    currentHeaderName = line.substring(0, separatorIndex).trim();
                    currentHeaderValue = new StringBuilder(
                            line.substring(separatorIndex + 1).trim());
                }
            }
        }

        // Guardar el último header
        if (currentHeaderName != null) {
            headers.put(
                    currentHeaderName.toLowerCase(),
                    currentHeaderValue.toString().trim());
        }

        return headers;
    }

    private String extractEmail(String rawFrom) {
        if (rawFrom == null || rawFrom.isBlank()) {
            return "";
        }

        if (rawFrom.contains("<") && rawFrom.contains(">")) {
            return rawFrom.substring(
                    rawFrom.indexOf("<") + 1,
                    rawFrom.indexOf(">")).trim();
        }

        return rawFrom.trim();
    }

    private String sendCommand(PrintWriter writer, BufferedReader reader, String command) throws IOException {
        writer.println(command);
        return readResponse(reader);
    }

    private String readResponse(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null || !line.startsWith("+OK")) {
            throw new IOException("Error POP3: " + line);
        }
        return line;
    }

    public void deleteMessage(int msgNum) throws IOException {
        try (Socket socket = createSocket();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            readResponse(reader);
            sendCommand(writer, reader, "USER " + user);
            sendCommand(writer, reader, "PASS " + password);
            sendCommand(writer, reader, "DELE " + msgNum);
            sendCommand(writer, reader, "QUIT");
        }
    }
}
