package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class MailOrchestratorService {

    @Autowired
    private Pop3ClientService pop3Service;

    @Autowired
    private CommandProcessorService commandProcessor;

    @org.springframework.beans.factory.annotation.Value("${mail.smtp.user}")
    private String botEmail;

    // Se ejecuta cada 10 segundos
    @Scheduled(fixedRate = 10000)
    public void checkMails() {
        System.out.println("--- Revisando buzón de correo ---");
        try {
            List<Pop3ClientService.EmailMessage> emails = pop3Service.getEmails();

            for (Pop3ClientService.EmailMessage email : emails) {
                // Ignorar correos enviados por el propio bot para evitar bucles infinitos
                if (email.from != null && email.from.equalsIgnoreCase(botEmail)) {
                    System.out.println("Omitiendo correo propio: " + email.from);
                    continue;
                }

                System.out.println("Procesando correo de: " + email.from);
                System.out.println("Imagenes encontradas: " + email.imagenesBase64.size());

                // Procesar el comando basado en el Subject
                commandProcessor.process(email.subject, email.from, email.imagenesBase64);

                // Eliminar correo después de procesar para no repetirlo
                pop3Service.deleteMessage(email.uid);
            }

        } catch (IOException e) {
            System.err.println("Error al consultar el buzón: " + e.getMessage());
        }
    }
}
