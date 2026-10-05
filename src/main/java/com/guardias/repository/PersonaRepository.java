package com.guardias.repository; import com.guardias.model.Persona; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface PersonaRepository extends JpaRepository<Persona,Long>{List<Persona> findByActivaTrueOrderByApellidoAscNombreAsc();}
