package com.example.edulearn.CONTROLLER.General;

import com.example.edulearn.ENTITY.Academie.*;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.ENTITY.Status.StatusEnseignant;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Diplome;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.ProfilEnseignant;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/edulearn/api/general")
@CrossOrigin("*")
public interface GeneralControllerInt {
    //General
    @GetMapping("/push/admin")
    ResponseEntity<ServerResponse> pushAdmin();
//    @GetMapping("/general/push/matiere")
//    ResponseEntity<ServerResponse> pushMatiere();
//    @GetMapping("/general/push/categoriematiere")
//    ResponseEntity<ServerResponse> pushCategorieMatiere();
//    @GetMapping("/general/push/profil-enseignant")
//    ResponseEntity<ServerResponse> pushProfilEnseignant();
//    @GetMapping("/general/push/status-enseignant")
//    ResponseEntity<ServerResponse> pushStatusEnseignant();
//    @GetMapping("/general/push/diplome")
//    ResponseEntity<ServerResponse> pushDiplome();
//    @GetMapping("/general/push/niveau")
//    ResponseEntity<ServerResponse> pushNiveau();
//    @GetMapping("/general/push/filiere")
//    ResponseEntity<ServerResponse> pushFiliere();
//


    //Section
    @GetMapping("/section/all")
    ResponseEntity<List<Section>> findAllSection();
    @PostMapping("/section/create")
    ResponseEntity<ServerResponse> createSection(@RequestParam("section") String section) throws JsonProcessingException;
    @PostMapping("/section/update")
    ResponseEntity<ServerResponse> updateSection(@RequestParam("section") String section) throws JsonProcessingException;
    @GetMapping("/section/delete/{id}")
    ResponseEntity<ServerResponse> deleteSection(@PathVariable Integer id);

    //Profil Enseigannt
    @GetMapping("/profil-enseignant/all")
    ResponseEntity<List<ProfilEnseignant>> findAllProfilEnseignant(); 
    @PostMapping("/profil-enseignant/create")
    ResponseEntity<ServerResponse> createProfilEnseignant(@RequestParam("profil") String profil) throws JsonProcessingException;
    @GetMapping("/profil-enseignant/delete/{id}")
    ResponseEntity<ServerResponse> deleteProfilEnseignant(@PathVariable Integer id);
    //Status Enseignant
    @GetMapping("/status-enseignant/all")
    ResponseEntity<List<StatusEnseignant>> findAllStatusEnseignant();
    @PostMapping("/status-enseignant/create")
    ResponseEntity<ServerResponse> createStatusEnseignant(@RequestParam("status") String status ) throws JsonProcessingException;
    @GetMapping("/status-enseignant/delete/{id}")
    ResponseEntity<ServerResponse> deleteStatusEnseignant(@PathVariable Integer id);


    //Diplome
    @GetMapping("/diplome/all") 
    ResponseEntity<List<Diplome>> findAllDiplome();
    @PostMapping("/diplome/create")
    ResponseEntity<ServerResponse> createDiplome(@RequestParam("diplome") String diplome) throws JsonProcessingException;
    @GetMapping("/diplome/delete/{id}")
    ResponseEntity<ServerResponse> deleteDiplome(@PathVariable Integer id);

    //Niveau
    @GetMapping("/niveau/all")
    ResponseEntity<List<Niveau>> findAllNiveau();

    @GetMapping("/niveau/allbySection/{id}")
    ResponseEntity<List<Niveau>> findAllNiveauBySection(@PathVariable Integer id);
    @PostMapping("/niveau/create")
    ResponseEntity<ServerResponse> createNiveau(@RequestParam("niveau") String niveau) throws JsonProcessingException;

    //Filiere
    @GetMapping("/filiere/all")
    ResponseEntity<List<Filiere>> findAllFiliere();
    @GetMapping("/filiere/allbySection/{id}")
    ResponseEntity<List<Filiere>> findAllFiliereBySection(@PathVariable Integer id);
    @PostMapping("/filiere/create")
    ResponseEntity<ServerResponse> createFiliere(@RequestParam("filiere") String filiere) throws JsonProcessingException;


    //Matiere
    @GetMapping("/matiere/allbySection/{id}")
    ResponseEntity<List<Matiere>> findAllMatiereBySection(@PathVariable Integer id);
    @GetMapping("/matiere/all")
    ResponseEntity<List<Matiere>> findAllMatiere();
    @PostMapping("/matiere/create")
    ResponseEntity<ServerResponse> createMatiere(@RequestParam("matiere") String matiere) throws JsonProcessingException;


    //CategorieMatiere
    @GetMapping("/categorie-matiere/all")
    ResponseEntity<List<CategorieMatiere>> findAllCatMatiere();
    @GetMapping("/categorie-matiere/allbySection/")
    ResponseEntity<List<CategorieMatiere>> findAllCatMatiereBySection(@PathVariable Integer id);
    @PostMapping("/categorie-matiere/create")
    ResponseEntity<ServerResponse> createCategorieMatiere(@RequestParam("categoriematiere") String categorie) throws JsonProcessingException;
}
