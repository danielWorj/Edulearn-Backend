package com.example.edulearn.REPOSITORY.Repetition;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Repetition.MatiereRepetition;
import com.example.edulearn.ENTITY.Repetition.Repetition;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatiereRepetitionRepository extends JpaRepository<MatiereRepetition,Integer> {
    List<MatiereRepetition> findByRepetition(Repetition repetition);

    @Query(value = "SELECT m FROM MatiereRepetition mr JOIN mr.matiere m JOIN mr.repetition rp WHERE rp=:r")
    List<Matiere> findMatieraByRepetition(@Param("r") Repetition repetition);

    @Query(value = "SELECT m FROM MatiereRepetition mr JOIN mr.matiere m JOIN mr.repetition rp JOIN rp.offreRepetition o JOIN o.eleve e WHERE e=:el")
    List<Matiere> findMatiereForEleve(@Param("el")Eleve eleve);
}
