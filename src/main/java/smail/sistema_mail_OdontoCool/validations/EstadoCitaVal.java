package smail.sistema_mail_OdontoCool.validations;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class EstadoCitaVal {
    public String insertValid(List<String> params) {
        String msg = "";

        String nombre = params.get(0);

        if (nombre.trim().isEmpty() || nombre == null) {
            msg += "- El nombre del estado no puede estar vacio.\n";
        }
        return msg;
    }

    public String updateValid(List<String> params) {
        String msg = "";

        if (params.get(0).trim().isEmpty() || params.get(0) == null) {
            msg += "- El ID del estado no puede estar vacio.\n";
            return msg;
        }

        String nombre = params.get(1);

        if (nombre.trim().isEmpty() || nombre == null) {
            msg += "- El nombre del estado no puede estar vacio.\n";
        }
        return msg;
    }
}
