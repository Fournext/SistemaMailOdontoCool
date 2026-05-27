package smail.sistema_mail_OdontoCool.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        System.out.println("====== INICIANDO CLOUDINARY CONFIG ======");
        System.out.println("  cloudName: " + cloudName);
        System.out.println("  apiKey: " + (apiKey != null ? apiKey.replaceAll(".", "*") : "null"));
        System.out.println("  apiSecret: " + (apiSecret != null ? "Cargado (Oculto)" : "null"));
        System.out.println("=========================================");
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }
}