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
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        public List<String> imagenesBase64 = new ArrayList<>();

        @Override
        public String toString() {
            return "From: " + from + " | Subject: " + subject + " | Images: " + imagenesBase64.size();
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
        email.from = extractEmail(decodeRFC2047(rawFrom));

        email.subject = decodeRFC2047(headers.getOrDefault("subject", ""));
        email.body = bodyBuilder.toString();
        email.imagenesBase64 = extractImages(headers, email.body);

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

    private List<String> extractImages(Map<String, String> headers, String body) {
        List<String> images = new ArrayList<>();
        if (body == null || body.isEmpty()) {
            return images;
        }

        String mainContentType = headers.getOrDefault("content-type", "");
        String mainEncoding = headers.getOrDefault("content-transfer-encoding", "");

        if (mainContentType.toLowerCase().contains("image/") && mainEncoding.toLowerCase().contains("base64")) {
            String cleanBase64 = body.replaceAll("[\\s\\r\\n]+", "");
            if (!cleanBase64.isEmpty()) {
                images.add(cleanBase64);
            }
        }

        String boundary = extractBoundary(mainContentType);
        if (boundary != null) {
            String[] parts = body.split("--" + Pattern.quote(boundary));
            for (String part : parts) {
                String normalizedPart = part.replace("\r\n", "\n");
                int doubleNewlineIndex = normalizedPart.indexOf("\n\n");
                if (doubleNewlineIndex != -1) {
                    String partHeadersRaw = normalizedPart.substring(0, doubleNewlineIndex);
                    String partBody = normalizedPart.substring(doubleNewlineIndex + 2);

                    Map<String, String> partHeaders = new HashMap<>();
                    String[] lines = partHeadersRaw.split("\n");
                    String currentHeaderName = null;
                    StringBuilder currentHeaderValue = new StringBuilder();
                    for (String line : lines) {
                        if (line.startsWith(" ") || line.startsWith("\t")) {
                            if (currentHeaderName != null) {
                                currentHeaderValue.append(" ").append(line.trim());
                            }
                        } else {
                            if (currentHeaderName != null) {
                                partHeaders.put(currentHeaderName.toLowerCase(), currentHeaderValue.toString().trim());
                            }
                            int sep = line.indexOf(":");
                            if (sep != -1) {
                                currentHeaderName = line.substring(0, sep).trim();
                                currentHeaderValue = new StringBuilder(line.substring(sep + 1).trim());
                            } else {
                                currentHeaderName = null;
                            }
                        }
                    }
                    if (currentHeaderName != null) {
                        partHeaders.put(currentHeaderName.toLowerCase(), currentHeaderValue.toString().trim());
                    }

                    String partContentType = partHeaders.getOrDefault("content-type", "");
                    String partEncoding = partHeaders.getOrDefault("content-transfer-encoding", "");

                    if (partContentType.toLowerCase().contains("image/") && partEncoding.toLowerCase().contains("base64")) {
                        String cleanBase64 = partBody.replaceAll("[\\s\\r\\n]+", "");
                        if (cleanBase64.endsWith("--")) {
                            cleanBase64 = cleanBase64.substring(0, cleanBase64.length() - 2);
                        }
                        if (!cleanBase64.isEmpty()) {
                            images.add(cleanBase64);
                        }
                    }
                }
            }
        }

        // También extraer imágenes base64 incrustadas en formato data:image/...
        Pattern inlineImagePattern = Pattern.compile("data:image/[a-zA-Z0-9.-]+;base64,([a-zA-Z0-9+/\\s\\r\\n=]+)");
        Matcher matcher = inlineImagePattern.matcher(body);
        while (matcher.find()) {
            String rawBase64 = matcher.group(1);
            String cleanBase64 = rawBase64.replaceAll("[\\s\\r\\n]+", "");
            if (!cleanBase64.isEmpty()) {
                images.add(cleanBase64);
            }
        }

        return images;
    }

    private String extractBoundary(String contentType) {
        if (contentType == null) return null;
        int boundaryIndex = contentType.toLowerCase().indexOf("boundary=");
        if (boundaryIndex == -1) return null;
        String boundary = contentType.substring(boundaryIndex + 9).trim();
        if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
            boundary = boundary.substring(1, boundary.length() - 1);
        } else if (boundary.startsWith("'") && boundary.endsWith("'")) {
            boundary = boundary.substring(1, boundary.length() - 1);
        }
        int semiColonIndex = boundary.indexOf(";");
        if (semiColonIndex != -1) {
            boundary = boundary.substring(0, semiColonIndex).trim();
        }
        return boundary;
    }

    private String decodeRFC2047(String text) {
        if (text == null || !text.contains("=?")) {
            return text;
        }

        Pattern pattern = Pattern.compile("=\\?([^?]+)\\?([BbQq])\\?([^?]*)\\?=");
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        ByteArrayOutputStream accumulatedBytes = new ByteArrayOutputStream();
        String currentCharset = null;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String prefix = text.substring(lastEnd, start);

            if (!prefix.isEmpty()) {
                if (prefix.trim().isEmpty() && currentCharset != null) {
                    // Ignorar espacios en blanco entre palabras codificadas (RFC 2047)
                } else {
                    if (accumulatedBytes.size() > 0) {
                        try {
                            sb.append(accumulatedBytes.toString(currentCharset));
                        } catch (Exception e) {
                            sb.append(accumulatedBytes.toString());
                        }
                        accumulatedBytes.reset();
                        currentCharset = null;
                    }
                    sb.append(prefix);
                }
            }

            String charset = matcher.group(1);
            String encoding = matcher.group(2).toUpperCase();
            String encodedText = matcher.group(3);

            if (currentCharset == null) {
                currentCharset = charset;
            }

            try {
                byte[] decodedBytes;
                if ("B".equals(encoding)) {
                    decodedBytes = Base64.getDecoder().decode(encodedText.trim());
                } else if ("Q".equals(encoding)) {
                    decodedBytes = decodeQuotedPrintable(encodedText);
                } else {
                    decodedBytes = matcher.group(0).getBytes();
                }
                accumulatedBytes.write(decodedBytes);
            } catch (Exception e) {
                try {
                    accumulatedBytes.write(matcher.group(0).getBytes());
                } catch (Exception ignored) {}
            }

            lastEnd = end;
        }

        if (accumulatedBytes.size() > 0) {
            try {
                sb.append(accumulatedBytes.toString(currentCharset));
            } catch (Exception e) {
                sb.append(accumulatedBytes.toString());
            }
        }

        if (lastEnd < text.length()) {
            sb.append(text.substring(lastEnd));
        }

        return sb.toString();
    }

    private byte[] decodeQuotedPrintable(String text) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '=') {
                if (i + 2 < text.length()) {
                    String hex = text.substring(i + 1, i + 3);
                    try {
                        out.write(Integer.parseInt(hex, 16));
                    } catch (NumberFormatException e) {
                        out.write('=');
                        out.write(text.charAt(i + 1));
                        out.write(text.charAt(i + 2));
                    }
                    i += 2;
                } else {
                    out.write('=');
                }
            } else if (c == '_') {
                out.write(' ');
            } else {
                out.write(c);
            }
        }
        return out.toByteArray();
    }
}
