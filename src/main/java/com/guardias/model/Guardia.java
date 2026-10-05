package com.guardias.model;
import jakarta.persistence.*; import java.time.LocalDate;
@Entity @Table(name="guardias", uniqueConstraints=@UniqueConstraint(columnNames="fecha"))
public class Guardia {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private LocalDate fecha;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private TipoGuardia tipo;
 @ManyToOne(fetch=FetchType.EAGER, optional=false) private Persona persona;
 private boolean manual;
 public Guardia(){} public Guardia(LocalDate f,TipoGuardia t,Persona p,boolean m){fecha=f;tipo=t;persona=p;manual=m;}
 public Long getId(){return id;} public void setId(Long v){id=v;} public LocalDate getFecha(){return fecha;} public void setFecha(LocalDate v){fecha=v;} public TipoGuardia getTipo(){return tipo;} public void setTipo(TipoGuardia v){tipo=v;} public Persona getPersona(){return persona;} public void setPersona(Persona v){persona=v;} public boolean isManual(){return manual;} public void setManual(boolean v){manual=v;}
}
