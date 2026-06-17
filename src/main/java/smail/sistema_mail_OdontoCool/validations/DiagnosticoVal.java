package smail.sistema_mail_OdontoCool.validations;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.repositories.CitaRepository;
import smail.sistema_mail_OdontoCool.repositories.DiagnosticoRepository;
import smail.sistema_mail_OdontoCool.repositories.DienteRepository;

@Component
public class DiagnosticoVal {

    @Autowired
    private DiagnosticoRepository diagnosticoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private DienteRepository dienteRepository;

    public String insertValid(List<String> params) {
        if (params == null || params.size() < 6) {
            return "Faltan parámetros. Se requieren: sintomas, tipoDiagnostico, gravedad, observaciones, idCita, detalles.\n";
        }

        StringBuilder msg = new StringBuilder();

        validarTextoObligatorio(params.get(0), "Los síntomas", msg);
        validarTextoObligatorio(params.get(1), "El tipo de diagnóstico", msg);
        validarTextoObligatorio(params.get(2), "La gravedad", msg);
        validarCita(params.get(4), true, true, null, msg);
        validarDetalles(params.get(5), msg);

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        if (params == null || params.size() < 6) {
            return "Faltan parámetros. Se requieren: id, sintomas, tipoDiagnostico, gravedad, observaciones, idCita.\n";
        }

        StringBuilder msg = new StringBuilder();

        Long diagnosticoId = validarDiagnosticoId(params.get(0), msg);

        if (!params.get(5).trim().isEmpty()) {
            validarCita(params.get(5), false, true, diagnosticoId, msg);
        }

        return msg.toString();
    }

    public String listValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "Falta especificar tipo de listado. Use *.\n";
        }

        if (params.size() != 1) {
            return "Cantidad de parámetros incorrecta para listar diagnósticos.\n";
        }

        if (!params.get(0).trim().equals("*")) {
            return "Listado no permitido para Diagnósticos. Use LISDIA[*].\n";
        }

        return "";
    }

    public String deleteValid(List<String> params) {
        if (params == null || params.size() != 1 || params.get(0).trim().isEmpty()) {
            return "Debe enviar el ID del diagnóstico.\n";
        }

        StringBuilder msg = new StringBuilder();
        validarDiagnosticoId(params.get(0), msg);
        return msg.toString();
    }

    private Long validarDiagnosticoId(String idTexto, StringBuilder msg) {
        try {
            Long id = Long.parseLong(idTexto.trim());

            if (!diagnosticoRepository.existsById(id)) {
                msg.append("No existe diagnóstico con ID: ").append(id).append(".\n");
            }

            return id;
        } catch (NumberFormatException e) {
            msg.append("El ID del diagnóstico debe ser numérico.\n");
            return null;
        }
    }

    private void validarCita(String citaIdTexto, boolean obligatorio, boolean validarUnico, Long diagnosticoActualId, StringBuilder msg) {
        if (citaIdTexto == null || citaIdTexto.trim().isEmpty()) {
            if (obligatorio) {
                msg.append("El ID de la cita es obligatorio.\n");
            }
            return;
        }

        try {
            Long citaId = Long.parseLong(citaIdTexto.trim());

            if (!citaRepository.existsById(citaId)) {
                msg.append("No existe cita con ID: ").append(citaId).append(".\n");
                return;
            }

            if (validarUnico && diagnosticoRepository.existsByCita_IdCita(citaId)) {
                if (diagnosticoActualId == null) {
                    msg.append("Esta cita ya tiene un diagnóstico registrado.\n");
                } else {
                    diagnosticoRepository.findById(diagnosticoActualId).ifPresent(diagnostico -> {
                        if (!diagnostico.getCita().getIdCita().equals(citaId)) {
                            msg.append("Esta cita ya tiene otro diagnóstico registrado.\n");
                        }
                    });
                }
            }

        } catch (NumberFormatException e) {
            msg.append("El ID de la cita debe ser numérico.\n");
        }
    }

    private void validarDetalles(String detallesRaw, StringBuilder msg) {
        if (detallesRaw == null || detallesRaw.trim().isEmpty()) {
            msg.append("Debe enviar al menos un detalle de diagnóstico.\n");
            return;
        }

        String[] detalles = detallesRaw.split("\\|");

        for (int i = 0; i < detalles.length; i++) {
            String detalleTexto = detalles[i].trim();

            if (detalleTexto.isEmpty()) {
                continue;
            }

            String[] partes = detalleTexto.split(";", -1);

            if (partes.length < 3) {
                msg.append("El detalle N° ").append(i + 1)
                        .append(" está incompleto. Formato: ObservacionDetalle;ZonaBucal;ID Diente.\n");
                continue;
            }

            String zonaBucal = partes[1].trim();
            String dienteIdTexto = partes[2].trim();

            if (zonaBucal.isEmpty()) {
                msg.append("La zona bucal del detalle N° ").append(i + 1).append(" es obligatoria.\n");
            }

            if (dienteIdTexto.isEmpty()) {
                msg.append("El ID del diente del detalle N° ").append(i + 1).append(" es obligatorio.\n");
            } else {
                try {
                    Long dienteId = Long.parseLong(dienteIdTexto);

                    if (!dienteRepository.existsById(dienteId)) {
                        msg.append("No existe diente con ID: ").append(dienteId)
                                .append(" en el detalle N° ").append(i + 1).append(".\n");
                    }

                } catch (NumberFormatException e) {
                    msg.append("El ID del diente del detalle N° ").append(i + 1).append(" debe ser numérico.\n");
                }
            }
        }
    }

    private void validarTextoObligatorio(String valor, String campo, StringBuilder msg) {
        if (valor == null || valor.trim().isEmpty()) {
            msg.append(campo).append(" es obligatorio.\n");
        }
    }
}