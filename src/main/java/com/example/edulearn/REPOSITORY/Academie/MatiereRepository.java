package com.example.edulearn.REPOSITORY.Academie;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Academie.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatiereRepository extends JpaRepository<Matiere,Integer> {
    @Query(value = "SELECT m FROM Matiere m JOIN m.categorieMatiere c JOIN c.section s WHERE s=:section ")
    List<Matiere> findMatiereBySection(Section section);
}
