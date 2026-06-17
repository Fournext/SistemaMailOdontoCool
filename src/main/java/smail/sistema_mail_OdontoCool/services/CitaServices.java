package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Cita;
import smail.sistema_mail_OdontoCool.entities.AsignacionEstadoCita;
import smail.sistema_mail_OdontoCool.entities.EstadoCita;
import smail.sistema_mail_OdontoCool.repositories.CitaRepository;
import smail.sistema_mail_OdontoCool.repositories.AsignacionEstadoCitaRepository;
import smail.sistema_mail_OdontoCool.repositories.EstadoCitaRepotory;
import smail.sistema_mail_OdontoCool.validations.CitaVal;
import smail.sistema_mail_OdontoCool.repositories.HistorialClinicoRepository;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;
import smail.sistema_mail_OdontoCool.repositories.SecretariaRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.repositories.DoctorRepository;

@Service
public class CitaServices {
    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private SecretariaRepository secretariaRepository;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private EstadoCitaRepotory estadoCitaRepotory;

    @Autowired
    private AsignacionEstadoCitaRepository asignacionEstadoCitaRepository;

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private CitaVal citaVal;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void handle(String actions, List<String> params, String fromEmail) {
        switch (actions) {
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
            case "CNL":
                cancel(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Citas.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria o Paciente
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PACIENTE")
                    || usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            // Parametros: FechaCita[0], HoraInicio[1], HoraFin[2], Mofivo[3],
            // Observacion[4], CI_Secretaria[5], CI_Paciente[6], CodigoHistorial[7],
            // CI_Doctor[8]
            if (params.size() < 9) {
                sendResponse(fromEmail, "Error",
                        "Faltan parámetros para Cita. Se requieren 9: FechaCita, HoraInicio, HoraFin, Motivo, Observacion, CI_Secretaria, CI_Paciente, CodigoHistorial, CI_Doctor");
                return;
            }

            String validationMsg = citaVal.insertValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Cita cita = new Cita();
            cita.setFechaCita(LocalDate.parse(params.get(0)));
            cita.setHoraInicio(parseLocalTimeSafely(params.get(1)));
            cita.setHoraFin(parseLocalTimeSafely(params.get(2)));
            cita.setMotivo(params.get(3));
            cita.setObservacion(params.get(4));
            cita.setSecretaria(secretariaRepository.findById(params.get(5)).orElse(null));
            cita.setPaciente(pacienteRepository.findById(params.get(6)).orElse(null));
            cita.setHistorialClinico(historialClinicoRepository.findById(params.get(7)).orElse(null));
            cita.setDoctor(doctorRepository.findById(params.get(8)).orElse(null));

            citaRepository.save(cita);
            sendResponse(fromEmail, "Éxito", "Cita registrada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar cita: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            if (params.size() == 0 || params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error",
                        "Falta especificar tipo de listado o término de búsqueda. Verifique el formato de comandos en la ayuda (HELP).");
                return;
            }

            String query = params.get(0).trim();
            StringBuilder sb = new StringBuilder();
            List<Cita> listado = new java.util.ArrayList<>();

            if ("*".equals(query)) {
                // Verificar si es Secretaria
                boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
                if (!exists) {
                    sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                    return;
                }
                listado = citaRepository.findAllWithAsignaciones();
                sb.append("Lista de Citas:\n\n");
            } else {
                // Verificar si es Secretaria o Paciente
                boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PACIENTE")
                        || usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
                if (!exists) {
                    sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                    return;
                }
                // Intentar buscar por ID de cita
                try {
                    Long idCita = Long.parseLong(query);
                    Cita cita = citaRepository.findByIdWithAsignaciones(idCita).orElse(null);
                    if (cita != null) {
                        listado.add(cita);
                    }
                } catch (NumberFormatException e) {
                    // No es un ID numérico
                }

                // Buscar por CI del paciente
                List<Cita> porPaciente = citaRepository.findByPaciente_CiWithAsignaciones(query);
                for (Cita c : porPaciente) {
                    if (!listado.contains(c)) {
                        listado.add(c);
                    }
                }
                sb.append("Resultados de búsqueda de Citas para '").append(query).append("':\n\n");
            }

            if (listado.isEmpty()) {
                sb.append("No se encontraron citas.\n");
            } else {
                int count = 0;
                for (Cita c : listado) {
                    AsignacionEstadoCita ultAsig = getUltimaAsignacion(c);
                    String estado = (ultAsig != null && ultAsig.getEstadoCita() != null)
                            ? ultAsig.getEstadoCita().getNombre()
                            : "PENDIENTE";
                    if ("ELIMINADA".equalsIgnoreCase(estado)) {
                        continue;
                    }
                    String obsAsig = (ultAsig != null && ultAsig.getObservaciones() != null)
                            ? ultAsig.getObservaciones()
                            : "Sin observaciones de la asignación";
                    count++;
                    sb.append("- Cita:\n")
                            .append("  * ID: ").append(c.getIdCita()).append("\n")
                            .append("  * Fecha: ").append(c.getFechaCita()).append("\n")
                            .append("  * Hora Inicio: ").append(c.getHoraInicio()).append("\n")
                            .append("  * Hora Fin: ").append(c.getHoraFin()).append("\n")
                            .append("  * Motivo: ").append(c.getMotivo() != null ? c.getMotivo() : "Sin motivo")
                            .append("\n")
                            .append("  * Observación: ")
                            .append(c.getObservacion() != null ? c.getObservacion() : "Sin observación").append("\n")
                            .append("  * Estado: ").append(estado).append("\n")
                            .append("  * Observación de la asignación: ").append(obsAsig).append("\n")
                            .append("  * Secretaria CI: ").append(c.getSecretaria().getCi()).append("\n")
                            .append("    * Secretaria Nombre: ")
                            .append(c.getSecretaria().getNombres() != null ? c.getSecretaria().getNombres()
                                    : "Sin nombre")
                            .append("\n")
                            .append("    * Secretaria Apellido: ")
                            .append(c.getSecretaria().getApellidos() != null ? c.getSecretaria().getApellidos()
                                    : "Sin apellido")
                            .append("\n")
                            .append("    * Secretaria Género: ")
                            .append(c.getSecretaria().getGenero() != null ? c.getSecretaria().getGenero()
                                    : "Sin género")
                            .append("\n")
                            .append("    * Secretaria Teléfono: ")
                            .append(c.getSecretaria().getTelefono() != null ? c.getSecretaria().getTelefono()
                                    : "Sin teléfono")
                            .append("\n")
                            .append("  * Paciente CI: ").append(c.getPaciente().getCi()).append("\n")
                            .append("    * Paciente Nombre: ")
                            .append(c.getPaciente().getNombres() != null ? c.getPaciente().getNombres() : "Sin nombre")
                            .append("\n")
                            .append("    * Paciente Apellido: ")
                            .append(c.getPaciente().getApellidos() != null ? c.getPaciente().getApellidos()
                                    : "Sin apellido")
                            .append("\n")
                            .append("    * Paciente Género: ")
                            .append(c.getPaciente().getGenero() != null ? c.getPaciente().getGenero() : "Sin género")
                            .append("\n")
                            .append("    * Paciente Teléfono: ")
                            .append(c.getPaciente().getTelefono() != null ? c.getPaciente().getTelefono()
                                    : "Sin teléfono")
                            .append("\n")
                            .append("  * Doctor CI: ")
                            .append(c.getDoctor() != null ? c.getDoctor().getCi() : "Sin doctor").append("\n")
                            .append("    * Doctor Nombre: ")
                            .append(c.getDoctor() != null && c.getDoctor().getNombres() != null
                                    ? c.getDoctor().getNombres()
                                    : "Sin nombre")
                            .append("\n")
                            .append("    * Doctor Apellido: ")
                            .append(c.getDoctor() != null && c.getDoctor().getApellidos() != null
                                    ? c.getDoctor().getApellidos()
                                    : "Sin apellido")
                            .append("\n")
                            .append("    * Doctor Género: ")
                            .append(c.getDoctor() != null && c.getDoctor().getGenero() != null
                                    ? c.getDoctor().getGenero()
                                    : "Sin género")
                            .append("\n")
                            .append("    * Doctor Teléfono: ")
                            .append(c.getDoctor() != null && c.getDoctor().getTelefono() != null
                                    ? c.getDoctor().getTelefono()
                                    : "Sin teléfono")
                            .append("\n")
                            .append("  * Historial: ")
                            .append(c.getHistorialClinico() != null ? c.getHistorialClinico().getCodigoHistorial()
                                    : "Sin historial")
                            .append("\n\n");
                }
                if (count == 0) {
                    sb.append("No se encontraron citas activas.\n");
                }
            }

            sendResponse(fromEmail, "Listado de Citas", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar citas: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params.size() < 7) {
                sendResponse(fromEmail, "Error",
                        "Faltan parámetros para Cita. Se requieren 7: IdCita, FechaCita, HoraInicio, HoraFin, Motivo, Observacion, CodigoHistorial.");
                return;
            }

            String validationMsg = citaVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long idCita = Long.parseLong(params.get(0));
            Cita cita = citaRepository.findByIdWithAsignaciones(idCita).orElse(null);
            if (cita == null) {
                sendResponse(fromEmail, "Error", "Cita no encontrada.");
                return;
            }

            if ("ELIMINADA".equalsIgnoreCase(getEstadoActual(cita))) {
                sendResponse(fromEmail, "Error", "No se puede modificar una cita eliminada.");
                return;
            }

            if (!params.get(1).isEmpty()) {
                cita.setFechaCita(LocalDate.parse(params.get(1).replace('/', '-')));
            }
            if (!params.get(2).isEmpty()) {
                cita.setHoraInicio(parseLocalTimeSafely(params.get(2)));
            }
            if (!params.get(3).isEmpty()) {
                cita.setHoraFin(parseLocalTimeSafely(params.get(3)));
            }
            if (!params.get(4).isEmpty()) {
                cita.setMotivo(params.get(4));
            }
            if (!params.get(5).isEmpty()) {
                cita.setObservacion(params.get(5));
            }

            if (!params.get(6).isEmpty()) {
                cita.setHistorialClinico(historialClinicoRepository.findById(params.get(6)).orElse(null));
            }

            citaRepository.save(cita);

            EstadoCita estadoCita = estadoCitaRepotory.findByNombre("REPROGRAMADA");
            if (estadoCita == null) {
                estadoCita = new EstadoCita();
                estadoCita.setNombre("REPROGRAMADA");
                estadoCita.setDescripcion("Cita reprogramada");
                estadoCita = estadoCitaRepotory.save(estadoCita);
            }
            AsignacionEstadoCita asignacion = new AsignacionEstadoCita();
            asignacion.setCita(cita);
            asignacion.setEstadoCita(estadoCita);
            asignacion.setObservaciones("Reprogramación automática por modificación de datos");
            asignacionEstadoCitaRepository.save(asignacion);

            sendResponse(fromEmail, "Éxito", "Cita modificada y reprogramada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al modificar cita: " + e.getMessage());
        }
    }

