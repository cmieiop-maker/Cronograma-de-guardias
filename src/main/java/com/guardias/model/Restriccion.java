package com.guardias.model;
import jakarta.persistence.*; import java.time.LocalDate;
@Entity @Table(name="restricciones", uniqueConstraints=@UniqueConstraint(columnNames={"persona_id","fecha"}))
public class Restriccion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.EAGER, optional=false) private Persona persona;
 @Column(nullable=false) private LocalDate fecha;
 private String motivo;
 public Restriccion(){} public Restriccion(Persona p,LocalDate f,String m){persona=p;fecha=f;motivo=m;}
 public Long getId(){return id;} public void setId(Long v){id=v;} public Persona getPersona(){return persona;} public void setPersona(Persona v){persona=v;} public LocalDate getFecha(){return fecha;} public void setFecha(LocalDate v){fecha=v;} public String getMotivo(){return motivo;} public void setMotivo(String v){motivo=v;}
}
