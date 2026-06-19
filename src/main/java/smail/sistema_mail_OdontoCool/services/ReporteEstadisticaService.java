package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.*;
import smail.sistema_mail_OdontoCool.repositories.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReporteEstadisticaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ServicioPrestadoRepository servicioPrestadoRepository;

    @Autowired
    private BoletaPagoRepository boletaPagoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SmtpClientService smtpService;

    public void handle(String action, List<String> params, String fromEmail) {
        if ("LIS".equals(action)) {
            generateAndSendReport(fromEmail, params);
        } else {
            sendResponse(fromEmail, "Error", "Acción no soportada para Reportes y Estadísticas.");
        }
    }

    private void generateAndSendReport(String fromEmail, List<String> params) {
        try {
            String option = (params != null && !params.isEmpty()) ? params.get(0).trim().toUpperCase() : "*";

            boolean showCitas = option.equals("*") || option.equals("ALL") || option.equals("CITAS")
                    || option.equals("DOCTOR") || option.equals("DOCTORES");
            boolean showServicios = option.equals("*") || option.equals("ALL") || option.equals("SERVICIOS")
                    || option.equals("SERVICIO");
            boolean showIngresos = option.equals("*") || option.equals("ALL") || option.equals("INGRESOS")
                    || option.equals("INGRESO");

            if (!showCitas && !showServicios && !showIngresos) {
                sendResponse(fromEmail, "Error",
                        "Parámetro de reporte no reconocido. Use: LISREP[\"*\"], LISREP[\"CITAS\"], LISREP[\"SERVICIOS\"] o LISREP[\"INGRESOS\"].");
                return;
            }

            // 1. Obtener Datos de la base de datos
            List<Cita> citas = citaRepository.findAllWithAsignaciones();
            List<ServicioPrestado> serviciosPrestados = servicioPrestadoRepository.findAll();
            List<BoletaPago> boletas = boletaPagoRepository.findAll();
            long totalPacientes = pacienteRepository.count();
            long totalDoctores = doctorRepository.count();

            // 2. Procesar estadísticas
            Map<String, Long> citasPorDoctor = new HashMap<>();
            if (showCitas) {
                for (Cita c : citas) {
                    if (!"ELIMINADA".equalsIgnoreCase(getEstadoActual(c))) {
                        String docName = c.getDoctor() != null && c.getDoctor().getPersona() != null
                                ? c.getDoctor().getPersona().getNombres() + " "
                                        + c.getDoctor().getPersona().getApellidos()
                                : "Desconocido";
                        citasPorDoctor.put(docName, citasPorDoctor.getOrDefault(docName, 0L) + 1);
                    }
                }
            }

            Map<String, Long> serviciosFrecuentes = new HashMap<>();
            if (showServicios) {
                for (ServicioPrestado sp : serviciosPrestados) {
                    if (sp.getServicio() != null) {
                        String srvName = sp.getServicio().getNombre();
                        serviciosFrecuentes.put(srvName,
                                serviciosFrecuentes.getOrDefault(srvName, 0L) + sp.getCantidad());
                    }
                }
            }

            Map<String, Double> ingresosPorMes = new HashMap<>();
            if (showIngresos) {
                DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
                for (BoletaPago bp : boletas) {
                    if (bp.getFechaEmicion() != null && !"ELIMINADO".equalsIgnoreCase(bp.getEstadoPago())) {
                        String monthStr = bp.getFechaEmicion().format(yearMonthFormatter);
                        ingresosPorMes.put(monthStr, ingresosPorMes.getOrDefault(monthStr, 0.0) + bp.getTotal());
                    }
                }
            }

            // 3. Generar las imágenes (Gráficos) y adjuntos
            List<String> attachmentsList = new ArrayList<>();

            // 4. Construir cuerpo del correo (Vertical / Columna)
            StringBuilder sb = new StringBuilder();
            sb.append("========================================\n");
            sb.append("REPORTE Y ESTADÍSTICAS - ODONTOCOOL\n");
            sb.append("========================================\n\n");

            sb.append("RESUMEN GENERAL:\n");
            sb.append("* Total Pacientes Registrados: ").append(totalPacientes).append("\n");
            sb.append("* Total Médicos Registrados: ").append(totalDoctores).append("\n");
            sb.append("* Total Citas Activas: ")
                    .append(citas.stream().filter(c -> !"ELIMINADA".equalsIgnoreCase(getEstadoActual(c))).count())
                    .append("\n");
            sb.append("* Total Ingresos Consolidados: ")
                    .append(String.format("%.2f",
                            boletas.stream().filter(b -> !"ELIMINADO".equalsIgnoreCase(b.getEstadoPago()))
                                    .mapToDouble(BoletaPago::getTotal).sum()))
                    .append(" Bs.\n\n");

            if (showCitas) {
                String chart1Base64 = generateBarChart("Citas por Doctor", citasPorDoctor, "Citas");
                attachmentsList.add(chart1Base64);

                sb.append("DETALLE DE CITAS POR DOCTOR:\n");
                if (citasPorDoctor.isEmpty()) {
                    sb.append("  * No hay citas registradas.\n");
                } else {
                    citasPorDoctor.forEach((doc, count) -> sb.append("  * ").append(doc).append(": ").append(count)
                            .append(" cita(s)\n"));
                }
                sb.append("\n");
            }

            if (showServicios) {
                String chart2Base64 = generatePieChart("Servicios más Solicitados", serviciosFrecuentes);
                attachmentsList.add(chart2Base64);

                sb.append("DETALLE DE SERVICIOS PRESTADOS:\n");
                if (serviciosFrecuentes.isEmpty()) {
                    sb.append("  * No hay servicios prestados registrados.\n");
                } else {
                    serviciosFrecuentes.forEach((srv, count) -> sb.append("  * ").append(srv).append(": ").append(count)
                            .append(" vez/veces solicitado\n"));
                }
                sb.append("\n");
            }

            if (showIngresos) {
                String chart3Base64 = generateLineChart("Evolución de Ingresos Mensuales", ingresosPorMes);
                attachmentsList.add(chart3Base64);

                sb.append("DETALLE DE INGRESOS MENSUALES:\n");
                if (ingresosPorMes.isEmpty()) {
                    sb.append("  * No hay ingresos registrados.\n");
                } else {
                    List<String> sortedMonths = new ArrayList<>(ingresosPorMes.keySet());
                    Collections.sort(sortedMonths);
                    for (String m : sortedMonths) {
                        sb.append("  * Mes ").append(m).append(": ")
                                .append(String.format("%.2f", ingresosPorMes.get(m))).append(" Bs.\n");
                    }
                }
                sb.append("\n");
            }

            sb.append("Se han adjuntado los gráficos correspondientes a este reporte.\n");

            String[] attachments = attachmentsList.toArray(new String[0]);

            // 5. Enviar respuesta por correo
            smtpService.sendEmail(fromEmail, "Reporte y Estadísticas - OdontoCool", sb.toString(), attachments);

        } catch (Exception e) {
            sendResponse(fromEmail, "Error de Reportes", "No se pudo generar el reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private AsignacionEstadoCita getUltimaAsignacion(Cita cita) {
        if (cita.getAsignacionesEstadoCita() == null || cita.getAsignacionesEstadoCita().isEmpty()) {
            return null;
        }
        AsignacionEstadoCita latest = null;
        for (AsignacionEstadoCita a : cita.getAsignacionesEstadoCita()) {
            if (latest == null || a.getFechaCambio().isAfter(latest.getFechaCambio())) {
                latest = a;
            }
        }
        return latest;
    }

    private String getEstadoActual(Cita cita) {
        AsignacionEstadoCita latest = getUltimaAsignacion(cita);
        return (latest != null && latest.getEstadoCita() != null) ? latest.getEstadoCita().getNombre() : "PENDIENTE";
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en ReporteEstadisticaService: " + e.getMessage());
        }
    }

    // --- GENERACIÓN DE GRÁFICOS CON AWT ---

    private String generateBarChart(String title, Map<String, Long> data, String yAxisLabel) {
        int width = 600;
        int height = 400;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fondo Blanco
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        int padding = 50;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding - 30;
        int startX = padding + 20;
        int startY = padding + 30;

        // Título
        g2.setColor(new Color(44, 62, 80));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString(title, padding, padding);

        if (data == null || data.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
            g2.drawString("No hay datos disponibles", width / 2 - 80, height / 2);
            g2.dispose();
            return toBase64(image);
        }

        double maxValue = data.values().stream().mapToDouble(Long::doubleValue).max().orElse(1.0);
        if (maxValue == 0)
            maxValue = 1.0;

        // Líneas de cuadrícula
        g2.setColor(new Color(230, 230, 230));
        g2.setStroke(new BasicStroke(1));
        int gridLines = (maxValue <= 10) ? (int) maxValue : 5;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 0; i <= gridLines; i++) {
            double val = (maxValue / gridLines) * i;
            int y = startY + chartHeight - (int) ((val / maxValue) * chartHeight);
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(startX, y, startX + chartWidth - 20, y);
            g2.setColor(new Color(127, 140, 141));
            g2.drawString(String.format("%.0f", val), padding - 15, y + 4);
        }

        // Ejes
        g2.setColor(new Color(189, 195, 199));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(startX, startY + chartHeight, startX + chartWidth - 20, startY + chartHeight);
        g2.drawLine(startX, startY, startX, startY + chartHeight);

        // Dibujar Barras
        int numBars = data.size();
        int barGap = 15;
        int totalGapsWidth = barGap * (numBars + 1);
        int barWidth = (chartWidth - 20 - totalGapsWidth) / numBars;
        if (barWidth < 5)
            barWidth = 5;

        Color[] colors = {
                new Color(77, 150, 255),
                new Color(107, 203, 119),
                new Color(255, 107, 107),
                new Color(185, 131, 255),
                new Color(255, 217, 61),
                new Color(255, 159, 67),
                new Color(72, 219, 251)
        };

        int idx = 0;
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            String label = entry.getKey();
            double value = entry.getValue().doubleValue();

            int barHeight = (int) ((value / maxValue) * chartHeight);
            int x = startX + barGap + idx * (barWidth + barGap);
            int y = startY + chartHeight - barHeight;

            g2.setColor(colors[idx % colors.length]);
            g2.fillRect(x, y, barWidth, barHeight);
            g2.setColor(colors[idx % colors.length].darker());
            g2.drawRect(x, y, barWidth, barHeight);

            // Valor sobre la barra
            g2.setColor(new Color(44, 62, 80));
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            String valStr = String.format("%.0f", value);
            int valWidth = g2.getFontMetrics().stringWidth(valStr);
            g2.drawString(valStr, x + (barWidth - valWidth) / 2, y - 5);

            // Etiqueta abajo
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(44, 62, 80));
            String truncatedLabel = label.length() > 10 ? label.substring(0, 8) + ".." : label;
            int lblWidth = g2.getFontMetrics().stringWidth(truncatedLabel);
            g2.drawString(truncatedLabel, x + (barWidth - lblWidth) / 2, startY + chartHeight + 15);

            idx++;
        }

        g2.dispose();
        return toBase64(image);
    }

    private String generatePieChart(String title, Map<String, Long> data) {
        int width = 600;
        int height = 400;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        int padding = 50;

        g2.setColor(new Color(44, 62, 80));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString(title, padding, padding);

        if (data == null || data.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
            g2.drawString("No hay datos disponibles", width / 2 - 80, height / 2);
            g2.dispose();
            return toBase64(image);
        }

        double total = data.values().stream().mapToDouble(Long::doubleValue).sum();

        int pieSize = 200;
        int pieX = padding;
        int pieY = padding + 50;

        Color[] colors = {
                new Color(77, 150, 255),
                new Color(107, 203, 119),
                new Color(255, 107, 107),
                new Color(185, 131, 255),
                new Color(255, 217, 61),
                new Color(255, 159, 67),
                new Color(72, 219, 251)
        };

        double currentAngle = 0;
        int idx = 0;

        int legendX = pieX + pieSize + 40;
        int legendY = pieY + 10;

        for (Map.Entry<String, Long> entry : data.entrySet()) {
            String label = entry.getKey();
            double value = entry.getValue().doubleValue();
            double angle = (value / total) * 360;

            g2.setColor(colors[idx % colors.length]);
            g2.fillArc(pieX, pieY, pieSize, pieSize, (int) currentAngle, (int) Math.ceil(angle));
            g2.setColor(Color.WHITE);
            g2.drawArc(pieX, pieY, pieSize, pieSize, (int) currentAngle, (int) Math.ceil(angle));

            currentAngle += angle;

            g2.setColor(colors[idx % colors.length]);
            g2.fillRect(legendX, legendY + idx * 25, 15, 15);

            g2.setColor(new Color(44, 62, 80));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            String legendText = String.format("%s (%.0f - %.1f%%)", label, value, (value / total) * 100);
            g2.drawString(legendText, legendX + 25, legendY + idx * 25 + 12);

            idx++;
        }

        g2.dispose();
        return toBase64(image);
    }

    private String generateLineChart(String title, Map<String, Double> data) {
        int width = 600;
        int height = 400;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        int padding = 50;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding - 30;
        int startX = padding + 30;
        int startY = padding + 30;

        g2.setColor(new Color(44, 62, 80));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString(title, padding, padding);

        if (data == null || data.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
            g2.drawString("No hay datos disponibles", width / 2 - 80, height / 2);
            g2.dispose();
            return toBase64(image);
        }

        List<String> sortedKeys = new ArrayList<>(data.keySet());
        Collections.sort(sortedKeys);

        double maxValue = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (maxValue == 0)
            maxValue = 1.0;

        // Cuadrícula e Y-labels
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            double val = (maxValue / gridLines) * i;
            int y = startY + chartHeight - (int) ((val / maxValue) * chartHeight);
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(startX, y, startX + chartWidth - 20, y);
            g2.setColor(new Color(127, 140, 141));
            g2.drawString(String.format("%.0f Bs", val), padding - 15, y + 4);
        }

        // Ejes
        g2.setColor(new Color(189, 195, 199));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(startX, startY + chartHeight, startX + chartWidth - 20, startY + chartHeight);
        g2.drawLine(startX, startY, startX, startY + chartHeight);

        int numPoints = sortedKeys.size();
        int stepX = numPoints > 1 ? (chartWidth - 40) / (numPoints - 1) : chartWidth - 40;

        int[] xCoords = new int[numPoints];
        int[] yCoords = new int[numPoints];

        for (int i = 0; i < numPoints; i++) {
            String key = sortedKeys.get(i);
            double val = data.get(key);
            xCoords[i] = startX + 10 + i * stepX;
            yCoords[i] = startY + chartHeight - (int) ((val / maxValue) * chartHeight);
        }

        // Dibujar línea conectora
        g2.setColor(new Color(77, 150, 255));
        g2.setStroke(new BasicStroke(3));
        for (int i = 0; i < numPoints - 1; i++) {
            g2.drawLine(xCoords[i], yCoords[i], xCoords[i + 1], yCoords[i + 1]);
        }

        // Dibujar puntos y etiquetas
        for (int i = 0; i < numPoints; i++) {
            String key = sortedKeys.get(i);
            double valActual = data.get(key);

            g2.setColor(new Color(255, 107, 107));
            g2.fillOval(xCoords[i] - 5, yCoords[i] - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawOval(xCoords[i] - 5, yCoords[i] - 5, 10, 10);

            g2.setColor(new Color(44, 62, 80));
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            String valStr = String.format("%.0f", valActual);
            int valWidth = g2.getFontMetrics().stringWidth(valStr);
            g2.drawString(valStr, xCoords[i] - valWidth / 2, yCoords[i] - 10);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            int lblWidth = g2.getFontMetrics().stringWidth(key);
            g2.drawString(key, xCoords[i] - lblWidth / 2, startY + chartHeight + 20);
        }

        g2.dispose();
        return toBase64(image);
    }

    private String toBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            System.err.println("Error encoding image: " + e.getMessage());
            return "";
        }
    }
}
