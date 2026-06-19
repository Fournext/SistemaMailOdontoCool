package smail.sistema_mail_OdontoCool.validations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.entities.Cita;
import smail.sistema_mail_OdontoCool.entities.AsignacionTurnoDoctor;
import smail.sistema_mail_OdontoCool.entities.AsignacionEstadoCita;
import smail.sistema_mail_OdontoCool.repositories.CitaRepository;
import smail.sistema_mail_OdontoCool.repositories.HistorialClinicoRepository;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;
import smail.sistema_mail_OdontoCool.repositories.SecretariaRepository;

@Component
public class CitaVal {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private SecretariaRepository secretariaRepository;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private smail.sistema_mail_OdontoCool.repositories.DoctorRepository doctorRepository;

    @Autowired
    private smail.sistema_mail_OdontoCool.repositories.AsignacionTurnoDoctorRepository asignacionTurnoDoctorRepository;

    public String insertValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "No se recibieron parámetros.\n";
        }

        if (params.size() < 9) {
            return "Faltan parámetros para Cita. Se requieren 9 (FechaCita, HoraInicio, HoraFin, Motivo, Observacion, CI_Secretaria, CI_Paciente, CodigoHistorial, CI_Doctor).\n";
        }

        StringBuilder msg = new StringBuilder();

        String fechaCita = params.get(0);
        String horaInicio = params.get(1);
        String horaFin = params.get(2);
        String motivo = params.get(3);
        // String observacion = params.get(4);
        String ciSecretaria = params.get(5);
        String ciPaciente = params.get(6);
        String codigoHistorial = params.get(7);
        String ciDoctor = params.get(8);

        if (fechaCita.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty() || motivo.isEmpty()
                || ciSecretaria.isEmpty() || ciPaciente.isEmpty() || ciDoctor.isEmpty()) {
            msg.append(
                    "Todos los campos obligatorios deben estar llenos (FechaCita, HoraInicio, HoraFin, Motivo, CI_Secretaria, CI_Paciente, CI_Doctor).\n");
        }

        // Validar formato fecha
        if (!fechaCita.isEmpty()) {
            if (!fechaCita.matches("^[0-9]{4}[-/][0-9]{2}[-/][0-9]{2}$")) {
                msg.append("La fecha de cita debe tener el formato yyyy-MM-dd o yyyy/MM/dd: (").append(fechaCita)
                        .append(").\n");
            } else {
                try {
                    LocalDate.parse(fechaCita.replace('/', '-'));
                } catch (Exception e) {
                    msg.append("La fecha de cita no es válida: (").append(fechaCita).append(").\n");
                }
            }
        }

        // Validar formato hora inicio
        LocalTime timeInicio = null;
        if (!horaInicio.isEmpty()) {
            if (!horaInicio.matches("^[0-9]{1,2}:[0-9]{2}(:[0-9]{2})?$")) {
                msg.append("La hora de inicio debe tener el formato HH:mm o HH:mm:ss: (").append(horaInicio)
                        .append(").\n");
            } else {
                try {
                    timeInicio = parseLocalTimeSafely(horaInicio);
                } catch (Exception e) {
                    msg.append("La hora de inicio no es válida: (").append(horaInicio).append(").\n");
                }
            }
        }

        // Validar formato hora fin
        LocalTime timeFin = null;
        if (!horaFin.isEmpty()) {
            if (!horaFin.matches("^[0-9]{1,2}:[0-9]{2}(:[0-9]{2})?$")) {
                msg.append("La hora de fin debe tener el formato HH:mm o HH:mm:ss: (").append(horaFin).append(").\n");
            } else {
                try {
                    timeFin = parseLocalTimeSafely(horaFin);
                } catch (Exception e) {
                    msg.append("La hora de fin no es válida: (").append(horaFin).append(").\n");
                }
            }
        }

        // Validar que hora inicio sea antes que hora fin
        if (timeInicio != null && timeFin != null) {
            if (!timeInicio.isBefore(timeFin)) {
                msg.append("La hora de inicio debe ser anterior a la hora de fin.\n");
            }
        }

        // Validar existencia de Secretaria
        if (!ciSecretaria.isEmpty() && !secretariaRepository.existsById(ciSecretaria)) {
            msg.append("No existe una secretaria registrada con el CI: (").append(ciSecretaria).append(").\n");
        }

        // Validar existencia de Paciente
        if (!ciPaciente.isEmpty() && !pacienteRepository.existsById(ciPaciente)) {
            msg.append("No existe un paciente registrado con el CI: (").append(ciPaciente).append(").\n");
        }

        // Validar existencia de Historial Clínico
        if (!codigoHistorial.isEmpty() && !historialClinicoRepository.existsById(codigoHistorial)) {
            msg.append("No existe un historial clínico registrado con el código: (").append(codigoHistorial)
                    .append(").\n");
        }

        // Validar existencia de Doctor
        if (!ciDoctor.isEmpty() && !doctorRepository.existsById(ciDoctor)) {
            msg.append("No existe un doctor registrado con el CI: (").append(ciDoctor).append(").\n");
        }

        if (msg.length() == 0) {
            try {
                LocalDate date = LocalDate.parse(fechaCita.replace('/', '-'));
                LocalTime tInicio = parseLocalTimeSafely(horaInicio);
                LocalTime tFin = parseLocalTimeSafely(horaFin);
                String checkConflict = validarTurnoYHorarioDoctor(ciDoctor, date, tInicio, tFin, null);
                msg.append(checkConflict);
            } catch (Exception e) {
                System.out.println("ERROR VALIDACIONES CITAS: " + e.getMessage());
            }
        }

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "No se recibieron parámetros.\n";
        }

        if (params.size() < 7) {
            return "Faltan parámetros para modificar Cita. Se requieren 7 (IdCita, FechaCita, HoraInicio, HoraFin, Motivo, Observacion, CodigoHistorial).\n";
        }

        StringBuilder msg = new StringBuilder();

        String idCitaStr = params.get(0);
        String fechaCita = params.get(1);
        String horaInicio = params.get(2);
        String horaFin = params.get(3);
        // String motivo = params.get(4);
        // String observacion = params.get(5);
        String codigoHistorial = params.get(6);

        if (idCitaStr.isEmpty()) {
            msg.append("El ID de la cita es obligatorio.\n");
        } else {
            try {
                Long idCita = Long.parseLong(idCitaStr);
                Cita cita = citaRepository.findById(idCita).orElse(null);
                if (cita == null) {
                    msg.append("No existe una cita registrada con el ID: (").append(idCitaStr).append(").\n");
                }
            } catch (NumberFormatException e) {
                msg.append("El ID de la cita debe ser numérico: (").append(idCitaStr).append(").\n");
            }
        }

        // Validar formato fecha si no está vacía
        if (!fechaCita.isEmpty()) {
            if (!fechaCita.matches("^[0-9]{4}[-/][0-9]{2}[-/][0-9]{2}$")) {
                msg.append("La fecha de cita debe tener el formato yyyy-MM-dd o yyyy/MM/dd: (").append(fechaCita)
                        .append(").\n");
            } else {
                try {
                    LocalDate.parse(fechaCita.replace('/', '-'));
                } catch (Exception e) {
                    msg.append("La fecha de cita no es válida: (").append(fechaCita).append(").\n");
                }
            }
        }

        // Validar formato hora inicio si no está vacía
        LocalTime timeInicio = null;
        if (!horaInicio.isEmpty()) {
            if (!horaInicio.matches("^[0-9]{1,2}:[0-9]{2}(:[0-9]{2})?$")) {
                msg.append("La hora de inicio debe tener el formato HH:mm o HH:mm:ss: (").append(horaInicio)
                        .append(").\n");
            } else {
                try {
                    timeInicio = parseLocalTimeSafely(horaInicio);
                } catch (Exception e) {
                    msg.append("La hora de inicio no es válida: (").append(horaInicio).append(").\n");
                }
            }
        }

        // Validar formato hora fin si no está vacía
        LocalTime timeFin = null;
        if (!horaFin.isEmpty()) {
            if (!horaFin.matches("^[0-9]{1,2}:[0-9]{2}(:[0-9]{2})?$")) {
                msg.append("La hora de fin debe tener el formato HH:mm o HH:mm:ss: (").append(horaFin).append(").\n");
            } else {
                try {
                    timeFin = parseLocalTimeSafely(horaFin);
                } catch (Exception e) {
                    msg.append("La hora de fin no es válida: (").append(horaFin).append(").\n");
                }
            }
        }

        // Validar que hora inicio sea antes que hora fin
        if (timeInicio != null && timeFin != null) {
            if (!timeInicio.isBefore(timeFin)) {
                msg.append("La hora de inicio debe ser anterior a la hora de fin.\n");
            }
        }

        // Validar existencia de Historial Clínico si no está vacía
        if (!codigoHistorial.isEmpty() && !historialClinicoRepository.existsById(codigoHistorial)) {
            msg.append("No existe un historial clínico registrado con el código: (").append(codigoHistorial)
                    .append(").\n");
        }

        if (msg.length() == 0) {
            try {
                Long idCita = Long.parseLong(idCitaStr);
                Cita cita = citaRepository.findById(idCita).orElse(null);
                if (cita != null && cita.getDoctor() != null) {
                    LocalDate date = fechaCita.isEmpty() ? cita.getFechaCita()
                            : LocalDate.parse(fechaCita.replace('/', '-'));
                    LocalTime tInicio = horaInicio.isEmpty() ? cita.getHoraInicio() : parseLocalTimeSafely(horaInicio);
                    LocalTime tFin = horaFin.isEmpty() ? cita.getHoraFin() : parseLocalTimeSafely(horaFin);

                    String checkConflict = validarTurnoYHorarioDoctor(cita.getDoctor().getCi(), date, tInicio, tFin,
                            idCita);
                    msg.append(checkConflict);
                }
            } catch (Exception e) {
                System.out.println("ERROR VALIDACION CITA: " + e.getMessage());
            }
        }

        return msg.toString();
    }

    private String getDayOfWeekInSpanish(java.time.DayOfWeek day) {
        switch (day) {
            case MONDAY:
                return "LU";
            case TUESDAY:
                return "MA";
            case WEDNESDAY:
                return "MI";
            case THURSDAY:
                return "JU";
            case FRIDAY:
                return "VI";
            case SATURDAY:
                return "SA";
            case SUNDAY:
                return "DO";
            default:
                return "";
        }
    }

    private String cleanString(String input) {
        if (input == null)
            return "";
        return input.toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }

    private String validarTurnoYHorarioDoctor(String ciDoctor, LocalDate fechaCita, LocalTime horaInicio,
            LocalTime horaFin, Long idCitaExcluir) {
        List<AsignacionTurnoDoctor> asignaciones = asignacionTurnoDoctorRepository.findByDoctor_Ci(ciDoctor);
        boolean tieneTurnoCubierto = false;
        String dayOfWeekSp = cleanString(getDayOfWeekInSpanish(fechaCita.getDayOfWeek()));

        for (AsignacionTurnoDoctor asig : asignaciones) {
            if (asig.getEstado() != null && "inactivo".equalsIgnoreCase(asig.getEstado().trim())) {
                continue;
            }
            if ((fechaCita.isEqual(asig.getFechaInicio()) || fechaCita.isAfter(asig.getFechaInicio())) &&
                    (fechaCita.isEqual(asig.getFechaFin()) || fechaCita.isBefore(asig.getFechaFin()))) {

                String diasAsignados = cleanString(asig.getDiaSemana());
                boolean diaValido = false;
                String fullName = getFullDayName(dayOfWeekSp);
                for (String dia : diasAsignados.split("[,\\s-]+")) {
                    if (dia.equals(dayOfWeekSp) || dia.equals(fullName)) {
                        diaValido = true;
                        break;
                    }
                }
                if (diaValido) {
                    if (asig.getTurno() != null) {
                        System.out.println("TURNOS " + asig.getTurno());
                        try {
                            LocalTime turnoInicio = parseLocalTimeSafely(asig.getTurno().getHoraInicio());
                            LocalTime turnoFin = parseLocalTimeSafely(asig.getTurno().getHoraFin());
                            System.out.println("HORA INICIO " + turnoInicio);
                            System.out.println("HORA FIN " + turnoFin);
                            System.out.println("CITA INICIO " + horaInicio);
                            System.out.println("CITA FIN " + horaFin);
                            if (!horaInicio.isBefore(turnoInicio) && !horaFin.isAfter(turnoFin)) {
                                tieneTurnoCubierto = true;
                                break;
                            }
                        } catch (Exception e) {
                            System.out.println("ERROR DE VALIDACION de CITA " + e.getMessage());
                        }
                    }
                }
            }
        }

        if (!tieneTurnoCubierto) {
            return "El doctor no tiene un turno asignado que cubra la fecha " + fechaCita + " en el horario "
                    + horaInicio + " - " + horaFin + ".\n";
        }

        List<Cita> citasDoctor = citaRepository.findByDoctor_CiAndFechaCitaWithAsignaciones(ciDoctor, fechaCita);
        for (Cita c : citasDoctor) {
            if (idCitaExcluir != null && idCitaExcluir.equals(c.getIdCita())) {
                continue;
            }
            if (c.getAsignacionesEstadoCita() != null) {
                AsignacionEstadoCita latest = null;
                for (AsignacionEstadoCita a : c.getAsignacionesEstadoCita()) {
                    if (latest == null || a.getFechaCambio().isAfter(latest.getFechaCambio())) {
                        latest = a;
                    }
                }
                if (latest != null && latest.getEstadoCita() != null) {
                    String est = latest.getEstadoCita().getNombre();
                    if ("ELIMINADA".equalsIgnoreCase(est) || "CANCELADA".equalsIgnoreCase(est)) {
                        continue;
                    }
                }
            }

            LocalTime cInicio = c.getHoraInicio();
            LocalTime cFin = c.getHoraFin();
            if (horaInicio.isBefore(cFin) && cInicio.isBefore(horaFin)) {
                return "El horario de la cita choca con otra cita existente del doctor (Cita ID: " + c.getIdCita()
                        + " de " + cInicio + " a " + cFin + ").\n";
            }
        }

        return "";
    }

    private String getFullDayName(String abbreviation) {
        switch (abbreviation) {
            case "lu": return "lunes";
            case "ma": return "martes";
            case "mi": return "miercoles";
            case "ju": return "jueves";
            case "vi": return "viernes";
            case "sa": return "sabado";
            case "do": return "domingo";
            default: return "";
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
