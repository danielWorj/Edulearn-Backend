package com.example.edulearn.REPOSITORY.Repetition;

import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import com.example.edulearn.ENTITY.Repetition.Repetition;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import com.example.edulearn.ENTITY.Utilisateur.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OffreRepetitionRepository extends JpaRepository<OffreRepetition,Integer> {
    List<OffreRepetition> findByEleve(Eleve eleve);

    @Query(value = "SELECT o FROM OffreRepetition o JOIN o.eleve e JOIN e.parent p WHERE p=:parent")
    List<OffreRepetition> findByParent(Parent parent);

    Optional<OffreRepetition> findByCode(String code);

    OffreRepetition findTopByOrderByIdDesc();

}
