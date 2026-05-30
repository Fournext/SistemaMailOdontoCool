package smail.sistema_mail_OdontoCool.validations;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AntecedenteOdontologicoVal {

    public String insertValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "No se recibieron parámetros.\n";
        }

        if (params.size() < 3) {
            return "Faltan parámetros para Antecedente Odontológico. Se requieren 3 (ObservacionGeneral, DetalleAntecedentes, CodHistorial).\n";
        }

        StringBuilder msg = new StringBuilder();

        String observacionGeneral = params.get(0);
        String detalleAntecedentes = params.get(1);
        String codHistorial = params.get(2);

        if (observacionGeneral.isEmpty()) {
            msg.append("La observación general es obligatoria.\n");
        }

        if (codHistorial.isEmpty()) {
            msg.append("El código de historial es obligatorio.\n");
        }

        if (detalleAntecedentes.isEmpty()) {
            msg.append("El detalle de los antecedentes es obligatorio.\n");
        } else {
            // Validar formato de la lista: Detalle1 | Detalle2 ...
            String[] details = detalleAntecedentes.split("\\s*\\|\\s*");
            int index = 1;
            for (String detail : details) {
                if (detail.trim().isEmpty()) {
                    continue;
                }
                String[] fields = detail.split("\\s*;\\s*", -1);
                if (fields.length < 5) {
                    msg.append("El detalle #").append(index)
                            .append(" no tiene el formato correcto. Debe tener 5 campos separados por ';' (nombreTratamiento;descripcion;fechaTratamiento;lugarTratamiento;observacion).\n");
                } else {
                    String nombre = fields[0].trim();
                    String fechaStr = fields[2].trim();
                    if (nombre.isEmpty()) {
                        msg.append("El nombre del tratamiento en el detalle #").append(index)
                                .append(" es obligatorio.\n");
                    }
                    if (fechaStr.isEmpty()) {
                        msg.append("La fecha de tratamiento en el detalle #").append(index)
                                .append(" es obligatoria.\n");
                    } else {
                        try {
                            LocalDate.parse(fechaStr);
                        } catch (DateTimeParseException e) {
                            msg.append("La fecha de tratamiento '").append(fechaStr)
                                    .append("' en el detalle #").append(index)
                                    .append(" no es válida. Debe tener el formato AAAA-MM-DD.\n");
                        }
                    }
                }
                index++;
            }
        }

        return msg.toString();
    }

    public String updateValid(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "No se recibieron parámetros.\n";
        }

        if (params.size() < 4) {
            return "Faltan parámetros para modificar Antecedente Odontológico. Se requieren 4 (idAntecedente, ObservacionGeneral, DetalleAntecedentes, CodHistorial).\n";
        }

        StringBuilder msg = new StringBuilder();

        String idAntecedente = params.get(0);
        // String observacionGeneral = params.get(1);
        String detalleAntecedentes = params.get(2);
        // String codHistorial = params.get(3);

        if (idAntecedente.isEmpty()) {
            msg.append("El ID del antecedente es obligatorio.\n");
        } else {
            try {
                Long.parseLong(idAntecedente);
            } catch (NumberFormatException e) {
                msg.append("El ID del antecedente debe ser un número válido.\n");
            }
        }

        if (!detalleAntecedentes.isEmpty()) {
            // Validar formato de la lista: Detalle1 | Detalle2 ...
            String[] details = detalleAntecedentes.split("\\s*\\|\\s*");
            int index = 1;
            for (String detail : details) {
                if (detail.trim().isEmpty()) {
                    continue;
                }
                String[] fields = detail.split("\\s*;\\s*", -1);
                if (fields.length < 5) {
                    msg.append("El detalle #").append(index)
                            .append(" no tiene el formato correcto. Debe tener 5 campos separados por ';' (nombreTratamiento;descripcion;fechaTratamiento;lugarTratamiento;observacion).\n");
                } else {
                    String nombre = fields[0].trim();
                    String fechaStr = fields[2].trim();
                    if (nombre.isEmpty()) {
                        msg.append("El nombre del tratamiento en el detalle #").append(index)
                                .append(" es obligatorio.\n");
                    }
                    if (fechaStr.isEmpty()) {
                        msg.append("La fecha de tratamiento en el detalle #").append(index)
                                .append(" es obligatoria.\n");
                    } else {
                        try {
                            LocalDate.parse(fechaStr);
                        } catch (DateTimeParseException e) {
                            msg.append("La fecha de tratamiento '").append(fechaStr)
                                    .append("' en el detalle #").append(index)
                                    .append(" no es válida. Debe tener el formato AAAA-MM-DD.\n");
                        }
                    }
                }
                index++;
            }
        }

        return msg.toString();
    }
}