    @Transactional
    private void delete(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params.size() != 1) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Cita. Se requieren 1.");
                return;
            }
            Long idCita = Long.parseLong(params.get(0));
            Cita cita = citaRepository.findByIdWithAsignaciones(idCita).orElse(null);
            if (cita == null) {
                sendResponse(fromEmail, "Error", "Cita no encontrada.");
                return;
            }

            if ("ELIMINADA".equalsIgnoreCase(getEstadoActual(cita))) {
                sendResponse(fromEmail, "Error", "La cita ya se encuentra eliminada.");
                return;
            }

            EstadoCita estadoCita = estadoCitaRepotory.findByNombre("ELIMINADA");
            if (estadoCita == null) {
                estadoCita = new EstadoCita();
                estadoCita.setNombre("ELIMINADA");
                estadoCita.setDescripcion("Cita eliminada de forma lógica");
                estadoCita = estadoCitaRepotory.save(estadoCita);
            }
            AsignacionEstadoCita asignacion = new AsignacionEstadoCita();
            asignacion.setCita(cita);
            asignacion.setEstadoCita(estadoCita);
            asignacion.setObservaciones("Eliminación lógica de la cita");
            asignacionEstadoCitaRepository.save(asignacion);

            sendResponse(fromEmail, "Éxito", "Cita eliminada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar cita: " + e.getMessage());
        }
    }

    @Transactional
    private void cancel(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params.size() != 1) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Cita. Se requieren 1 (IdCita).");
                return;
            }
            Long idCita = Long.parseLong(params.get(0));
            Cita cita = citaRepository.findByIdWithAsignaciones(idCita).orElse(null);
            if (cita == null) {
                sendResponse(fromEmail, "Error", "Cita no encontrada.");
                return;
            }

            String estadoActual = getEstadoActual(cita);
            if ("ELIMINADA".equalsIgnoreCase(estadoActual)) {
                sendResponse(fromEmail, "Error", "No se puede cancelar una cita eliminada.");
                return;
            }
            if ("CANCELADA".equalsIgnoreCase(estadoActual)) {
                sendResponse(fromEmail, "Error", "La cita ya se encuentra cancelada.");
                return;
            }

            EstadoCita estadoCita = estadoCitaRepotory.findByNombre("CANCELADA");
            if (estadoCita == null) {
                estadoCita = new EstadoCita();
                estadoCita.setNombre("CANCELADA");
                estadoCita.setDescripcion("Cita cancelada");
                estadoCita = estadoCitaRepotory.save(estadoCita);
            }
            AsignacionEstadoCita asignacion = new AsignacionEstadoCita();
            asignacion.setCita(cita);
            asignacion.setEstadoCita(estadoCita);
            asignacion.setObservaciones("Cancelación de la cita");
            asignacionEstadoCitaRepository.save(asignacion);

            sendResponse(fromEmail, "Éxito", "Cita cancelada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al cancelar cita: " + e.getMessage());
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
            System.err.println("Error SMTP en CitaServices: " + e.getMessage());
        }
    }

    private LocalTime parseLocalTimeSafely(String timeStr) {
        if (timeStr == null)
            return null;
        timeStr = timeStr.trim().replaceAll("\\s+", "");
        String[] parts = timeStr.split(":");
        if (parts.length >= 2) {
            String hour = parts[0];
            String minute = parts[1];
            if (hour.length() == 1) {
                hour = "0" + hour;
            }
            if (minute.length() == 1) {
                minute = "0" + minute;
            }
            String second = "00";
            if (parts.length >= 3) {
                second = parts[2];
                if (second.length() == 1) {
                    second = "0" + second;
                }
            }
            timeStr = hour + ":" + minute + ":" + second;
        }
        return LocalTime.parse(timeStr);
    }
}
