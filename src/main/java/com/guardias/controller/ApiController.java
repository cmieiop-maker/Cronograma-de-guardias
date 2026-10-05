package com.guardias.controller;
import com.guardias.model.*; import com.guardias.repository.*; import com.guardias.service.GuardiaService; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api")
public class ApiController {
 private final PersonaRepository personas; private final FeriadoRepository feriados; private final RestriccionRepository restricciones; private final GuardiaRepository guardias; private final GuardiaService service;
 public ApiController(PersonaRepository p,FeriadoRepository f,RestriccionRepository r,GuardiaRepository g,GuardiaService s){personas=p;feriados=f;restricciones=r;guardias=g;service=s;}
 @GetMapping("/personas") public List<Persona> personas(){return personas.findAll();}
 @PostMapping("/personas") public Persona crear(@RequestBody Persona p){p.setId(null);return personas.save(p);}
 @PutMapping("/personas/{id}") public Persona editar(@PathVariable Long id,@RequestBody Persona x){Persona p=personas.findById(id).orElseThrow();p.setNombre(x.getNombre());p.setApellido(x.getApellido());p.setActiva(x.isActiva());return personas.save(p);}
 @DeleteMapping("/personas/{id}") public void eliminar(@PathVariable Long id){Persona p=personas.findById(id).orElseThrow();p.setActiva(false);personas.save(p);}
 @GetMapping("/feriados") public List<Feriado> feriados(@RequestParam String desde,@RequestParam String hasta){return feriados.findByFechaBetweenOrderByFechaAsc(LocalDate.parse(desde),LocalDate.parse(hasta));}
 @PostMapping("/feriados") public Feriado crearFeriado(@RequestBody Feriado f){f.setId(null);return feriados.save(f);}
 @DeleteMapping("/feriados/{id}") public void borrarFeriado(@PathVariable Long id){feriados.deleteById(id);}
 @GetMapping("/restricciones") public List<Restriccion> restricciones(@RequestParam String desde,@RequestParam String hasta){return restricciones.findByFechaBetweenOrderByFechaAsc(LocalDate.parse(desde),LocalDate.parse(hasta));}
 @PostMapping("/restricciones") public Restriccion crearRestriccion(@RequestBody Map<String,Object> b){Long pid=Long.valueOf(b.get("personaId").toString());Persona p=personas.findById(pid).orElseThrow();return restricciones.save(new Restriccion(p,LocalDate.parse(b.get("fecha").toString()),Objects.toString(b.get("motivo"),"")));}
 @DeleteMapping("/restricciones/{id}") public void borrarRestriccion(@PathVariable Long id){restricciones.deleteById(id);}
 @GetMapping("/cronograma") public List<Guardia> cronograma(@RequestParam int anio,@RequestParam int mes){return service.calendario(YearMonth.of(anio,mes));}
 @PostMapping("/cronograma/generar") public List<Guardia> generar(@RequestParam int anio,@RequestParam int mes,@RequestParam(defaultValue="true") boolean reemplazar){return service.generar(YearMonth.of(anio,mes),reemplazar);}
 @PutMapping("/cronograma/{fecha}") public Guardia manual(@PathVariable String fecha,@RequestBody Map<String,Object> b){return service.asignarManual(LocalDate.parse(fecha),Long.valueOf(b.get("personaId").toString()));}
 @GetMapping("/estadisticas") public Map<String,Object> estadisticas(@RequestParam int anio,@RequestParam int mes){return service.estadisticas(YearMonth.of(anio,mes));}
}
