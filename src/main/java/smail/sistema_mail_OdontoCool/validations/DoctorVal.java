package smail.sistema_mail_OdontoCool.validations;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smail.sistema_mail_OdontoCool.entities.Doctor;
import smail.sistema_mail_OdontoCool.repositories.DoctorRepository;

@Component
public class DoctorVal {

    @Autowired
    private DoctorRepository doctorRepository;

    public String insertValid(List<String> params) {
        // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
        // FNac[6], Exp[7], Matricula[8], TelfProf[9], EMAIL[10], PASSWORD[11]

        String msg = "";

        String CI = params.get(0);
        String Nombres = params.get(1);
        String Apellidos = params.get(2);
        String Dir = params.get(3);
        String Gen = params.get(4);
        String Telf = params.get(5);
        String FNac = params.get(6);
        String Exp = params.get(7);
        String Matricula = params.get(8);
        String TelfProf = params.get(9);
        String EMAIL = params.get(10);
        String PASSWORD = params.get(11);

        if (CI.isEmpty() || Nombres.isEmpty() || Apellidos.isEmpty() || Dir.isEmpty() || Gen.isEmpty() || Telf.isEmpty()
                || FNac.isEmpty() || Exp.isEmpty() || Matricula.isEmpty() || TelfProf.isEmpty() || EMAIL.isEmpty()
                || PASSWORD.isEmpty()) {
            msg += "Todos los campos son obligatorios.\n";
        }

        if (!CI.isEmpty() && !CI.matches("^[0-9]+$")) {
            msg += "La CI debe ser numérica: " + "(" + CI + ").\n";
        }

        if (!Nombres.isEmpty() && !Nombres.matches("^[A-Za-z ]+$")) {
            msg += "Los nombres deben ser solo letras: " + "(" + Nombres + ").\n";
        }

        if (!Apellidos.isEmpty() && !Apellidos.matches("^[A-Za-z ]+$")) {
            msg += "Los apellidos deben ser solo letras: " + "(" + Apellidos + ").\n";
        }

        if (!Dir.isEmpty() && !Dir.matches("^[A-Za-z0-9 ]+$")) {
            msg += "La dirección debe ser solo letras y números: " + "(" + Dir + ").\n";
        }

        if (!Gen.isEmpty() && !Gen.matches("^[F,M]$")) {
            msg += "El género debe ser F o M: " + "(" + Gen + ").\n";
        }

        if (!Telf.isEmpty() && !Telf.matches("^[0-9]+$")) {
            msg += "El teléfono debe ser solo números: " + "(" + Telf + ").\n";
        }

        if (!Telf.isEmpty() && Telf.length() != 8) {
            msg += "El teléfono debe tener 8 dígitos: " + "(" + Telf + ").\n";
        }

        if (!FNac.isEmpty() && !FNac.matches("^[0-9]{4}[-/][0-9]{2}[-/][0-9]{2}$")) {
            msg += "La fecha de nacimiento debe ser en formato yyyy-MM-dd o yyyy/MM/dd: " + "(" + FNac + ").\n";
        }

        if (!TelfProf.isEmpty() && !TelfProf.matches("^[0-9]+$")) {
            msg += "El teléfono profesional debe ser solo números: " + "(" + TelfProf + ").\n";
        }

        if (!TelfProf.isEmpty() && TelfProf.length() != 8) {
            msg += "El teléfono profesional debe tener 8 dígitos: " + "(" + TelfProf + ").\n";
        }

        if (!EMAIL.isEmpty() && !EMAIL.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            msg += "El correo electrónico debe ser válido: " + "(" + EMAIL + ").\n";
        }

        if (!PASSWORD.isEmpty() && PASSWORD.length() < 4) {
            msg += "La contraseña debe tener al menos 4 caracteres: " + "(" + PASSWORD + ").\n";
        }

        Doctor d = doctorRepository.findById(CI).orElse(null);
        if (d != null) {
            msg += "Ya existe un doctor registrado con el CI: " + "(" + CI + ").\n";
        }

        return msg;
    }

    public String updateValid(List<String> params) {

        String msg = "";

        if (params == null || params.isEmpty()) {
            return "No se recibieron parámetros.";
        }

        String CI = params.get(0);
        String Nombres = params.get(1);
        String Apellidos = params.get(2);
        String Dir = params.get(3);
        String Gen = params.get(4);
        String Telf = params.get(5);
        String FNac = params.get(6);
        // String Exp = params.get(7);
        // String Matricula = params.get(8);
        String TelfProf = params.get(9);
        String EMAIL = params.get(10);
        String PASSWORD = params.get(11);

        if (CI.isEmpty()) {
            msg += "La CI es obligatoria: " + "(" + CI + ").\n";
        }

        if (!CI.matches("^[0-9]+$")) {
            msg += "La CI debe ser numérica: " + "(" + CI + ").\n";
        }

        if (!Nombres.isEmpty() && !Nombres.matches("^[A-Za-z ]+$")) {
            msg += "Los nombres deben ser solo letras: " + "(" + Nombres + ").\n";
        }

        if (!Apellidos.isEmpty() && !Apellidos.matches("^[A-Za-z ]+$")) {
            msg += "Los apellidos deben ser solo letras: " + "(" + Apellidos + ").\n";
        }

        if (!Dir.isEmpty() && !Dir.matches("^[A-Za-z0-9 ]+$")) {
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
            msg += "La fecha de nacimiento debe ser en formato yyyy-MM-dd o yyyy/MM/dd: " + "(" + FNac + ").\n";
        }

        if (!TelfProf.isEmpty()) {
            if (!TelfProf.matches("^[0-9]+$")) {
                msg += "El teléfono profesional debe ser solo números: " + "(" + TelfProf + ").\n";
            } else if (TelfProf.length() != 8) {
                msg += "El teléfono profesional debe tener 8 dígitos: " + "(" + TelfProf + ").\n";
            }
        }

        if (!EMAIL.isEmpty() && !EMAIL.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            msg += "El correo electrónico debe ser válido: " + "(" + EMAIL + ").\n";
        }

        if (!PASSWORD.isEmpty() && PASSWORD.length() < 4) {
            msg += "La contraseña debe tener al menos 4 caracteres: " + "(" + PASSWORD + ").\n";
        }

        Doctor d = doctorRepository.findById(CI).orElse(null);
        if (d == null) {
            msg += "No existe un doctor con ese CI: " + "(" + CI + ").\n";
        }

        return msg;
    }
}
