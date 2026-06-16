package smail.sistema_mail_OdontoCool.validations;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.repositories.DienteRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoDienteRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;

@Component
public class TratamientoDienteVal {

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private DienteRepository dienteRepository;

    @Autowired
    private TratamientoDienteRepository tratamientoDienteRepository;

    public String insertValid(List<String> params) {
        StringBuilder msg = new StringBuilder();

        if (params == null || params.isEmpty()) {
            return "Faltan parámetros para registrar tratamientos de dientes.\n";
        }

        String detallesRaw = params.get(0);

        if (detallesRaw == null || detallesRaw.trim().isEmpty()) {
            return "El detalle de tratamientos de dientes está vacío.\n";
        }

        String[] registros = detallesRaw.split("\\s*\\|\\s*");

        int nroRegistro = 1;

        for (String registro : registros) {
            if (registro == null || registro.trim().isEmpty()) {
                msg.append("El registro ").append(nroRegistro).append(" está vacío.\n");
                nroRegistro++;
                continue;
            }

            String[] campos = registro.split("\\s*;\\s*", -1);

            if (campos.length != 7) {
                msg.append("El registro ").append(nroRegistro)
                        .append(" debe tener 7 campos: caraDental;observacion;fechaRegistro;estado;tratamientoPlanificado;tratamientoId;dienteId.\n");
                nroRegistro++;
                continue;
            }

            validarCampos(campos, nroRegistro, true, msg);
            nroRegistro++;
        }

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        StringBuilder msg = new StringBuilder();

        if (params == null || params.size() < 8) {
            return "Faltan parámetros para actualizar. Se requieren 8 campos: id;caraDental;observacion;fechaRegistro;estado;tratamientoPlanificado;tratamientoId;dienteId.\n";
        }

        String idTexto = params.get(0).trim();

        if (idTexto.isEmpty()) {
            msg.append("El ID del tratamiento de diente es obligatorio.\n");
        } else {
            try {
                Long id = Long.parseLong(idTexto);
                if (!tratamientoDienteRepository.existsById(id)) {
                    msg.append("No existe un tratamiento de diente con ID: ").append(id).append(".\n");
                }
            } catch (NumberFormatException e) {
                msg.append("El ID del tratamiento de diente debe ser numérico.\n");
            }
        }

        String[] campos = {
                params.get(1),
                params.get(2),
                params.get(3),
                params.get(4),
                params.get(5),
                params.get(6),
                params.get(7)
        };

        validarCampos(campos, 1, false, msg);

        return msg.toString();
    }

    public String listValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "Falta especificar tipo de listado. Use * o Tratamiento:ID.\n";
        }

        if (params.size() != 1) {
            return "Cantidad de parámetros incorrecta para listar tratamientos de dientes.\n";
        }

        String parametro = params.get(0).trim();

        if (parametro.equals("*")) {
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
                    return "No existe un tratamiento con ID: " + id + ".\n";
                }
            } catch (NumberFormatException e) {
                return "El ID del tratamiento debe ser numérico.\n";
            }

            return "";
        }

        return "Listado no permitido para Tratamientos de Dientes.\n";
    }

    private void validarCampos(String[] campos, int nroRegistro, boolean obligatorio, StringBuilder msg) {
        String caraDental = campos[0].trim();
        String fechaTexto = campos[2].trim();
        String estado = campos[3].trim();
        String tratamientoPlanificado = campos[4].trim();
        String tratamientoIdTexto = campos[5].trim();
        String dienteIdTexto = campos[6].trim();

        if (obligatorio) {
            if (caraDental.isEmpty()) msg.append("Registro ").append(nroRegistro).append(": la cara dental es obligatoria.\n");
            if (fechaTexto.isEmpty()) msg.append("Registro ").append(nroRegistro).append(": la fecha de registro es obligatoria.\n");
            if (estado.isEmpty()) msg.append("Registro ").append(nroRegistro).append(": el estado es obligatorio.\n");
            if (tratamientoPlanificado.isEmpty()) msg.append("Registro ").append(nroRegistro).append(": el tratamiento planificado es obligatorio.\n");
            if (tratamientoIdTexto.isEmpty()) msg.append("Registro ").append(nroRegistro).append(": el tratamientoId es obligatorio.\n");
            if (dienteIdTexto.isEmpty()) msg.append("Registro ").append(nroRegistro).append(": el dienteId es obligatorio.\n");
        }

        if (!fechaTexto.isEmpty()) {
            try {
                LocalDate.parse(fechaTexto.replace('/', '-'));
            } catch (Exception e) {
                msg.append("Registro ").append(nroRegistro).append(": la fecha debe tener formato yyyy-MM-dd.\n");
            }
        }

        if (!estado.isEmpty()
                && !estado.equalsIgnoreCase("ACTIVO")
                && !estado.equalsIgnoreCase("FINALIZADO")
                && !estado.equalsIgnoreCase("INACTIVO")) {
            msg.append("Registro ").append(nroRegistro).append(": el estado debe ser ACTIVO, FINALIZADO o INACTIVO.\n");
        }

        if (!tratamientoIdTexto.isEmpty()) {
            try {
                Long tratamientoId = Long.parseLong(tratamientoIdTexto);
                if (!tratamientoRepository.existsById(tratamientoId)) {
                    msg.append("Registro ").append(nroRegistro).append(": no existe tratamiento con ID: ").append(tratamientoId).append(".\n");
                }
            } catch (NumberFormatException e) {
                msg.append("Registro ").append(nroRegistro).append(": tratamientoId debe ser numérico.\n");
            }
        }

        if (!dienteIdTexto.isEmpty()) {
            try {
                Long dienteId = Long.parseLong(dienteIdTexto);
                if (!dienteRepository.existsById(dienteId)) {
                    msg.append("Registro ").append(nroRegistro).append(": no existe diente con ID: ").append(dienteId).append(".\n");
                }
            } catch (NumberFormatException e) {
                msg.append("Registro ").append(nroRegistro).append(": dienteId debe ser numérico.\n");
            }
        }
    }
}