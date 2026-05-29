package smail.sistema_mail_OdontoCool.validations;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.repositories.HistorialClinicoRepository;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;

@Component
public class HistorialClinicoVal {

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    public String insertValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "No se recibieron parámetros.\n";
        }

        if (params.size() < 6) {
            return "Faltan parámetros para Historial Clínico. Se requieren 6 (CI, Alergias, AntecedentesMedicos, EnfermedadesBase, MotivoApertura, ObservacionesGenerales).\n";
        }

        StringBuilder msg = new StringBuilder();

        String ci = params.get(0);
        String motivoApertura = params.get(4);

        if (ci.isEmpty()) {
            msg.append("El CI del paciente es obligatorio.\n");
        } else if (!ci.matches("^[0-9]+$")) {
            msg.append("El CI del paciente debe ser numérico: (").append(ci).append(").\n");
        } else {
            // Verificar existencia del paciente
            if (!pacienteRepository.existsById(ci)) {
                msg.append("No existe un paciente registrado con el CI: (").append(ci).append(").\n");
            } else {
                // Verificar si ya tiene historial clínico
                if (historialClinicoRepository.existsByPaciente_Ci(ci)) {
                    msg.append("El paciente con CI (").append(ci).append(") ya tiene un historial clínico registrado.\n");
                }
            }
        }

        if (motivoApertura.isEmpty()) {
            msg.append("El motivo de apertura es obligatorio.\n");
        }

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "No se recibieron parámetros.\n";
        }

        if (params.size() < 6) {
            return "Faltan parámetros para modificar Historial Clínico. Se requieren 6 (CodigoHistorial, Alergias, AntecedentesMedicos, EnfermedadesBase, MotivoApertura, ObservacionesGenerales).\n";
        }

        StringBuilder msg = new StringBuilder();

        String codigoHistorial = params.get(0);

        if (codigoHistorial.isEmpty()) {
            msg.append("El código del historial clínico es obligatorio.\n");
        } else {
            if (!historialClinicoRepository.existsById(codigoHistorial)) {
                msg.append("No existe un historial clínico registrado con el código: (").append(codigoHistorial).append(").\n");
            }
        }

        return msg.toString();
    }
}
