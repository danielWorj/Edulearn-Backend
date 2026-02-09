package com.example.edulearn.REPOSITORY.Academie;

import com.example.edulearn.ENTITY.Academie.CategorieMatiere;
import com.example.edulearn.ENTITY.Academie.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategorieMatiereRepository extends JpaRepository<CategorieMatiere,Integer> {

    List<CategorieMatiere> findBySection(Section section);

}
