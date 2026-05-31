package smail.sistema_mail_OdontoCool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SistemaMailOdontoCoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaMailOdontoCoolApplication.class, args);
    }

}
