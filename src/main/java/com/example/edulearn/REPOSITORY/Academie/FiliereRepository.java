package com.example.edulearn.REPOSITORY.Academie;

import com.example.edulearn.ENTITY.Academie.Filiere;
import com.example.edulearn.ENTITY.Academie.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FiliereRepository extends JpaRepository<Filiere,Integer> {
    List<Filiere> findBySection(Section section);
}
