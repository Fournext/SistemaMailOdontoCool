package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.DetalleDiagnostico;
import smail.sistema_mail_OdontoCool.entities.Diagnostico;
import smail.sistema_mail_OdontoCool.entities.HistorialClinico;
import smail.sistema_mail_OdontoCool.entities.RecetaRecomendacion;
import smail.sistema_mail_OdontoCool.entities.ResultadoAnalisis;
import smail.sistema_mail_OdontoCool.entities.ServicioPrestado;
import smail.sistema_mail_OdontoCool.entities.SolicitudAnalisis;
import smail.sistema_mail_OdontoCool.entities.Tratamiento;
import smail.sistema_mail_OdontoCool.entities.TratamientoDiente;
import smail.sistema_mail_OdontoCool.repositories.DetalleDiagnosticoRepository;
import smail.sistema_mail_OdontoCool.repositories.DiagnosticoRepository;
import smail.sistema_mail_OdontoCool.repositories.HistorialClinicoRepository;
import smail.sistema_mail_OdontoCool.repositories.RecetaRecomendacionRepository;
import smail.sistema_mail_OdontoCool.repositories.ServicioPrestadoRepository;
import smail.sistema_mail_OdontoCool.repositories.SolicitudAnalisisRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoDienteRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.validations.TratamientoVal;

@Service
public class TratamientoService {

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private ServicioPrestadoRepository servicioPrestadoRepository;

    @Autowired
    private TratamientoDienteRepository tratamientoDienteRepository;

    @Autowired
    private SolicitudAnalisisRepository solicitudAnalisisRepository;

    @Autowired
    private RecetaRecomendacionRepository recetaRecomendacionRepository;

    @Autowired
    private DiagnosticoRepository diagnosticoRepository;

