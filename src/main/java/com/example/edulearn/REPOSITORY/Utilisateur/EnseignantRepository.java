package com.example.edulearn.REPOSITORY.Utilisateur;

import com.example.edulearn.ENTITY.Academie.Section;
import com.example.edulearn.ENTITY.Status.StatusEnseignant;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.ProfilEnseignant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnseignantRepository extends JpaRepository<Enseignant,Integer> {
    Enseignant findTopByOrderByIdDesc();
    List<Enseignant> findByProfilEnseignant(ProfilEnseignant profilEnseignant);
    List<Enseignant> findByStatusEnseignant(StatusEnseignant statusEnseignant); 
    List<Enseignant> findBySection(Section section);

}
