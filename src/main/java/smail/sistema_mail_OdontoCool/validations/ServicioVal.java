package smail.sistema_mail_OdontoCool.validations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.repositories.ServicioRepository;

@Component
public class ServicioVal {

    @Autowired
    private ServicioRepository servicioRepository;

    public String insertValid(List<String> params) {
        StringBuilder msg = new StringBuilder();

        if (params == null || params.size() < 4) {
            return "Faltan parámetros. Se requieren: nombre, descripción, estado, tipo.\n";
        }

        String nombre = params.get(0).trim();
        String descripcion = params.get(1).trim();
        String estado = params.get(2).trim();
        String tipo = params.get(3).trim();

        if (nombre.isEmpty()) msg.append("El nombre es obligatorio.\n");
        if (descripcion.isEmpty()) msg.append("La descripción es obligatoria.\n");
        if (estado.isEmpty()) msg.append("El estado es obligatorio.\n");
        if (tipo.isEmpty()) msg.append("El tipo es obligatorio.\n");

        if (!estado.equalsIgnoreCase("ACTIVO") && !estado.equalsIgnoreCase("INACTIVO")) {
            msg.append("El estado debe ser ACTIVO o INACTIVO.\n");
        }

        if (!nombre.isEmpty() && servicioRepository.findByNombre(nombre).isPresent()) {
            msg.append("Ya existe un servicio con el nombre: ").append(nombre).append(".\n");
        }

        //vamos a validar que el tipo sea string y no contenga números ni simbolos
        if (!tipo.matches("^[a-zA-ZÑñáéíóúÁÉÍÓÚüÜ ]+$")) {
            msg.append("El tipo solo debe contener letras y espacios.\n");
        }

        if (!nombre.matches("^[a-zA-ZÑñáéíóúÁÉÍÓÚüÜ ]+$")) {
            msg.append("El nombre solo debe contener letras y espacios.\n");
        }

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        StringBuilder msg = new StringBuilder();

        if (params == null || params.size() < 5) {
            return "Faltan parámetros. Se requieren: id, nombre, descripción, estado, tipo.\n";
        }

        String idTexto = params.get(0).trim();
        String nombre = params.get(1).trim();
        String descripcion = params.get(2).trim();
        String estado = params.get(3).trim();
        String tipo = params.get(4).trim();

        if (idTexto.isEmpty()) {
            msg.append("El ID es obligatorio.\n");
        } else {
            try {
                Long id = Long.parseLong(idTexto);
                if (!servicioRepository.existsById(id)) {
                    msg.append("No existe un servicio con ID: ").append(id).append(".\n");
                }
            } catch (NumberFormatException e) {
                msg.append("El ID debe ser numérico.\n");
            }
        }

        if (!estado.isEmpty()
                && !estado.equalsIgnoreCase("ACTIVO")
                && !estado.equalsIgnoreCase("INACTIVO")) {
            msg.append("El estado debe ser ACTIVO o INACTIVO.\n");
        }

        // Validar que el nombre no contenga números ni símbolos
        if (!nombre.isEmpty() && !nombre.matches("^[a-zA-ZÑñáéíóúÁÉÍÓÚüÜ ]+$")) {
            msg.append("El nombre solo debe contener letras y espacios.\n");
        }
        // Validar que el tipo no contenga números ni símbolos
        if (!tipo.isEmpty() && !tipo.matches("^[a-zA-ZÑñáéíóúÁÉÍÓÚüÜ ]+$")) {
            msg.append("El tipo solo debe contener letras y espacios.\n");
        }   

        return msg.toString();
    }

    public String asignarPrecioValid(List<String> params) {
        StringBuilder msg = new StringBuilder();

        if (params == null || params.size() < 5) {
            return "Faltan parámetros. Se requieren: nombreServicio, fechaInicio, fechaFin, moneda, monto.\n";
        }

        String nombreServicio = params.get(0).trim();
        String fechaInicioTexto = params.get(1).trim();
        String fechaFinTexto = params.get(2).trim();
        String moneda = params.get(3).trim();
        String montoTexto = params.get(4).trim();

        if (nombreServicio.isEmpty()) msg.append("El nombre del servicio es obligatorio.\n");
        if (fechaInicioTexto.isEmpty()) msg.append("La fecha de inicio es obligatoria.\n");
        if (fechaFinTexto.isEmpty()) msg.append("La fecha fin es obligatoria.\n");
        if (moneda.isEmpty()) msg.append("La moneda es obligatoria.\n");
        if (montoTexto.isEmpty()) msg.append("El monto es obligatorio.\n");

        LocalDate fechaInicio = null;
        LocalDate fechaFin = null;

        try {
            fechaInicio = LocalDate.parse(fechaInicioTexto.replace('/', '-'));
        } catch (Exception e) {
            msg.append("La fecha de inicio debe tener formato yyyy-MM-dd.\n");
        }

        try {
            fechaFin = LocalDate.parse(fechaFinTexto.replace('/', '-'));
        } catch (Exception e) {
            msg.append("La fecha fin debe tener formato yyyy-MM-dd.\n");
        }

        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            msg.append("La fecha fin no puede ser anterior a la fecha inicio.\n");
        }

        if (!moneda.equalsIgnoreCase("BS")
                && !moneda.equalsIgnoreCase("BOB")
                && !moneda.equalsIgnoreCase("USD")) {
            msg.append("La moneda permitida es BS, BOB o USD.\n");
        }

        try {
            BigDecimal monto = new BigDecimal(montoTexto);
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                msg.append("El monto debe ser mayor a 0.\n");
            }
        } catch (NumberFormatException e) {
            msg.append("El monto debe ser numérico.\n");
        }

        if (!nombreServicio.isEmpty() && servicioRepository.findByNombre(nombreServicio).isEmpty()) {
            msg.append("No existe el servicio: ").append(nombreServicio).append(".\n");
        }

        return msg.toString();
    }
}