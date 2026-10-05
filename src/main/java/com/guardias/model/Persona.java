package com.guardias.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name="personas")
public class Persona {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String nombre;
    @NotBlank private String apellido;
    private boolean activa = true;
    public Persona() {}
    public Persona(String nombre, String apellido) { this.nombre=nombre; this.apellido=apellido; }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
    public String getApellido(){return apellido;} public void setApellido(String v){apellido=v;}
    public boolean isActiva(){return activa;} public void setActiva(boolean v){activa=v;}
    @Transient public String getNombreCompleto(){return nombre+" "+apellido;}
}
