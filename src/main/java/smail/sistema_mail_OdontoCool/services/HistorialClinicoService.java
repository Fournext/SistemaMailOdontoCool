package smail.sistema_mail_OdontoCool.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.entities.Diagnostico;
import smail.sistema_mail_OdontoCool.entities.HistorialClinico;
import smail.sistema_mail_OdontoCool.entities.Paciente;
import smail.sistema_mail_OdontoCool.entities.Tratamiento;
import smail.sistema_mail_OdontoCool.repositories.HistorialClinicoRepository;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;
import smail.sistema_mail_OdontoCool.validations.HistorialClinicoVal;

@Service
@Transactional
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

            String term = params.get(0).trim();
            if ("*".equals(term)) {
                List<HistorialClinico> lista = historialClinicoRepository.findAll();
                if (lista.isEmpty()) {
                    sb.append("No se encontraron Historiales Clínicos registrados.");
                } else if (lista.size() == 1) {
                    sb = formatSingleHistorial(lista.get(0));
                } else {
                    sb = listMultiple(lista);
                }
            } else {
                HistorialClinico h = historialClinicoRepository.findByCodigoHistorial(term);
                if (h != null) {
                    sb = formatSingleHistorial(h);
                } else {
                    sb.append("No se encontró ningún Historial Clínico con el código: ").append(term);
                }
            }
            sendResponse(fromEmail, "Listado de Historiales Clínicos", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar Historiales Clínicos: " + e.getMessage());
        }
    }

    private StringBuilder formatSingleHistorial(HistorialClinico h) {
        StringBuilder sb = new StringBuilder();
        sb.append("Detalle de Historial Clínico:\n\n");
        sb.append("* Código Historial: ").append(h.getCodigoHistorial()).append("\n");
        sb.append("* Paciente: ")
                .append(h.getPaciente() != null
                        ? h.getPaciente().getNombres() + " " + h.getPaciente().getApellidos() + " (CI: "
                                + h.getPaciente().getCi() + ")"
                        : "N/A")
                .append("\n");
        sb.append("* Motivo Apertura: ").append(h.getMotivoApertura() != null ? h.getMotivoApertura() : "Ninguno")
                .append("\n");
        sb.append("* Alergias: ").append(h.getAlergias() != null ? h.getAlergias() : "Ninguna").append("\n");
        sb.append("* Antecedentes Médicos: ")
                .append(h.getAntencedentesMedicos() != null ? h.getAntencedentesMedicos() : "Ninguno").append("\n");
        sb.append("* Enfermedades de Base: ")
                .append(h.getEnfermedadesBase() != null ? h.getEnfermedadesBase() : "Ninguna").append("\n");
        sb.append("* Fecha Apertura: ").append(h.getFechaApertura() != null ? h.getFechaApertura().toString() : "N/A")
                .append("\n");
        sb.append("* Fecha Actualización: ")
                .append(h.getFechaActualizacion() != null ? h.getFechaActualizacion().toString() : "N/A").append("\n");
        sb.append("* Observaciones Generales: ")
                .append(h.getObservacionesGenerales() != null ? h.getObservacionesGenerales() : "Ninguna").append("\n");
        sb.append("* Estado: ").append(h.getEstado() != null ? h.getEstado() : "ACTIVO").append("\n\n");

        if (h.getTratamientos() == null || h.getTratamientos().isEmpty()) {
            sb.append("No tiene tratamientos registrados.\n\n");
        } else {
            sb.append("Tratamientos registrados:\n");
            for (Tratamiento t : h.getTratamientos()) {
                sb.append("  - Tratamiento ID: ").append(t.getId()).append("\n");
                sb.append("    * Objetivo: ")
                        .append(t.getObjetivoTratamiento() != null ? t.getObjetivoTratamiento() : "N/A").append("\n");
                sb.append("    * Observación: ").append(t.getObservacion() != null ? t.getObservacion() : "Ninguna")
                        .append("\n");
                sb.append("    * Estado: ").append(t.getEstado() != null ? t.getEstado() : "N/A").append("\n");
                sb.append("    * Fecha Inicio: ")
                        .append(t.getFechaInicio() != null ? t.getFechaInicio().toString() : "N/A").append("\n");
                sb.append("    * Fecha Fin Estimada: ")
                        .append(t.getFechaFin() != null ? t.getFechaFin().toString() : "N/A").append("\n");
                sb.append("    * Fecha Fin Real: ")
                        .append(t.getFechaFinReal() != null ? t.getFechaFinReal().toString() : "N/A").append("\n");

                Diagnostico d = t.getDiagnostico();
                if (d != null) {
                    sb.append("    * Diagnóstico asociado:\n");
                    sb.append("      - ID: ").append(d.getId()).append("\n");
                    sb.append("      - Síntomas: ").append(d.getSintomas() != null ? d.getSintomas() : "N/A")
                            .append("\n");
                    sb.append("      - Tipo: ").append(d.getTipoDiagnostico() != null ? d.getTipoDiagnostico() : "N/A")
                            .append("\n");
                    sb.append("      - Gravedad: ").append(d.getGravedad() != null ? d.getGravedad() : "N/A")
                            .append("\n");
                    sb.append("      - Observaciones: ")
                            .append(d.getObservaciones() != null ? d.getObservaciones() : "Ninguna").append("\n");
                } else {
                    sb.append("    * Diagnóstico asociado: Ninguno\n");
                }
                sb.append("\n");
            }
        }

        List<Diagnostico> diags = new java.util.ArrayList<>();
        if (h.getTratamientos() != null) {
            for (Tratamiento t : h.getTratamientos()) {
                if (t.getDiagnostico() != null) {
                    diags.add(t.getDiagnostico());
                }
            }
        }

        if (diags.isEmpty()) {
            sb.append("No tiene diagnósticos registrados.\n");
        } else {
            sb.append("Diagnósticos registrados:\n");
            for (Diagnostico d : diags) {
                sb.append("  - Diagnóstico ID: ").append(d.getId()).append("\n");
                sb.append("    * Síntomas: ").append(d.getSintomas() != null ? d.getSintomas() : "N/A").append("\n");
                sb.append("    * Tipo: ").append(d.getTipoDiagnostico() != null ? d.getTipoDiagnostico() : "N/A")
                        .append("\n");
                sb.append("    * Gravedad: ").append(d.getGravedad() != null ? d.getGravedad() : "N/A").append("\n");
                sb.append("    * Observaciones: ")
                        .append(d.getObservaciones() != null ? d.getObservaciones() : "Ninguna").append("\n");
                if (d.getCita() != null) {
                    sb.append("    * Cita ID: ").append(d.getCita().getIdCita()).append(" (Fecha: ")
                            .append(d.getCita().getFechaCita()).append(")\n");
                }
                sb.append("\n");
            }
        }
        return sb;
    }

    private StringBuilder listMultiple(List<HistorialClinico> lista) {
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
                pad("Obs. Generales", 25));

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
                    pad(h.getObservacionesGenerales(), 25));
            sb.append(row);
        }

        sb.append(separator);
        return sb;
    }

    private String pad(String val, int width) {
        if (val == null)
            val = "";
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
