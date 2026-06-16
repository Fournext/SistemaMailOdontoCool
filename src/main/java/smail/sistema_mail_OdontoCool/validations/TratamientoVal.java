package smail.sistema_mail_OdontoCool.validations;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.repositories.HistorialClinicoRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;

@Component
public class TratamientoVal {

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    public String insertValid(List<String> params) {
        if (params == null || params.size() < 7) {
            return "Faltan parámetros. Se requieren: objetivo, observacion, estado, fechaInicio, fechaFin, fechaFinReal, codigoHistorial.\n";
        }

        StringBuilder msg = new StringBuilder();

        validarTextoObligatorio(params.get(0), "El objetivo del tratamiento", msg);
        validarTextoObligatorio(params.get(2), "El estado", msg);
        validarEstado(params.get(2), msg);
        validarFecha(params.get(3), "fecha de inicio", true, msg);
        validarFecha(params.get(4), "fecha fin", true, msg);
        validarFecha(params.get(5), "fecha fin real", true, msg);
        validarRangoFechas(params.get(3), params.get(4), params.get(5), msg);
        validarHistorial(params.get(6), true, msg);

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        if (params == null || params.size() < 7) {
            return "Faltan parámetros. Se requieren: id, objetivo, observacion, estado, fechaInicio, fechaFin, fechaFinReal.\n";
        }

        StringBuilder msg = new StringBuilder();

        validarTratamientoId(params.get(0), msg);

        if (!params.get(3).trim().isEmpty()) {
            validarEstado(params.get(3), msg);
        }

        validarFecha(params.get(4), "fecha de inicio", false, msg);
        validarFecha(params.get(5), "fecha fin", false, msg);
        validarFecha(params.get(6), "fecha fin real", false, msg);
        validarRangoFechas(params.get(4), params.get(5), params.get(6), msg);

        return msg.toString();
    }

    public String listValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "Falta especificar tipo de listado. Use * o Historial:CODIGO.\n";
        }

        if (params.size() != 1) {
            return "Cantidad de parámetros incorrecta para listar tratamientos.\n";
        }

        String parametro = params.get(0).trim();

        if ("*".equals(parametro)) {
            return "";
        }

        if (parametro.startsWith("Historial:")) {
            String codigoHistorial = parametro.substring("Historial:".length()).trim();

            if (codigoHistorial.isEmpty()) {
                return "Debe indicar el código del historial clínico.\n";
            }

            if (!historialClinicoRepository.existsById(codigoHistorial)) {
                return "No existe historial clínico con código: " + codigoHistorial + ".\n";
            }

            return "";
        }

        return "Listado no permitido para Tratamientos.\n";
    }

    public String buscarValid(List<String> params) {
        if (params == null || params.size() != 1 || params.get(0).trim().isEmpty()) {
            return "Debe enviar el ID del tratamiento.\n";
        }

        return validarTratamientoId(params.get(0), new StringBuilder()).toString();
    }

    public String deleteValid(List<String> params) {
        return buscarValid(params);
    }

    private StringBuilder validarTratamientoId(String idTexto, StringBuilder msg) {
        try {
            Long id = Long.parseLong(idTexto.trim());
            if (!tratamientoRepository.existsById(id)) {
                msg.append("No existe tratamiento con ID: ").append(id).append(".\n");
            }
        } catch (NumberFormatException e) {
            msg.append("El ID del tratamiento debe ser numérico.\n");
        }

        return msg;
    }

    private void validarTextoObligatorio(String valor, String campo, StringBuilder msg) {
        if (valor == null || valor.trim().isEmpty()) {
            msg.append(campo).append(" es obligatorio.\n");
        }
    }

    private void validarEstado(String estado, StringBuilder msg) {
        if (estado == null || estado.trim().isEmpty()) {
            return;
        }

        if (!estado.equalsIgnoreCase("ACTIVO")
                && !estado.equalsIgnoreCase("INACTIVO")
                && !estado.equalsIgnoreCase("FINALIZADO")
                && !estado.equalsIgnoreCase("EN_PROCESO")) {
            msg.append("El estado debe ser ACTIVO, INACTIVO, EN_PROCESO o FINALIZADO.\n");
        }
    }

    private void validarFecha(String fechaTexto, String campo, boolean obligatorio, StringBuilder msg) {
        if (fechaTexto == null || fechaTexto.trim().isEmpty()) {
            if (obligatorio) {
                msg.append("La ").append(campo).append(" es obligatoria.\n");
            }
            return;
        }

        try {
            LocalDate.parse(fechaTexto.trim().replace('/', '-'));
        } catch (Exception e) {
            msg.append("La ").append(campo).append(" debe tener formato yyyy-MM-dd.\n");
        }
    }

    private void validarRangoFechas(String inicioTexto, String finTexto, String finRealTexto, StringBuilder msg) {
        try {
            LocalDate inicio = inicioTexto.trim().isEmpty() ? null : LocalDate.parse(inicioTexto.trim().replace('/', '-'));
            LocalDate fin = finTexto.trim().isEmpty() ? null : LocalDate.parse(finTexto.trim().replace('/', '-'));
            LocalDate finReal = finRealTexto.trim().isEmpty() ? null : LocalDate.parse(finRealTexto.trim().replace('/', '-'));

            if (inicio != null && fin != null && fin.isBefore(inicio)) {
                msg.append("La fecha fin no puede ser anterior a la fecha inicio.\n");
            }

            if (inicio != null && finReal != null && finReal.isBefore(inicio)) {
                msg.append("La fecha fin real no puede ser anterior a la fecha inicio.\n");
            }
        } catch (Exception ignored) {
        }
    }

    private void validarHistorial(String codigoHistorial, boolean obligatorio, StringBuilder msg) {
        if (codigoHistorial == null || codigoHistorial.trim().isEmpty()) {
            if (obligatorio) {
                msg.append("El código de historial clínico es obligatorio.\n");
            }
            return;
        }

        if (!historialClinicoRepository.existsById(codigoHistorial.trim())) {
            msg.append("No existe historial clínico con código: ").append(codigoHistorial).append(".\n");
        }
    }
}