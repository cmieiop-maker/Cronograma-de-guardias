package com.guardias.model;
import jakarta.persistence.*; import jakarta.validation.constraints.NotNull; import java.time.LocalDate;
@Entity @Table(name="feriados", uniqueConstraints=@UniqueConstraint(columnNames="fecha"))
public class Feriado {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotNull @Column(nullable=false) private LocalDate fecha;
 private String descripcion;
 public Feriado(){} public Feriado(LocalDate f,String d){fecha=f;descripcion=d;}
 public Long getId(){return id;} public void setId(Long v){id=v;} public LocalDate getFecha(){return fecha;} public void setFecha(LocalDate v){fecha=v;} public String getDescripcion(){return descripcion;} public void setDescripcion(String v){descripcion=v;}
}
