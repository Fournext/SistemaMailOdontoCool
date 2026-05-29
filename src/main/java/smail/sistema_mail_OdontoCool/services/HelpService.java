package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelpService {

    @Autowired
    private SmtpClientService smtpService;

    public void sendHelp(String to, String messageError) {
        String message = ""
                + messageError + "\n"
                + "========================================\n"
                + "AYUDA - SISTEMA ODONTOCOOL (CORREO)\n"
                + "========================================\n\n"
                + "FORMATO GENERAL DE COMANDOS:\n"
                + "  COMANDO[\"param1\", \"param2\", ...]\n\n"
                + "1. DOCTORES (DOC):\n"
                + "   - INSDOC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"Exp\", \"Matrícula\", \"TelfProf\", \"Correo\", \"Contraseña\"]\n"
                + "        Ejemplo: INSDOC[\"12345678\", \"Juan\", \"Perez\", \"Calle Falsa 123\", \"M\", \"55512348\", \"1980-01-01\", \"10 años\", \"MAT-001\", \"77777777\", \"juan@gmail.com\", \"p123\"]\n"
                + "        *Nota: Se debe adjuntar una imagen al correo para registrar la foto de perfil.*\n"
                + "   - MODDOC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"Exp\", \"Matrícula\", \"TelfProf\", \"Correo\", \"Contraseña\"]\n"
                + "        Ejemplo: MODDOC[\"12345678\", \"Juan Carlos\", \"Perez\", \"Calle Falsa 124\", \"M\", \"55512348\", \"1980-01-01\", \"12 años\", \"MAT-001\", \"77777777\", \"juan@gmail.com\", \"p123new\"]\n"
                + "        *Nota: Opcionalmente se puede adjuntar una nueva imagen para cambiar la foto de perfil.*\n"
                + "   - DELDOC[\"CI\"]\n"
                + "        Ejemplo: DELDOC[\"12345678\"]\n"
                + "   - LISDOC[*] -> Listar todos los doctores.\n\n"
                + "   - ASEDOC[\"CI Doctor\", \"UUID Especialidad\"] -> Asignar una especialidad a un doctor.\n"
                + "2. PACIENTES (PAC):\n"
                + "   - INSPAC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"ContactoEmerg\", \"TelfEmerg\", \"Correo\", \"Contraseña\"]\n"
                + "        Ejemplo: INSPAC[\"87654321\", \"Maria\", \"Gomez\", \"Av. Siempre Viva 456\", \"F\", \"55512348\", \"1990-02-02\", \"Carlos Gomez\", \"555-8765\", \"maria@gmail.com\", \"m456\"]\n"
                + "        *Nota: Se debe adjuntar una imagen al correo para registrar la foto de perfil.*\n"
                + "   - MODPAC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"ContactoEmerg\", \"TelfEmerg\", \"Correo\", \"Contraseña\"]\n"
                + "        Ejemplo: MODPAC[\"87654321\", \"Maria Luz\", \"Gomez\", \"Av. Siempre Viva 457\", \"F\", \"55512348\", \"1990-02-02\", \"Carlos Gomez\", \"555-8765\", \"maria.g@gmail.com\", \"m456new\"]\n"
                + "   - DELPAC[\"CI\"]\n"
                + "        Ejemplo: DELPAC[\"87654321\"]\n"
                + "   - LISPAC[*] -> Listar todos los pacientes.\n\n"
                + "3. SECRETARIAS (SEC):\n"
                + "   - INSSEC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"FechaContratacion\", \"Correo\", \"Contraseña\"]\n"
                + "        Ejemplo: INSSEC[\"11223344\", \"Ana\", \"Lopez\", \"Calle Luna 789\", \"F\", \"555-4321\", \"1985-03-03\", \"2010-05-01\", \"ana@gmail.com\", \"a789\"]\n"
                + "        *Nota: Se debe adjuntar una imagen al correo para registrar la foto de perfil.*\n"
                + "   - MODSEC[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"FechaContratacion\", \"Correo\", \"Contraseña\"]\n"
                + "        Ejemplo: MODSEC[\"11223344\", \"Ana Maria\", \"Lopez\", \"Calle Sol 123\", \"F\", \"55512348\", \"1985-03-03\", \"2010-05-01\", \"ana.l@gmail.com\", \"a789new\"]\n"
                + "   - DELSEC[\"CI\"]\n"
                + "        Ejemplo: DELSEC[\"11223344\"]\n"
                + "   - LISSEC[*] -> Listar todas las secretarias.\n\n"
                + "4. PROPIETARIOS (PRO):\n"
                + "   - INSPRO[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"Porcentaje\", \"Correo\", \"Contraseña\"]\n"
                + "        Ejemplo: INSPRO[\"55667788\", \"Luis\", \"Martinez\", \"Av. Sol 321\", \"M\", \"555-6789\", \"1975-04-04\", \"25.5\", \"luis@gmail.com\", \"l012\"]\n"
                + "        *Nota: Se debe adjuntar una imagen al correo para registrar la foto de perfil.*\n"
                + "   - MODPRO[\"CI\", \"Nombres\", \"Apellidos\", \"Direccion\", \"Genero\", \"Telf\", \"FechaNacimiento\", \"Porcentaje\", \"Correo\", \"Contraseña\"]\n"
                + "        Ejemplo: MODPRO[\"55667788\", \"Luis Alberto\", \"Martinez\", \"Av. Sol 322\", \"M\", \"55512348\", \"1975-04-04\", \"30.0\", \"luis.m@gmail.com\", \"l012new\"]\n"
                + "   - DELPRO[\"CI\"]\n"
                + "        Ejemplo: DELPRO[\"55667788\"]\n"
                + "   - LISPRO[*] -> Listar todos los propietarios.\n\n"
                + "5. ESPECIALIDADES (ESP):\n"
                + "   - INSESP[\"Nombre\", \"Descripcion\",\"estado\"]\n"
                + "   - LISESP[*] -> Listar todas las especialidades.\n\n"
                + "6. HISTORIALES CLÍNICOS (HIS):\n"
                + "   - INSHIS[\"CI_Paciente\", \"Alergias\", \"AntecedentesMedicos\", \"EnfermedadesBase\", \"MotivoApertura\", \"ObservacionesGenerales\"]\n"
                + "        Ejemplo: INSHIS[\"12345678\", \"Ninguna\", \"Operación de apéndice\", \"Ninguna\", \"Dolor de muela\", \"Paciente requiere cuidado especial\"]\n"
                + "   - MODHIS[\"CodigoHistorial\", \"Alergias\", \"AntecedentesMedicos\", \"EnfermedadesBase\", \"MotivoApertura\", \"ObservacionesGenerales\"]\n"
                + "        Ejemplo: MODHIS[\"12345678P\", \"Penicilina\", \"\", \"\", \"\", \"\"]\n"
                + "   - DELHIS[\"CodigoHistorial\"]\n"
                + "   - LISHIS[*] -> Listar todos los historiales clínicos.\n\n"
                + "7. CITAS (CIT):\n"
                + "   - INSCIT[\"FechaCita\", \"HoraInicio\", \"HoraFin\", \"Motivo\", \"Observacion\", \"CI_Secretaria\", \"CI_Paciente\", \"CodigoHistorial\"]\n"
                + "        Ejemplo: INSCIT[\"2026-06-01\", \"09:00\", \"09:30\", \"Consulta General\", \"Ninguna\", \"11223344\", \"87654321\", \"87654321G\"]\n"
                + "   - MODCIT[\"IdCita\", \"FechaCita\", \"HoraInicio\", \"HoraFin\", \"Motivo\", \"Observacion\", \"CI_Secretaria\", \"CI_Paciente\", \"CodigoHistorial\"]\n"
                + "        Ejemplo: MODCIT[\"1\", \"2026-06-01\", \"10:00\", \"10:30\", \"\", \"\", \"\", \"\", \"\"]\n"
                + "   - DELCIT[\"IdCita\"]\n"
                + "   - LISCIT[*] -> Listar todas las citas.\n\n"
                + "8. ESTADOS DE CITA (ECI):\n"
                + "   - INSECI[\"Nombre\", \"Descripcion\"]\n"
                + "        Ejemplo: INSECI[\"CONFIRMADA\", \"La cita ha sido confirmada por el paciente\"]\n"
                + "   - MODECI[\"IdEstadoCita\", \"Nombre\", \"Descripcion\"]\n"
                + "        Ejemplo: MODECI[\"1\", \"CANCELADA\", \"Cita cancelada\"]\n"
                + "   - DELECI[\"IdEstadoCita\"]\n"
                + "   - LISECI[*] -> Listar todos los estados de cita.\n"
                + "   - ASEECI[\"IdCita\", \"NombreEstado\", \"Observaciones\"] -> Asignar un estado a una cita.\n"
                + "        Ejemplo: ASEECI[\"1\", \"CONFIRMADA\", \"El paciente confirmó por llamada\"]\n"
                + "========================================\n"
                + "NOTA: Las fechas deben ir en formato AAAA-MM-DD (Ej: 1995-05-15).\n"
                + "(El porcentaje es un número decimal que representa la participación del propietario en la clínica, por ejemplo, 25 para un 25% de participación)"
                + "(El código de usuario es generado automáticamente por el sistema, no es necesario enviarlo, primera letra del apellido mayuscula + CI)"
                + "(La foto se carga mediante un archivo adjunto al correo)"
                + "========================================\n";

        try {
            smtpService.sendEmail(to, "Manual de Comandos - OdontoCool", message);
        } catch (IOException e) {
            System.err.println("Error al enviar ayuda: " + e.getMessage());
        }
    }
}
