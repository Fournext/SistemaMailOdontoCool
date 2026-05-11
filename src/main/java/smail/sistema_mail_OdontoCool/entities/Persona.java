package smail.sistema_mail_OdontoCool.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "persona")
public class Persona {

    @Id
    private String ci;
    private String nombre;
    private String apellido;
    private String profesion;
    private String telefono;
    private String celular;
    private String email;

    public Persona() {}

    public Persona(String ci, String nombre, String apellido, String profesion, String telefono, String celular, String email) {
        this.ci = ci;
        this.nombre = nombre;
        this.apellido = apellido;
        this.profesion = profesion;
        this.telefono = telefono;
        this.celular = celular;
        this.email = email;
    }

    // Getters and Setters
    public String getCi() { return ci; }
    public void setCi(String ci) { this.ci = ci; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getProfesion() { return profesion; }
    public void setProfesion(String profesion) { this.profesion = profesion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s - %s", ci, nombre, apellido, email);
    }
}
