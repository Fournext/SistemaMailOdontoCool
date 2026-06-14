package smail.sistema_mail_OdontoCool.validations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.repositories.DoctorRepository;
import smail.sistema_mail_OdontoCool.repositories.TurnoRepository;

@Component
public class AsignacionTurnoDoctorVal {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    private static final List<String> DIAS_VALIDOS = Arrays.asList("LU", "MA", "MI", "JU", "VI", "SA", "DO");

    public String insertValid(List<String> params) {
        if (params == null || params.size() < 6) {
            return "Parámetros insuficientes. Se requieren 6: DiaSemana, FechaInicio, FechaFin, Estado, CI Doctor, ID Turno.\n";
        }

        StringBuilder msg = new StringBuilder();

        String diaSemana = params.get(0).trim().toUpperCase();
        String fechaInicioStr = params.get(1).trim();
        String fechaFinStr = params.get(2).trim();
        String estado = params.get(3).trim();
        String doctorCi = params.get(4).trim();
        String turnoIdStr = params.get(5).trim();

        if (diaSemana.isEmpty() || fechaInicioStr.isEmpty() || fechaFinStr.isEmpty() || estado.isEmpty() || doctorCi.isEmpty() || turnoIdStr.isEmpty()) {
            msg.append("Todos los campos son obligatorios.\n");
        }

        if (!DIAS_VALIDOS.contains(diaSemana)) {
            msg.append("El día de la semana debe ser uno de los siguientes: LU, MA, MI, JU, VI, SA, DO. Se recibió: (").append(diaSemana).append(").\n");
        }

        // Validar fecha_inicio
        if (!fechaInicioStr.isEmpty()) {
            try {
                LocalDate.parse(fechaInicioStr);
            } catch (Exception e) {
                msg.append("La fecha de inicio no es válida (formato YYYY-MM-DD): (").append(fechaInicioStr).append(").\n");
            }
        }

        // Validar fecha_fin
        if (!fechaFinStr.isEmpty()) {
            try {
                LocalDate.parse(fechaFinStr);
            } catch (Exception e) {
                msg.append("La fecha de fin no es válida (formato YYYY-MM-DD): (").append(fechaFinStr).append(").\n");
            }
        }

        // Validar doctor
        if (!doctorCi.isEmpty() && !doctorRepository.existsById(doctorCi)) {
            msg.append("No existe un doctor registrado con el CI: (").append(doctorCi).append(").\n");
        }

        // Validar turno
        if (!turnoIdStr.isEmpty()) {
            try {
                Long turnoId = Long.parseLong(turnoIdStr);
                if (!turnoRepository.existsById(turnoId)) {
                    msg.append("No existe un turno registrado con el ID: (").append(turnoIdStr).append(").\n");
                }
            } catch (NumberFormatException e) {
                msg.append("El ID del turno debe ser numérico: (").append(turnoIdStr).append(").\n");
            }
        }

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        if (params == null || params.size() < 7) {
            return "Parámetros insuficientes. Se requieren 7: ID Asignación, DiaSemana, FechaInicio, FechaFin, Estado, CI Doctor, ID Turno.\n";
        }

        StringBuilder msg = new StringBuilder();

        String idStr = params.get(0).trim();
        String diaSemana = params.get(1).trim().toUpperCase();
        String fechaInicioStr = params.get(2).trim();
        String fechaFinStr = params.get(3).trim();
        String doctorCi = params.get(5).trim();
        String turnoIdStr = params.get(6).trim();

        if (idStr.isEmpty()) {
            msg.append("El ID de la asignación es obligatorio.\n");
        }

        if (!diaSemana.isEmpty() && !DIAS_VALIDOS.contains(diaSemana)) {
            msg.append("El día de la semana debe ser uno de los siguientes: LU, MA, MI, JU, VI, SA, DO. Se recibió: (").append(diaSemana).append(").\n");
        }

        if (!fechaInicioStr.isEmpty()) {
            try {
                LocalDate.parse(fechaInicioStr);
            } catch (Exception e) {
                msg.append("La fecha de inicio no es válida: (").append(fechaInicioStr).append(").\n");
            }
        }

        if (!fechaFinStr.isEmpty()) {
            try {
                LocalDate.parse(fechaFinStr);
            } catch (Exception e) {
                msg.append("La fecha de fin no es válida: (").append(fechaFinStr).append(").\n");
            }
        }

        if (!doctorCi.isEmpty() && !doctorRepository.existsById(doctorCi)) {
            msg.append("No existe un doctor registrado con el CI: (").append(doctorCi).append(").\n");
        }

        if (!turnoIdStr.isEmpty()) {
            try {
                Long turnoId = Long.parseLong(turnoIdStr);
                if (!turnoRepository.existsById(turnoId)) {
                    msg.append("No existe un turno registrado con el ID: (").append(turnoIdStr).append(").\n");
                }
            } catch (NumberFormatException e) {
                msg.append("El ID del turno debe ser numérico: (").append(turnoIdStr).append(").\n");
            }
        }

        return msg.toString();
    }
}
