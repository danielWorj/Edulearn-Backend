package com.example.edulearn.REPOSITORY.Academie;

import com.example.edulearn.ENTITY.Academie.Niveau;
import com.example.edulearn.ENTITY.Academie.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NiveauRepository extends JpaRepository<Niveau,Integer> {
    List<Niveau> findBySection(Section section);
}
