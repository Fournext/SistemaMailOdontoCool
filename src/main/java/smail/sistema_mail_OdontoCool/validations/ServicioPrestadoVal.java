package smail.sistema_mail_OdontoCool.validations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.repositories.AsignacionPrecioRepository;
import smail.sistema_mail_OdontoCool.repositories.ServicioPrestadoRepository;
import smail.sistema_mail_OdontoCool.repositories.ServicioRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;

@Component
public class ServicioPrestadoVal {

    @Autowired
    private ServicioPrestadoRepository servicioPrestadoRepository;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private AsignacionPrecioRepository asignacionPrecioRepository;

    public String insertValid(List<String> params) {
        StringBuilder msg = new StringBuilder();

        if (params == null || params.isEmpty()) {
            return "Faltan parámetros para registrar servicios prestados.\n";
        }

        String detallesRaw = params.get(0);

        if (detallesRaw == null || detallesRaw.trim().isEmpty()) {
            return "El detalle de servicios prestados está vacío.\n";
        }

        String[] registros = detallesRaw.split("\\s*\\|\\s*");

        int nroRegistro = 1;

        for (String registro : registros) {
            if (registro == null || registro.trim().isEmpty()) {
                msg.append("Registro ").append(nroRegistro).append(": está vacío.\n");
                nroRegistro++;
                continue;
            }

            String[] campos = registro.split("\\s*;\\s*", -1);

            if (campos.length != 6) {
                msg.append("Registro ").append(nroRegistro)
                        .append(": debe tener 6 campos: cantidad;precio;fechaServicio;estado;tratamientoId;servicioId.\n");
                nroRegistro++;
                continue;
            }

            msg.append(validarCampos(List.of(campos), false, nroRegistro));

            nroRegistro++;
        }

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        StringBuilder msg = new StringBuilder();

        if (params == null || params.size() < 7) {
            return "Faltan parámetros. Se requieren: id, cantidad, precio, fechaServicio, estado, tratamientoId, servicioId.\n";
        }

        String idTexto = params.get(0).trim();

        if (idTexto.isEmpty()) {
            msg.append("El ID del servicio prestado es obligatorio.\n");
        } else {
            try {
                Long id = Long.parseLong(idTexto);
                if (!servicioPrestadoRepository.existsById(id)) {
                    msg.append("No existe un servicio prestado con ID: ").append(id).append(".\n");
                }
            } catch (NumberFormatException e) {
                msg.append("El ID del servicio prestado debe ser numérico.\n");
            }
        }

        msg.append(validarCampos(params.subList(1, params.size()), true, 1));

        return msg.toString();
    }

    public String listValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "Falta especificar tipo de listado. Use * o Tratamiento:ID.\n";
        }

        if (params.size() != 1) {
            return "Cantidad de parámetros incorrecta para listar servicios prestados.\n";
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
                    return "No existe un tratamiento con ID: " + id + ".\n";
                }

            } catch (NumberFormatException e) {
                return "El ID del tratamiento debe ser numérico.\n";
            }

            return "";
        }

        return "Listado no permitido para Servicios Prestados.\n";
    }

    public String deleteValid(List<String> params) {
        if (params == null || params.size() != 1 || params.get(0).trim().isEmpty()) {
            return "Debe enviar el ID del servicio prestado.\n";
        }

        try {
            Long id = Long.parseLong(params.get(0).trim());
            if (!servicioPrestadoRepository.existsById(id)) {
                return "No existe un servicio prestado con ID: " + id + ".\n";
            }
        } catch (NumberFormatException e) {
            return "El ID del servicio prestado debe ser numérico.\n";
        }

        return "";
    }

    private String validarCampos(List<String> params, boolean permitirVacios, int nroRegistro) {
        StringBuilder msg = new StringBuilder();

        String cantidadTexto = params.get(0).trim();
        String precioTexto = params.get(1).trim();
        String fechaTexto = params.get(2).trim();
        String estado = params.get(3).trim();
        String tratamientoIdTexto = params.get(4).trim();
        String servicioIdTexto = params.get(5).trim();

        if (!permitirVacios) {
            if (cantidadTexto.isEmpty()) {
                msg.append("Registro ").append(nroRegistro).append(": la cantidad es obligatoria.\n");
            }
            if (precioTexto.isEmpty()) {
                msg.append("Registro ").append(nroRegistro).append(": el precio es obligatorio.\n");
            }
            if (fechaTexto.isEmpty()) {
                msg.append("Registro ").append(nroRegistro).append(": la fecha del servicio es obligatoria.\n");
            }
            if (estado.isEmpty()) {
                msg.append("Registro ").append(nroRegistro).append(": el estado es obligatorio.\n");
            }
            if (tratamientoIdTexto.isEmpty()) {
                msg.append("Registro ").append(nroRegistro).append(": el tratamientoId es obligatorio.\n");
            }
            if (servicioIdTexto.isEmpty()) {
                msg.append("Registro ").append(nroRegistro).append(": el servicioId es obligatorio.\n");
            }
        }

        if (!cantidadTexto.isEmpty()) {
            try {
                int cantidad = Integer.parseInt(cantidadTexto);
                if (cantidad <= 0) {
                    msg.append("Registro ").append(nroRegistro).append(": la cantidad debe ser mayor a 0.\n");
                }
            } catch (NumberFormatException e) {
                msg.append("Registro ").append(nroRegistro).append(": la cantidad debe ser numérica entera.\n");
            }
        }

        if (!precioTexto.isEmpty()) {
            try {
                BigDecimal precio = new BigDecimal(precioTexto);
                if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                    msg.append("Registro ").append(nroRegistro).append(": el precio debe ser mayor a 0.\n");
                }
            } catch (NumberFormatException e) {
                msg.append("Registro ").append(nroRegistro).append(": el precio debe ser numérico.\n");
            }
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
                && !estado.equalsIgnoreCase("INACTIVO")
                && !estado.equalsIgnoreCase("FINALIZADO")) {
            msg.append("Registro ").append(nroRegistro).append(": el estado debe ser ACTIVO, INACTIVO o FINALIZADO.\n");
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

        if (!servicioIdTexto.isEmpty()) {
            try {
                Long servicioId = Long.parseLong(servicioIdTexto);
                if (!servicioRepository.existsById(servicioId)) {
                    msg.append("Registro ").append(nroRegistro).append(": no existe servicio con ID: ").append(servicioId).append(".\n");
                } else if (asignacionPrecioRepository
                        .findTopByServicioIdAndEstadoOrderByFechaInicioDesc(servicioId, "ACTIVO")
                        .isEmpty()) {
                    msg.append("Registro ").append(nroRegistro).append(": el servicio no tiene precio activo asignado.\n");
                }
            } catch (NumberFormatException e) {
                msg.append("Registro ").append(nroRegistro).append(": servicioId debe ser numérico.\n");
            }
        }

        return msg.toString();
    }
}
