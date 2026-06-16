package smail.sistema_mail_OdontoCool.validations;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.repositories.AnalisisRepository;
import smail.sistema_mail_OdontoCool.repositories.ResultadoAnalisisRepository;
import smail.sistema_mail_OdontoCool.repositories.SolicitudAnalisisRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;

@Component
public class SolicitudAnalisisVal {

    @Autowired
    private SolicitudAnalisisRepository solicitudAnalisisRepository;

    @Autowired
    private AnalisisRepository analisisRepository;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private ResultadoAnalisisRepository resultadoAnalisisRepository;

    public String insertValid(List<String> params) {
        if (params == null || params.size() < 4) {
            return "Faltan parámetros. Se requieren: fechaSolicitud, motivo, estado, analisisId, tratamientoId opcional.\n";
        }

        StringBuilder msg = new StringBuilder();

        String fechaTexto = params.get(0).trim();
        String motivo = params.get(1).trim();
        String estado = params.get(2).trim();
        String analisisIdTexto = params.get(3).trim();
        String tratamientoIdTexto = params.size() > 4 ? params.get(4).trim() : "";

        if (fechaTexto.isEmpty()) msg.append("La fecha de solicitud es obligatoria.\n");
        if (motivo.isEmpty()) msg.append("El motivo es obligatorio.\n");
        if (analisisIdTexto.isEmpty()) msg.append("El ID del análisis es obligatorio.\n");

        validarFecha(fechaTexto, "fecha de solicitud", msg);
        validarEstadoSolicitud(estado, true, msg);
        validarAnalisis(analisisIdTexto, msg);
        validarTratamientoOpcional(tratamientoIdTexto, msg);

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        if (params == null || params.size() < 6) {
            return "Faltan parámetros. Se requieren: id, fechaSolicitud, motivo, estado, analisisId, tratamientoId.\n";
        }

        StringBuilder msg = new StringBuilder();

        String idTexto = params.get(0).trim();
        String fechaTexto = params.get(1).trim();
        String estado = params.get(3).trim();
        String analisisIdTexto = params.get(4).trim();
        String tratamientoIdTexto = params.get(5).trim();

        if (idTexto.isEmpty()) {
            msg.append("El ID de la solicitud es obligatorio.\n");
        } else {
            try {
                Long id = Long.parseLong(idTexto);
                if (!solicitudAnalisisRepository.existsById(id)) {
                    msg.append("No existe una solicitud de análisis con ID: ").append(id).append(".\n");
                }
            } catch (NumberFormatException e) {
                msg.append("El ID de la solicitud debe ser numérico.\n");
            }
        }

        validarFecha(fechaTexto, "fecha de solicitud", msg);
        validarEstadoSolicitud(estado, false, msg);
        validarAnalisisOpcional(analisisIdTexto, msg);
        validarTratamientoOpcional(tratamientoIdTexto, msg);

        return msg.toString();
    }

