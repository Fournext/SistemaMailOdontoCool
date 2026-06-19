package smail.sistema_mail_OdontoCool.validations;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.entities.Propietario;
import smail.sistema_mail_OdontoCool.repositories.PropietarioRepository;

@Component
public class PropietarioVal {
    @Autowired
    private PropietarioRepository propietarioRepository;

    public String insertValid(List<String> params) {
        // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
        // FNac[6], Porcentaje[7], EMAIL[8], PASSWORD[9]

        String msg = "";

        String CI = params.get(0);
        String Nombres = params.get(1);
        String Apellidos = params.get(2);
        String Dir = params.get(3);
        String Gen = params.get(4);
        String Telf = params.get(5);
        String FNac = params.get(6);
        String Porcentaje = params.get(7);
        String EMAIL = params.get(8);
        String PASSWORD = params.get(9);

        if (CI.isEmpty() || Nombres.isEmpty() || Apellidos.isEmpty() || Dir.isEmpty() || Gen.isEmpty() || Telf.isEmpty()
                || FNac.isEmpty() || Porcentaje.isEmpty() || EMAIL.isEmpty() || PASSWORD.isEmpty()) {
            msg += "Todos los campos son obligatorios.\n";
        }

        if (!CI.matches("^[0-9]+$")) {
            msg += "La CI debe ser numérica: " + "(" + CI + ").\n";
        }

        if (!Nombres.matches("^[A-Za-zÑñáéíóúÁÉÍÓÚüÜ ]+$")) {
            msg += "Los nombres deben ser solo letras: " + "(" + Nombres + ").\n";
        }

        if (!Apellidos.matches("^[A-Za-zÑñáéíóúÁÉÍÓÚüÜ ]+$")) {
            msg += "Los apellidos deben ser solo letras: " + "(" + Apellidos + ").\n";
        }

        if (!Dir.matches("^[A-Za-z0-9ÑñáéíóúÁÉÍÓÚüÜ ]+$")) {
            msg += "La dirección debe ser solo letras y números: " + "(" + Dir + ").\n";
        }

        if (!Gen.matches("^[F,M]$")) {
            msg += "El género debe ser F o M: " + "(" + Gen + ").\n";
        }

        if (!Telf.matches("^[0-9]+$")) {
            msg += "El teléfono debe ser solo números: " + "(" + Telf + ").\n";
        }

        if (Telf.length() != 8) {
            msg += "El teléfono debe tener 8 dígitos: " + "(" + Telf + ").\n";
        }

        if (!FNac.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}$")) {
            msg += "La fecha de nacimiento debe ser en formato yyyy-MM-dd: " + "(" + FNac + ").\n";
        }

        if (!Porcentaje.matches("^[0-9]*.[0-9]+$")) {
            msg += "El porcentaje debe ser numérico: " + "(" + Porcentaje + ").\n";
        }

        if (!EMAIL.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            msg += "El correo electrónico debe ser válido: " + "(" + EMAIL + ").\n";
        }

        if (PASSWORD.length() < 4) {
            msg += "La contraseña debe tener al menos 4 caracteres: " + "(" + PASSWORD + ").\n";
        }

        Propietario prop = propietarioRepository.findById(CI).orElse(null);
        if (prop != null) {
            msg += "Ya existe un propietario con ese CI: " + "(" + CI + ").\n";
        }

        return msg;
    }

    public String updateValid(List<String> params) {
        String msg = "";

        if (params == null || params.isEmpty()) {
            return "No se recibieron parámetros.\n";
        }

        String CI = params.get(0);
        String Nombres = params.size() > 1 ? params.get(1) : "";
        String Apellidos = params.size() > 2 ? params.get(2) : "";
        String Dir = params.size() > 3 ? params.get(3) : "";
        String Gen = params.size() > 4 ? params.get(4) : "";
        String Telf = params.size() > 5 ? params.get(5) : "";
        String FNac = params.size() > 6 ? params.get(6) : "";
        String Porcentaje = params.size() > 7 ? params.get(7) : "";
        String EMAIL = params.size() > 8 ? params.get(8) : "";
        String PASSWORD = params.size() > 9 ? params.get(9) : "";

        if (CI.isEmpty()) {
            msg += "La CI es obligatoria: " + "(" + CI + ").\n";
        }

        if (!CI.matches("^[0-9]+$")) {
            msg += "La CI debe ser numérica: " + "(" + CI + ").\n";
        }

        if (!Nombres.isEmpty() && !Nombres.matches("^[A-Za-zÑñáéíóúÁÉÍÓÚüÜ ]+$")) {
            msg += "Los nombres deben ser solo letras: " + "(" + Nombres + ").\n";
        }

        if (!Apellidos.isEmpty() && !Apellidos.matches("^[A-Za-zÑñáéíóúÁÉÍÓÚüÜ ]+$")) {
            msg += "Los apellidos deben ser solo letras: " + "(" + Apellidos + ").\n";
        }

        if (!Dir.isEmpty() && !Dir.matches("^[A-Za-z0-9ÑñáéíóúÁÉÍÓÚüÜ ]+$")) {
            msg += "La dirección debe ser solo letras y números: " + "(" + Dir + ").\n";
        }

        if (!Gen.isEmpty() && !Gen.matches("^[F,M]$")) {
            msg += "El género debe ser F o M: " + "(" + Gen + ").\n";
        }

        if (!Telf.isEmpty()) {
            if (!Telf.matches("^[0-9]+$")) {
                msg += "El teléfono debe ser solo números: " + "(" + Telf + ").\n";
            } else if (Telf.length() != 8) {
                msg += "El teléfono debe tener 8 dígitos: " + "(" + Telf + ").\n";
            }
        }

        if (!FNac.isEmpty() && !FNac.matches("^[0-9]{4}[-/][0-9]{2}[-/][0-9]{2}$")) {
            msg += "La fecha de nacimiento debe ser en formato yyyy-MM-dd o yyyy/MM/dd: "
                    + "(" + FNac + ").\n";
        }

        if (!Porcentaje.isEmpty() && !Porcentaje.matches("^[0-9]*.[0-9]+$")) {
            msg += "El porcentaje debe ser numérico: " + "(" + Porcentaje + ").\n";
        }

        if (!EMAIL.isEmpty() && !EMAIL.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            msg += "El correo electrónico debe ser válido: " + "(" + EMAIL + ").\n";
        }

        if (!PASSWORD.isEmpty() && PASSWORD.length() < 4) {
            msg += "La contraseña debe tener al menos 4 caracteres: " + "(" + PASSWORD + ").\n";
        }

        Propietario prop = propietarioRepository.findById(CI).orElse(null);
        if (prop == null) {
            msg += "No existe un propietario con ese CI: " + "(" + CI + ").\n";
        }

        return msg;
    }
}
