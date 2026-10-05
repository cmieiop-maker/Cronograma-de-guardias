package com.guardias.repository; import com.guardias.model.Feriado; import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.*;
public interface FeriadoRepository extends JpaRepository<Feriado,Long>{Optional<Feriado> findByFecha(LocalDate fecha); List<Feriado> findByFechaBetweenOrderByFechaAsc(LocalDate desde,LocalDate hasta);}
