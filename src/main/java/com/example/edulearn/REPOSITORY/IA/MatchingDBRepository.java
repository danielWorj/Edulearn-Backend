package com.example.edulearn.REPOSITORY.IA;

import com.example.edulearn.ENTITY.IA.MatchingDB;
import com.example.edulearn.ENTITY.Repetition.New.Offre;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchingDBRepository extends JpaRepository<MatchingDB,Integer> {
    List<MatchingDB> findByEnseignant(Enseignant enseignant);
    List<MatchingDB> findByOffre(Offre offre);

    boolean existsByOffreIdAndEnseignantId(Integer offreId, Integer enseignantId);
}
