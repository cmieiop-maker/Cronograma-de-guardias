```java
package com.guardias.service;

import com.guardias.model.*;
import com.guardias.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GuardiaService {

    private final PersonaRepository personas;
    private final FeriadoRepository feriados;
    private final RestriccionRepository restricciones;
    private final GuardiaRepository guardias;

    public GuardiaService(
            PersonaRepository p,
            FeriadoRepository f,
            RestriccionRepository r,
            GuardiaRepository g) {
        personas = p;
        feriados = f;
        restricciones = r;
        guardias = g;
    }

    public List<Guardia> calendario(YearMonth ym) {
        return guardias.findByFechaBetweenOrderByFechaAsc(
                ym.atDay(1),
                ym.atEndOfMonth()
        );
    }

    @Transactional
    public List<Guardia> generar(YearMonth ym, boolean reemplazarNoManuales) {

        LocalDate ini = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        List<Persona> activas =
                personas.findByActivaTrueOrderByApellidoAscNombreAsc();

        if (activas.isEmpty()) {
            throw new IllegalStateException("No hay personas activas.");
        }

        Map<LocalDate, Guardia> existentes =
                guardias.findByFechaBetweenOrderByFechaAsc(ini, fin)
                        .stream()
                        .collect(Collectors.toMap(
                                Guardia::getFecha,
                                g -> g
                        ));

        if (reemplazarNoManuales) {
            existentes.values()
                    .stream()
                    .filter(g -> !g.isManual())
                    .forEach(guardias::delete);
        }

        Set<LocalDate> feriadosSet =
                feriados.findByFechaBetweenOrderByFechaAsc(ini, fin)
                        .stream()
                        .map(Feriado::getFecha)
                        .collect(Collectors.toSet());

        Set<String> bloqueos =
                restricciones.findByFechaBetweenOrderByFechaAsc(ini, fin)
                        .stream()
                        .map(r -> r.getPersona().getId() + "|" + r.getFecha())
                        .collect(Collectors.toSet());

        Map<Long, Integer> total = new HashMap<>();
        Map<Long, Integer> finSemana = new HashMap<>();
        Map<Long, Integer> feriado = new HashMap<>();

        for (Persona p : activas) {
            total.put(p.getId(), 0);
            finSemana.put(p.getId(), 0);
            feriado.put(p.getId(), 0);
        }

        // Cuenta todo el cronograma existente del mes, incluidos manuales.
        for (Guardia g :
                guardias.findByFechaBetweenOrderByFechaAsc(ini, fin)) {
            add(g, total, finSemana, feriado);
        }

        for (LocalDate d = ini; !d.isAfter(fin); d = d.plusDays(1)) {

            Guardia manual = guardias.findByFecha(d).orElse(null);

            if (manual != null && manual.isManual()) {
                continue;
            }

            TipoGuardia tipo = tipoDe(d, feriadosSet);

            List<Persona> candidatos = activas.stream()
                    .filter(p -> !bloqueos.contains(
                            p.getId() + "|" + d))
                    .filter(p -> !tieneGuardiaAnterior(p, d))
                    .toList();

            if (candidatos.isEmpty()) {
                candidatos = activas.stream()
                        .filter(p -> !bloqueos.contains(
                                p.getId() + "|" + d))
                        .toList();
            }

            if (candidatos.isEmpty()) {
                throw new IllegalStateException(
                        "No hay una persona disponible para " + d + "."
                );
            }

            // Variables locales finales para evitar el error de lambda.
            final LocalDate fechaActual = d;
            final TipoGuardia tipoActual = tipo;

            Persona elegido = candidatos.stream()
                    .min(Comparator.comparingDouble(
                            p -> puntaje(
                                    p,
                                    fechaActual,
                                    tipoActual,
                                    total,
                                    finSemana,
                                    feriado
                            )
                    ))
                    .orElseThrow();

            Guardia g = guardias.findByFecha(d)
                    .orElse(new Guardia());

            g.setFecha(d);
            g.setTipo(tipo);
            g.setPersona(elegido);
            g.setManual(false);

            guardias.save(g);

            total.put(
                    elegido.getId(),
                    total.get(elegido.getId()) + 1
            );

            if (tipo == TipoGuardia.FIN_DE_SEMANA) {
                finSemana.put(
                        elegido.getId(),
                        finSemana.get(elegido.getId()) + 1
                );
            }

            if (tipo == TipoGuardia.FERIADO) {
                feriado.put(
                        elegido.getId(),
                        feriado.get(elegido.getId()) + 1
                );
            }
        }

        return calendario(ym);
    }

    private void add(
            Guardia g,
            Map<Long, Integer> t,
            Map<Long, Integer> fs,
            Map<Long, Integer> f) {

        t.computeIfPresent(
                g.getPersona().getId(),
                (k, v) -> v + 1
        );

        if (g.getTipo() == TipoGuardia.FIN_DE_SEMANA) {
            fs.computeIfPresent(
                    g.getPersona().getId(),
                    (k, v) -> v + 1
            );
        }

        if (g.getTipo() == TipoGuardia.FERIADO) {
            f.computeIfPresent(
                    g.getPersona().getId(),
                    (k, v) -> v + 1
            );
        }
    }

    private double puntaje(
            Persona p,
            LocalDate d,
            TipoGuardia tipo,
            Map<Long, Integer> t,
            Map<Long, Integer> fs,
            Map<Long, Integer> f) {

        double s = t.get(p.getId()) * 10.0;

        s += fs.get(p.getId()) *
                (tipo == TipoGuardia.FIN_DE_SEMANA ? 18 : 4);

        s += f.get(p.getId()) *
                (tipo == TipoGuardia.FERIADO ? 18 : 4);

        if (tieneGuardiaAnterior(d.minusDays(1), p)) {
            s += 35;
        }

        if (tieneGuardiaAnterior(d.minusDays(2), p)) {
            s += 18;
        }

        return s + ((p.getId() % 7) * 0.001);
    }

    private boolean tieneGuardiaAnterior(
            LocalDate d,
            Persona p) {

        return guardias.findByFecha(d)
                .map(g -> g.getPersona().getId().equals(p.getId()))
                .orElse(false);
    }

    private boolean tieneGuardiaAnterior(
            Persona p,
            LocalDate d) {

        return tieneGuardiaAnterior(d.minusDays(1), p);
    }

    private TipoGuardia tipoDe(
            LocalDate d,
            Set<LocalDate> feriados) {

        if (feriados.contains(d)) {
            return TipoGuardia.FERIADO;
        }

        DayOfWeek x = d.getDayOfWeek();

        return (x == DayOfWeek.SATURDAY ||
                x == DayOfWeek.SUNDAY)
                ? TipoGuardia.FIN_DE_SEMANA
                : TipoGuardia.SEMANA;
    }

    @Transactional
    public Guardia asignarManual(
            LocalDate fecha,
            Long personaId) {

        Persona p = personas.findById(personaId)
                .orElseThrow();

        if (!p.isActiva()) {
            throw new IllegalArgumentException(
                    "La persona está inactiva."
            );
        }

        if (restricciones.existsByPersonaIdAndFecha(
                personaId,
                fecha)) {

            throw new IllegalArgumentException(
                    "La persona está bloqueada para esa fecha."
            );
        }

        TipoGuardia t = tipoDe(
                fecha,
                feriados.findByFecha(fecha)
                        .map(Feriado::getFecha)
                        .map(Set::of)
                        .orElse(Set.of())
        );

        Guardia g = guardias.findByFecha(fecha)
                .orElse(new Guardia());

        g.setFecha(fecha);
        g.setTipo(t);
        g.setPersona(p);
        g.setManual(true);

        return guardias.save(g);
    }

    @Transactional
    public void borrarCronograma(YearMonth ym) {

        guardias.findByFechaBetweenOrderByFechaAsc(
                ym.atDay(1),
                ym.atEndOfMonth()
        )
        .stream()
        .filter(g -> !g.isManual())
        .forEach(guardias::delete);
    }

    public Map<String, Object> estadisticas(YearMonth ym) {

        List<Persona> ps = personas.findAll();
        List<Guardia> gs = calendario(ym);

        List<Map<String, Object>> out = new ArrayList<>();

        for (Persona p : ps) {

            long total = gs.stream()
                    .filter(g -> g.getPersona().getId().equals(p.getId()))
                    .count();

            long semana = gs.stream()
                    .filter(g ->
                            g.getPersona().getId().equals(p.getId()) &&
                            g.getTipo() == TipoGuardia.SEMANA)
                    .count();

            long fs = gs.stream()
                    .filter(g ->
                            g.getPersona().getId().equals(p.getId()) &&
                            g.getTipo() == TipoGuardia.FIN_DE_SEMANA)
                    .count();

            long fer = gs.stream()
                    .filter(g ->
                            g.getPersona().getId().equals(p.getId()) &&
                            g.getTipo() == TipoGuardia.FERIADO)
                    .count();

            Map<String, Object> m = new LinkedHashMap<>();

            m.put("personaId", p.getId());
            m.put("persona", p.getNombreCompleto());
            m.put("semana", semana);
            m.put("finDeSemana", fs);
            m.put("feriados", fer);
            m.put("total", total);

            out.add(m);
        }

        Map<String, Object> r = new LinkedHashMap<>();

        r.put("mes", ym.toString());
        r.put("personas", out);

        return r;
    }
}
```
