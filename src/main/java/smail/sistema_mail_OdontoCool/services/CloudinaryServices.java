package smail.sistema_mail_OdontoCool.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryServices {

    private final Cloudinary cloudinary;

    public CloudinaryServices(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    private String subirImagenDesdeBytes(byte[] imagenBytes, String nombreArchivo) throws IOException {
        Map<?, ?> resultado = cloudinary.uploader().upload(
                imagenBytes,
                ObjectUtils.asMap(
                        "folder", "imagenes_correo",
                        "resource_type", "image",
                        "public_id", limpiarNombre(nombreArchivo)));

        return resultado.get("secure_url").toString();
    }

    private String limpiarNombre(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return "imagen_correo_" + System.currentTimeMillis();
        }

        return nombreArchivo
                .replaceAll("\\.[^.]+$", "")
                .replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    public String subirImagen(String imagenBase64) {
        try {
            String base64Imagen = imagenBase64;
            byte[] imagenBytes = Base64.getMimeDecoder().decode(base64Imagen);
            String url = subirImagenDesdeBytes(
                    imagenBytes,
                    "imagen_correo" + UUID.randomUUID());
            return url;
        } catch (Exception e) {
            System.out.println("Error al subir la imagen: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}