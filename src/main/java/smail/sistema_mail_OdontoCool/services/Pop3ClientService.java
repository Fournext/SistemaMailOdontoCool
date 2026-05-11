package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
        writer.println("RETR " + msgNum);
        String response = reader.readLine();
        if (!response.startsWith("+OK")) {
            throw new IOException("Error al recuperar mensaje: " + response);
        }

        EmailMessage email = new EmailMessage();
        StringBuilder bodyBuilder = new StringBuilder();
        boolean isBody = false;
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.equals(".")) break;

            if (!isBody) {
                if (line.isEmpty()) {
                    isBody = true;
                } else if (line.toLowerCase().startsWith("from: ")) {
                    String rawFrom = line.substring(6).trim();
                    if (rawFrom.contains("<") && rawFrom.contains(">")) {
                        email.from = rawFrom.substring(rawFrom.indexOf("<") + 1, rawFrom.indexOf(">"));
                    } else {
                        email.from = rawFrom;
                    }
                } else if (line.toLowerCase().startsWith("subject: ")) {
                    email.subject = line.substring(9).trim();
                }
            } else {
                bodyBuilder.append(line).append("\n");
            }
        }
        email.body = bodyBuilder.toString();
        return email;
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
