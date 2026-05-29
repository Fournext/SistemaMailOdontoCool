package smail.sistema_mail_OdontoCool.validations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.entities.Cita;
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

    public String insertValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "No se recibieron parámetros.\n";
        }

        if (params.size() < 8) {
            return "Faltan parámetros para Cita. Se requieren 8 (FechaCita, HoraInicio, HoraFin, Motivo, Observacion, CI_Secretaria, CI_Paciente, CodigoHistorial).\n";
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

        if (fechaCita.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty() || motivo.isEmpty()
                || ciSecretaria.isEmpty() || ciPaciente.isEmpty() || codigoHistorial.isEmpty()) {
            msg.append(
                    "Todos los campos obligatorios deben estar llenos (FechaCita, HoraInicio, HoraFin, Motivo, CI_Secretaria, CI_Paciente, CodigoHistorial).\n");
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
            if (!horaInicio.matches("^[0-9]{2}:[0-9]{2}(:[0-9]{2})?$")) {
                msg.append("La hora de inicio debe tener el formato HH:mm o HH:mm:ss: (").append(horaInicio)
                        .append(").\n");
            } else {
                try {
                    timeInicio = LocalTime.parse(horaInicio);
                } catch (Exception e) {
                    msg.append("La hora de inicio no es válida: (").append(horaInicio).append(").\n");
                }
            }
        }

        // Validar formato hora fin
        LocalTime timeFin = null;
        if (!horaFin.isEmpty()) {
            if (!horaFin.matches("^[0-9]{2}:[0-9]{2}(:[0-9]{2})?$")) {
                msg.append("La hora de fin debe tener el formato HH:mm o HH:mm:ss: (").append(horaFin).append(").\n");
            } else {
                try {
                    timeFin = LocalTime.parse(horaFin);
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

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "No se recibieron parámetros.\n";
        }

        if (params.size() < 9) {
            return "Faltan parámetros para modificar Cita. Se requieren 9 (IdCita, FechaCita, HoraInicio, HoraFin, Motivo, Observacion, CI_Secretaria, CI_Paciente, CodigoHistorial).\n";
        }

        StringBuilder msg = new StringBuilder();

        String idCitaStr = params.get(0);
        String fechaCita = params.get(1);
        String horaInicio = params.get(2);
        String horaFin = params.get(3);
        // String motivo = params.get(4);
        // String observacion = params.get(5);
        String ciSecretaria = params.get(6);
        String ciPaciente = params.get(7);
        String codigoHistorial = params.get(8);

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
            if (!horaInicio.matches("^[0-9]{2}:[0-9]{2}(:[0-9]{2})?$")) {
                msg.append("La hora de inicio debe tener el formato HH:mm o HH:mm:ss: (").append(horaInicio)
                        .append(").\n");
            } else {
                try {
                    timeInicio = LocalTime.parse(horaInicio);
                } catch (Exception e) {
                    msg.append("La hora de inicio no es válida: (").append(horaInicio).append(").\n");
                }
            }
        }

        // Validar formato hora fin si no está vacía
        LocalTime timeFin = null;
        if (!horaFin.isEmpty()) {
            if (!horaFin.matches("^[0-9]{2}:[0-9]{2}(:[0-9]{2})?$")) {
                msg.append("La hora de fin debe tener el formato HH:mm o HH:mm:ss: (").append(horaFin).append(").\n");
            } else {
                try {
                    timeFin = LocalTime.parse(horaFin);
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

        // Validar existencia de Secretaria si no está vacía
        if (!ciSecretaria.isEmpty() && !secretariaRepository.existsById(ciSecretaria)) {
            msg.append("No existe una secretaria registrada con el CI: (").append(ciSecretaria).append(").\n");
        }

        // Validar existencia de Paciente si no está vacía
        if (!ciPaciente.isEmpty() && !pacienteRepository.existsById(ciPaciente)) {
            msg.append("No existe un paciente registrado con el CI: (").append(ciPaciente).append(").\n");
        }

        // Validar existencia de Historial Clínico si no está vacía
        if (!codigoHistorial.isEmpty() && !historialClinicoRepository.existsById(codigoHistorial)) {
            msg.append("No existe un historial clínico registrado con el código: (").append(codigoHistorial)
                    .append(").\n");
        }

        return msg.toString();
    }
}
