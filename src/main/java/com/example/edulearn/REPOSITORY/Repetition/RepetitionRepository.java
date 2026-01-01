package com.example.edulearn.REPOSITORY.Repetition;

import com.example.edulearn.ENTITY.Repetition.Repetition;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepetitionRepository extends JpaRepository<Repetition,Integer> {
    List<Repetition> findByEnseignant(Enseignant enseignant);


    Repetition findTopByOrderByIdDesc();

}
