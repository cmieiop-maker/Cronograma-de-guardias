# Cronograma de Guardias — MVP

Aplicación web para administrar personas, feriados, restricciones y generar cronogramas de guardias equitativos.

## Requisitos
- Java 21+
- Maven 3.9+

No hace falta instalar SQLite: la aplicación crea `guardias.db` automáticamente.

## Ejecutar

Desde la raíz del proyecto:

```bash
mvn spring-boot:run
```

Abrir:

http://localhost:8080

También se puede empaquetar:

```bash
mvn clean package
java -jar target/cronograma-guardias-1.0.0.jar
```

## Funcionalidades incluidas

- Alta de personas.
- Baja lógica de personas.
- Alta y eliminación de feriados.
- Bloqueo de una persona para una fecha determinada.
- Detección de días hábiles, sábados, domingos y feriados.
- Generación automática del mes seleccionado.
- Penalización de guardias totales, fines de semana, feriados y consecutividad.
- Fallback cuando las restricciones hacen imposible evitar una guardia consecutiva.
- Edición manual de una guardia.
- Las guardias marcadas como manuales se conservan al regenerar.
- Estadísticas mensuales por persona.

## Nota sobre el algoritmo

La primera versión usa un algoritmo heurístico greedy: para cada fecha calcula un puntaje para las personas disponibles y elige la de menor carga. La ponderación da más peso a fines de semana, feriados y días consecutivos que a una guardia común.

Para una segunda versión se puede reemplazar por una optimización global (por ejemplo, programación lineal/constraint solving) para buscar la mejor solución de todo el mes en lugar de decidir fecha por fecha.
