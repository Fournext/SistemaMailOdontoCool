package smail.sistema_mail_OdontoCool.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.HistorialClinico;
import smail.sistema_mail_OdontoCool.entities.Paciente;
import smail.sistema_mail_OdontoCool.repositories.HistorialClinicoRepository;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;
import smail.sistema_mail_OdontoCool.validations.HistorialClinicoVal;

@Service
public class HistorialClinicoService {
    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private HistorialClinicoVal historialClinicoVal;

    public void handle(String action, List<String> params, String fromEmail) {
        switch (action) {
            case "INS":
                insert(params, fromEmail);
                break;
            case "LIS":
                list(params, fromEmail);
                break;
            case "MOD":
                update(params, fromEmail);
                break;
            case "DEL":
                delete(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Historiales Clínicos.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parametros: CI[0], Alergias[1], AntecendentesMedicos[2],
            // EnfermedadesBase[3], motivoApertura[4], ObservacionesGenerales[5],
            if (params.size() < 6) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Historial Clínico. Se requieren 6.");
                return;
            }

            String validationMsg = historialClinicoVal.insertValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Paciente pac = pacienteRepository.findById(params.get(0)).orElse(null);
            if (pac == null) {
                sendResponse(fromEmail, "Error", "Paciente no encontrado con CI: " + params.get(0));
                return;
            }

            // Sacar las iniciales de los apellidos del paciente para formar el codigo del
            // historial
            String firstLetter = "";
            if (pac.getPersona().getApellidos() != null && !pac.getPersona().getApellidos().trim().isEmpty()) {
                String[] parts = pac.getPersona().getApellidos().trim().split("\\s+");
                StringBuilder sb = new StringBuilder();
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        sb.append(part.charAt(0));
                    }
                }
                firstLetter = sb.toString().toUpperCase();
            }

            HistorialClinico historial = new HistorialClinico();
            historial.setCodigoHistorial(params.get(0) + firstLetter);
            historial.setPaciente(pac);
            historial.setAlergias(params.get(1));
            historial.setAntencedentesMedicos(params.get(2));
            historial.setEnfermedadesBase(params.get(3));
            historial.setMotivoApertura(params.get(4));
            historial.setObservacionesGenerales(params.get(5));

            historialClinicoRepository.save(historial);
            sendResponse(fromEmail, "Éxito", "Historial Clínico registrado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar Historial Clínico: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            StringBuilder sb = new StringBuilder();
            if (params.size() == 0) {
                sendResponse(fromEmail, "Error",
                        "Falta especificar tipo de listado. Verifique el formato de comandos en la ayuda (HELP).");
                return;
            }
            if (params.size() == 1) {

                switch (params.get(0)) {
                    case "*":
                        sb = listAll();
                        break;
                    default:
                        sendResponse(fromEmail, "Error", "Listado no permitido para Historiales Clínicos.");
                }

            }
            sendResponse(fromEmail, "Listado de Historiales Clínicos", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar Historiales Clínicos: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<HistorialClinico> lista = historialClinicoRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Historiales Clínicos:\n\n");

        String separator = "+" 
            + "-".repeat(16) + "+" 
            + "-".repeat(13) + "+" 
            + "-".repeat(22) + "+" 
            + "-".repeat(17) + "+" 
            + "-".repeat(17) + "+" 
            + "-".repeat(17) + "+" 
            + "-".repeat(13) + "+" 
            + "-".repeat(13) + "+" 
            + "-".repeat(27) + "+\n";

        String header = String.format("| %s | %s | %s | %s | %s | %s | %s | %s | %s |\n",
            pad("Cod. Historial", 14),
            pad("Paciente CI", 11),
            pad("Motivo Apertura", 20),
            pad("Alergias", 15),
            pad("Antecedentes", 15),
            pad("Enf. Base", 15),
            pad("F. Apertura", 11),
            pad("F. Act.", 11),
            pad("Obs. Generales", 25)
        );

        sb.append(separator).append(header).append(separator);

        for (HistorialClinico h : lista) {
            String row = String.format("| %s | %s | %s | %s | %s | %s | %s | %s | %s |\n",
                pad(h.getCodigoHistorial(), 14),
                pad(h.getPaciente() != null ? h.getPaciente().getCi() : "N/A", 11),
                pad(h.getMotivoApertura(), 20),
                pad(h.getAlergias(), 15),
                pad(h.getAntencedentesMedicos(), 15),
                pad(h.getEnfermedadesBase(), 15),
                pad(h.getFechaApertura() != null ? h.getFechaApertura().toString() : "N/A", 11),
                pad(h.getFechaActualizacion() != null ? h.getFechaActualizacion().toString() : "N/A", 11),
                pad(h.getObservacionesGenerales(), 25)
            );
            sb.append(row);
        }

        sb.append(separator);
        return sb;
    }

    private String pad(String val, int width) {
        if (val == null) val = "";
        val = val.trim();
        if (val.length() > width) {
            return val.substring(0, width - 3) + "...";
        }
        return String.format("%-" + width + "s", val);
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // Parámetros: codigoHistorial[0], Alergias[1], AntecedentesMedicos[2],
            // EnfermedadesBase[3], MotivoApertura[4], ObservacionesGenerales[5]
            if (params.size() < 6) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para modificar Historial Clínico. Se requieren 6.");
                return;
            }

            String validationMsg = historialClinicoVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            String codigoHistorial = params.get(0);
            HistorialClinico historial = historialClinicoRepository.findById(codigoHistorial).orElse(null);
            if (historial == null) {
                sendResponse(fromEmail, "Error", "Historial Clínico no encontrado.");
                return;
            }

            String alergias = params.get(1);
            String antecedentesMedicos = params.get(2);
            String enfermedadesBase = params.get(3);
            String motivoApertura = params.get(4);
            String observacionesGenerales = params.get(5);

            if (alergias != null && !alergias.trim().isEmpty()) {
                historial.setAlergias(alergias);
            }
            if (antecedentesMedicos != null && !antecedentesMedicos.trim().isEmpty()) {
                historial.setAntencedentesMedicos(antecedentesMedicos);
            }
            if (enfermedadesBase != null && !enfermedadesBase.trim().isEmpty()) {
                historial.setEnfermedadesBase(enfermedadesBase);
            }
            if (motivoApertura != null && !motivoApertura.trim().isEmpty()) {
                historial.setMotivoApertura(motivoApertura);
            }
            if (observacionesGenerales != null && !observacionesGenerales.trim().isEmpty()) {
                historial.setObservacionesGenerales(observacionesGenerales);
            }

            historialClinicoRepository.save(historial);
            sendResponse(fromEmail, "Éxito", "Historial Clínico modificado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al modificar Historial Clínico: " + e.getMessage());
        }
    }

    @Transactional
    private void delete(List<String> params, String fromEmail) {
        try {
            if (params.size() < 1) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Historial Clínico. Se requiere 1.");
                return;
            }
            String codigoHistorial = params.get(0);
            HistorialClinico historial = historialClinicoRepository.findById(codigoHistorial).orElse(null);
            if (historial == null) {
                sendResponse(fromEmail, "Error", "Historial Clínico no encontrado.");
                return;
            }
            historialClinicoRepository.delete(historial);
            sendResponse(fromEmail, "Éxito", "Historial Clínico eliminado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar Historial Clínico: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (Exception e) {
            System.err.println("Error al enviar respuesta: " + e.getMessage());
        }
    }

}