    public String listValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "Falta especificar tipo de listado. Use * o Tratamiento:ID.\n";
        }

        if (params.size() != 1) {
            return "Cantidad de parámetros incorrecta para listar solicitudes de análisis.\n";
        }

        String parametro = params.get(0).trim();

        if ("*".equals(parametro)) {
            return "";
        }

        if (parametro.startsWith("Tratamiento:")) {
            String idTexto = parametro.substring("Tratamiento:".length()).trim();

            if (idTexto.isEmpty()) {
                return "Debe indicar el ID del tratamiento.\n";
            }

            try {
                Long id = Long.parseLong(idTexto);
                if (!tratamientoRepository.existsById(id)) {
                    return "No existe tratamiento con ID: " + id + ".\n";
                }
            } catch (NumberFormatException e) {
                return "El ID del tratamiento debe ser numérico.\n";
            }

            return "";
        }

        return "Listado no permitido para Solicitudes de Análisis.\n";
    }

    public String agregarResultadoValid(List<String> params) {
        if (params == null || params.size() < 7) {
            return "Faltan parámetros. Se requieren: solicitudId, fechaResultado, resultado, observaciones, interpretacion, estado, archivoAdjunto.\n";
        }

        StringBuilder msg = new StringBuilder();

        String solicitudIdTexto = params.get(0).trim();
        String fechaTexto = params.get(1).trim();
        String resultado = params.get(2).trim();
        String estado = params.get(5).trim();

        if (solicitudIdTexto.isEmpty()) {
            msg.append("El ID de solicitud es obligatorio.\n");
        } else {
            try {
                Long id = Long.parseLong(solicitudIdTexto);

                if (!solicitudAnalisisRepository.existsById(id)) {
                    msg.append("No existe solicitud de análisis con ID: ").append(id).append(".\n");
                }

                if (resultadoAnalisisRepository.findBySolicitudAnalisisId(id) != null) {
                    msg.append("La solicitud ya tiene un resultado registrado.\n");
                }

            } catch (NumberFormatException e) {
                msg.append("El ID de solicitud debe ser numérico.\n");
            }
        }

        if (fechaTexto.isEmpty()) msg.append("La fecha de resultado es obligatoria.\n");
        if (resultado.isEmpty()) msg.append("El resultado es obligatorio.\n");

        validarFecha(fechaTexto, "fecha de resultado", msg);
        validarEstadoResultado(estado, msg);

        return msg.toString();
    }

    public String deleteValid(List<String> params) {
        if (params == null || params.size() != 1 || params.get(0).trim().isEmpty()) {
            return "Debe enviar el ID de la solicitud de análisis.\n";
        }

        try {
            Long id = Long.parseLong(params.get(0).trim());
            if (!solicitudAnalisisRepository.existsById(id)) {
                return "No existe solicitud de análisis con ID: " + id + ".\n";
            }
        } catch (NumberFormatException e) {
            return "El ID de la solicitud debe ser numérico.\n";
        }

        return "";
    }

    private void validarFecha(String fechaTexto, String campo, StringBuilder msg) {
        if (fechaTexto == null || fechaTexto.trim().isEmpty()) {
            return;
        }

        try {
            LocalDate.parse(fechaTexto.trim().replace('/', '-'));
        } catch (Exception e) {
            msg.append("La ").append(campo).append(" debe tener formato yyyy-MM-dd.\n");
        }
    }

    private void validarEstadoSolicitud(String estado, boolean permitirVacioComoActivo, StringBuilder msg) {
        if (estado == null || estado.trim().isEmpty()) {
            if (!permitirVacioComoActivo) return;
            return;
        }

        if (!estado.equalsIgnoreCase("ACTIVO")
                && !estado.equalsIgnoreCase("INACTIVO")
                && !estado.equalsIgnoreCase("FINALIZADO")) {
            msg.append("El estado debe ser ACTIVO, INACTIVO o FINALIZADO.\n");
        }
    }

    private void validarEstadoResultado(String estado, StringBuilder msg) {
        if (estado == null || estado.trim().isEmpty()) {
            msg.append("El estado del resultado es obligatorio.\n");
            return;
        }

        if (!estado.equalsIgnoreCase("ACTIVO")
                && !estado.equalsIgnoreCase("INACTIVO")
                && !estado.equalsIgnoreCase("FINALIZADO")) {
            msg.append("El estado del resultado debe ser ACTIVO, INACTIVO o FINALIZADO.\n");
        }
    }

    private void validarAnalisis(String analisisIdTexto, StringBuilder msg) {
        if (analisisIdTexto == null || analisisIdTexto.trim().isEmpty()) return;

        try {
            Long id = Long.parseLong(analisisIdTexto);
            if (!analisisRepository.existsById(id)) {
                msg.append("No existe análisis con ID: ").append(id).append(".\n");
            }
        } catch (NumberFormatException e) {
            msg.append("El ID del análisis debe ser numérico.\n");
        }
    }

    private void validarAnalisisOpcional(String analisisIdTexto, StringBuilder msg) {
        if (analisisIdTexto == null || analisisIdTexto.trim().isEmpty()) return;
        validarAnalisis(analisisIdTexto, msg);
    }

    private void validarTratamientoOpcional(String tratamientoIdTexto, StringBuilder msg) {
        if (tratamientoIdTexto == null || tratamientoIdTexto.trim().isEmpty()) return;

        try {
            Long id = Long.parseLong(tratamientoIdTexto);
            if (!tratamientoRepository.existsById(id)) {
                msg.append("No existe tratamiento con ID: ").append(id).append(".\n");
            }
        } catch (NumberFormatException e) {
            msg.append("El ID del tratamiento debe ser numérico.\n");
        }
    }
}