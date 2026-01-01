package com.example.edulearn.REPOSITORY.Academie;

import com.example.edulearn.ENTITY.Utilisateur.Eleve.Etablissement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtablissementRepository extends JpaRepository<Etablissement,Integer> {
}
