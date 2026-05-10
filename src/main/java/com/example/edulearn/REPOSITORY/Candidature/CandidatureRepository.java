package com.example.edulearn.REPOSITORY.Candidature;

import com.example.edulearn.ENTITY.Candidature.Candidature;
import com.example.edulearn.ENTITY.Repetition.New.Offre;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature,Integer> {
    List<Candidature> findByOffre(Offre offre);
    List<Candidature> findByEnseignant(Enseignant enseignant);

}