    @Autowired
    private DetalleDiagnosticoRepository detalleDiagnosticoRepository;

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private TratamientoVal tratamientoVal;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void handle(String action, List<String> params, String fromEmail) {
        switch (action) {
            case "INS":
                insert(params, fromEmail);
                break;
            case "BUS":
                buscar(params, fromEmail);
                break;
            case "LIS":
                list(params, fromEmail);
                break;
            case "MOD":
                update(params, fromEmail);
                break;
            case "DEL":
                delete(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no permitida para tratamientos.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Verificar si es Doctor
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            String validationMsg = tratamientoVal.insertValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            String historialId = params.get(6).trim();
            String diagnosticoId = params.get(7).trim();

            HistorialClinico historialClinico = historialClinicoRepository.findById(historialId)
                    .orElseThrow(() -> new RuntimeException("Historial clínico no encontrado: " + historialId));
            Diagnostico diagnostico = diagnosticoRepository.findById(Long.parseLong(diagnosticoId))
                    .orElseThrow(() -> new RuntimeException("Diagnóstico no encontrado con ID: " + diagnosticoId));

            Tratamiento tratamiento = new Tratamiento();
            tratamiento.setObjetivoTratamiento(params.get(0).trim());
            tratamiento.setObservacion(params.get(1).trim());
            tratamiento.setEstado(params.get(2).trim().toUpperCase());
            tratamiento.setFechaInicio(LocalDate.parse(params.get(3).trim().replace('/', '-')));
            tratamiento.setFechaFin(LocalDate.parse(params.get(4).trim().replace('/', '-')));
            tratamiento.setFechaFinReal(LocalDate.parse(params.get(5).trim().replace('/', '-')));
            tratamiento.setHistorialClinico(historialClinico);
            tratamiento.setDiagnostico(diagnostico);

            tratamientoRepository.save(tratamiento);

            sendResponse(fromEmail, "Éxito",
                    "Tratamiento registrado correctamente con ID: " + tratamiento.getId());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un tratamiento: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            String validationMsg = tratamientoVal.listValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            String parametro = params.get(0).trim();
            StringBuilder sb;

            if ("*".equals(parametro)) {
                // Verificar si es Doctor
                boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
                if (!exists) {
                    sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                    return;
                }
                sb = listAll();
            } else {
                // Verificar si es Doctor o paciente
                boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR")
                        || usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PACIENTE");
                if (!exists) {
                    sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                    return;
                }
                String historialId = parametro.substring("Historial:".length()).trim();
                sb = findByHistorialClinico(historialId);
            }

            sendResponse(fromEmail, "Listado de Tratamientos", sb.toString());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar tratamientos: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // Verificar si es Doctor
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            String validationMsg = tratamientoVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            Tratamiento tratamiento = tratamientoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tratamiento con ID: " + id + " no encontrado."));

            String objetivoTratamiento = params.get(1).trim();
            String observacion = params.get(2).trim();
            String estado = params.get(3).trim();
            String fechaInicioTexto = params.get(4).trim();
            String fechaFinTexto = params.get(5).trim();
            String fechaFinRealTexto = params.get(6).trim();
            String codigoHistorial = params.get(7).trim();
            String codigoDiagnostico = params.get(8).trim();

            if (!codigoDiagnostico.isEmpty()) {
                Diagnostico diagnostico = diagnosticoRepository.findById(Long.parseLong(codigoDiagnostico))
                        .orElseThrow(
                                () -> new RuntimeException("Diagnóstico no encontrado con ID: " + codigoDiagnostico));
                tratamiento.setDiagnostico(diagnostico);
            }
            if (!codigoHistorial.isEmpty()) {
                HistorialClinico historialClinico = historialClinicoRepository.findById(codigoHistorial)
                        .orElseThrow(() -> new RuntimeException("Historial clínico no encontrado: " + codigoHistorial));
                tratamiento.setHistorialClinico(historialClinico);
            }

            if (!objetivoTratamiento.isEmpty()) {
                tratamiento.setObjetivoTratamiento(objetivoTratamiento);
            }

            if (!observacion.isEmpty()) {
                tratamiento.setObservacion(observacion);
            }

            if (!estado.isEmpty()) {
                tratamiento.setEstado(estado.toUpperCase());
            }

            if (!fechaInicioTexto.isEmpty()) {
                tratamiento.setFechaInicio(LocalDate.parse(fechaInicioTexto.replace('/', '-')));
            }

            if (!fechaFinTexto.isEmpty()) {
                tratamiento.setFechaFin(LocalDate.parse(fechaFinTexto.replace('/', '-')));
            }

            if (!fechaFinRealTexto.isEmpty()) {
                tratamiento.setFechaFinReal(LocalDate.parse(fechaFinRealTexto.replace('/', '-')));
            }

            tratamientoRepository.save(tratamiento);

            sendResponse(fromEmail, "Éxito",
                    "Tratamiento: " + tratamiento.getId() + " actualizado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar tratamiento: " + e.getMessage());
        }
    }

    @Transactional
    private void delete(List<String> params, String fromEmail) {
        try {
            // Verificar si es Doctor
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            String validationMsg = tratamientoVal.deleteValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            Tratamiento tratamiento = tratamientoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tratamiento con ID: " + id + " no encontrado."));

            tratamiento.setEstado("INACTIVO");
            tratamientoRepository.save(tratamiento);

            sendResponse(fromEmail, "Éxito",
                    "Tratamiento con ID: " + id + " desactivado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar tratamiento: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Tratamiento> tratamientos = tratamientoRepository.findAll();
        StringBuilder sb = new StringBuilder("Listado de Tratamientos\n");

        if (tratamientos.isEmpty()) {
            sb.append("- No hay tratamientos registrados.\n");
            return sb;
        }

        for (Tratamiento tratamiento : tratamientos) {
            sb.append(formatearTratamiento(tratamiento));
        }

        return sb;
    }

    private StringBuilder findByHistorialClinico(String historialId) {
        List<Tratamiento> tratamientos = tratamientoRepository.findByHistorialClinicoCodigoHistorial(historialId);

        StringBuilder sb = new StringBuilder();
        sb.append("Tratamientos para Historial Clínico ID: ")
                .append(historialId)
                .append("\n");

        if (tratamientos.isEmpty()) {
            sb.append("- No se encontraron tratamientos para este historial.\n");
            return sb;
        }

        for (Tratamiento tratamiento : tratamientos) {
            sb.append(formatearTratamiento(tratamiento));
        }

        return sb;
    }

    private void buscar(List<String> params, String fromEmail) {
        try {
            // Verificar si es Doctor o Paciente
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR")
                    || usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PACIENTE");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            String validationMsg = tratamientoVal.buscarValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            Tratamiento tratamiento = tratamientoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tratamiento con ID: " + id + " no encontrado."));

            List<ServicioPrestado> serviciosPrestados = servicioPrestadoRepository.findByTratamientoId(id);

            List<TratamientoDiente> tratamientosDientes = tratamientoDienteRepository.findByTratamientoId(id);

            List<SolicitudAnalisis> solicitudesAnalisis = solicitudAnalisisRepository
                    .findByTratamientoIdConResultado(id);

            List<RecetaRecomendacion> recetas = recetaRecomendacionRepository.findByTratamientoId(id);
            Diagnostico diagnostico = tratamiento.getDiagnostico();
            List<DetalleDiagnostico> detalleDiagnostico = detalleDiagnosticoRepository
                    .findByDiagnosticoId(diagnostico.getId());

            StringBuilder sb = new StringBuilder();

            sb.append(formatearDetalleTratamiento(
                    tratamiento,
                    serviciosPrestados,
                    tratamientosDientes,
                    solicitudesAnalisis,
                    recetas,
                    diagnostico,
                    detalleDiagnostico));

            sendResponse(fromEmail, "Detalle del Tratamiento", sb.toString());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al buscar tratamiento: " + e.getMessage());
        }
    }

    private String formatearTratamiento(Tratamiento tratamiento) {
        return String.format(
                "- [%s] Objetivo: %s | Observación: %s | Estado: %s | Fecha Inicio: %s | Fecha Fin: %s | Fecha Fin Real: %s\n",
                tratamiento.getId(),
                tratamiento.getObjetivoTratamiento(),
                tratamiento.getObservacion(),
                tratamiento.getEstado(),
                tratamiento.getFechaInicio(),
                tratamiento.getFechaFin(),
                tratamiento.getFechaFinReal());
    }

    private String formatearDetalleTratamiento(
            Tratamiento tratamiento,
            List<ServicioPrestado> serviciosPrestados,
            List<TratamientoDiente> tratamientosDientes,
            List<SolicitudAnalisis> solicitudesAnalisis,
            List<RecetaRecomendacion> recetas,
            Diagnostico diagnostico,
            List<DetalleDiagnostico> detalleDiagnostico) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                "Tratamiento [%s]\nObjetivo: %s\nObservación: %s\nEstado: %s\nFecha Inicio: %s\nFecha Fin: %s\nFecha Fin Real: %s\n\n",
                tratamiento.getId(),
                tratamiento.getObjetivoTratamiento(),
                tratamiento.getObservacion(),
                tratamiento.getEstado(),
                tratamiento.getFechaInicio(),
                tratamiento.getFechaFin(),
                tratamiento.getFechaFinReal()));

        sb.append("Servicios Prestados:\n");
        if (serviciosPrestados.isEmpty()) {
            sb.append("- Sin servicios prestados registrados.\n");
        } else {
            for (ServicioPrestado sp : serviciosPrestados) {
                sb.append(String.format(
                        "- [%s] Servicio: %s | Cantidad: %s | Precio: %s | Subtotal: %s | Estado: %s\n",
                        sp.getId(),
                        sp.getServicio() != null ? sp.getServicio().getNombre() : "N/A",
                        sp.getCantidad(),
                        sp.getPrecio(),
                        sp.getSubtotal(),
                        sp.getEstado()));
            }
        }

        sb.append("\nTratamientos de Dientes:\n");
        if (tratamientosDientes.isEmpty()) {
            sb.append("- Sin tratamientos de dientes registrados.\n");
        } else {
            for (TratamientoDiente td : tratamientosDientes) {
                sb.append(String.format(
                        "- [%s] Diente: %s | Cara: %s | Planificado: %s | Estado: %s\n",
                        td.getId(),
                        td.getDiente() != null ? td.getDiente().getNumero() : "N/A",
                        td.getCaraDental(),
                        td.getTratamientoPlanificado(),
                        td.getEstado()));
            }
        }

        sb.append("\nSolicitudes de Análisis:\n");
        if (solicitudesAnalisis.isEmpty()) {
            sb.append("- Sin solicitudes de análisis registradas.\n");
        } else {
            for (SolicitudAnalisis sa : solicitudesAnalisis) {
                sb.append(String.format(
                        "- Solicitud [%s] Análisis: %s | Motivo: %s | Estado: %s\n",
                        sa.getId(),
                        sa.getAnalisis() != null ? sa.getAnalisis().getNombre() : "N/A",
                        sa.getMotivo(),
                        sa.getEstado()));

                ResultadoAnalisis r = sa.getResultadoAnalisis();

                if (r != null) {
                    sb.append(String.format(
                            "  Resultado [%s]: %s | Observaciones: %s | Interpretación: %s | Archivo: %s\n",
                            r.getId(),
                            r.getResultado(),
                            r.getObservaciones(),
                            r.getInterpretacion(),
                            r.getArchivoAdjunto()));
                } else {
                    sb.append("  Resultado: Pendiente\n");
                }
            }
        }
        // vamos a agregar los detalles al diagnostico
        if (diagnostico != null) {
            sb.append("\nDiagnóstico:\n");
            sb.append(String.format(
                    "- Diagnóstico [%s] Síntomas: %s | Tipo: %s | Gravedad: %s | Observaciones: %s\n",
                    diagnostico.getId(),
                    diagnostico.getSintomas(),
                    diagnostico.getTipoDiagnostico(),
                    diagnostico.getGravedad(),
                    diagnostico.getObservaciones()));
            for (DetalleDiagnostico detalle : detalleDiagnostico) {
                sb.append(String.format(
                        "  Detalle [%s] Observación: %s | Zona Bucal: %s | Diente: %s\n",
                        detalle.getId(),
                        detalle.getObservacion(),
                        detalle.getZonaBucal(),
                        detalle.getDiente() != null ? detalle.getDiente().getNumero() : "N/A"));
            }
        } else {
            sb.append("\nDiagnóstico: No registrado\n");
        }

        sb.append("\nRecetas/Recomendaciones:\n");
        if (recetas.isEmpty()) {
            sb.append("- Sin recetas/recomendaciones registradas.\n");
        } else {
            for (RecetaRecomendacion receta : recetas) {
                sb.append(String.format(
                        "- [%s] Fecha: %s | Observaciones: %s\n",
                        receta.getId(),
                        receta.getFechaEmision(),
                        receta.getObservaciones()));
            }
        }

        return sb.toString();
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }
}
