package com.guardias.repository; import com.guardias.model.Guardia; import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.*;
public interface GuardiaRepository extends JpaRepository<Guardia,Long>{List<Guardia> findByFechaBetweenOrderByFechaAsc(LocalDate desde,LocalDate hasta); Optional<Guardia> findByFecha(LocalDate fecha);}
