package com.example.edulearn.CONTROLLER.Repetition;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Candidature.Candidature;
import com.example.edulearn.ENTITY.Repetition.*;
import com.example.edulearn.ENTITY.Repetition.New.MatiereNewOffre;
import com.example.edulearn.ENTITY.Repetition.New.Offre;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("/edulearn/api/repetition")
@CrossOrigin("*")
public interface RepetitionController {

    //SUIVANT LA NOUVELLE APPROCHE

    //Offre
    @PostMapping("/new/offre/create")
    ResponseEntity<ServerResponse> createNewOffer(@RequestParam("offre") String offre , @RequestParam("photo") MultipartFile photo, @RequestParam("cni") MultipartFile cni) throws IOException;
    @GetMapping("/new/offre/all")
    ResponseEntity<List<Offre>> getAllNewOffre();

    @GetMapping("/new/offre/all/byparent/{id}")
    ResponseEntity<List<Offre>> getAllNewOffreByParent(@PathVariable Integer id);

    //Candidature
    @GetMapping("/candidature/all")
    ResponseEntity<List<Candidature>> findAllCandidature();
    @GetMapping("/candidature/allbyoffre/{id}")
    ResponseEntity<List<Candidature>> findAllCandidatureByOffre(@PathVariable Integer id);
    @GetMapping("/candidature/allByEnseignant/{id}")
    ResponseEntity<List<Candidature>> findAllCandidatureByEnseignant(@PathVariable Integer id);

    @PostMapping("/candidature/create")
    ResponseEntity<ServerResponse> createCandidature(@RequestParam("candidature") String candidature) throws JsonProcessingException;
    @PostMapping("/candidature/update")
    ResponseEntity<ServerResponse> updateCandidature(@RequestParam("candidature") String candidature) throws JsonProcessingException;
    @GetMapping("/candidature/delete/{id}")
    ResponseEntity<ServerResponse> deleteCandidature(@PathVariable Integer id);

    //Matiere Offre
    @GetMapping("/new/matiere-offre/allbyoffre/{id}")
    ResponseEntity<List<MatiereNewOffre>> findAllNewMatiereOffre(@PathVariable Integer id);




    //ANCIENNE APPROCHE
    //Repetition
    @PostMapping("/create")
    ResponseEntity<Integer> createRepetition(@RequestParam("repetition") String repetition) throws JsonProcessingException;
    @GetMapping("/allsession/byenseignant/{id}")
    ResponseEntity<List<Repetition>> findAllSessionRepetitionByEnseignant(@PathVariable Integer id);


    //Matiere repetition
    @PostMapping("/matiere-repetition/create")
    ResponseEntity<ServerResponse> createMatiereRepetition(@RequestParam("matiererepetition") String matiererepetition) throws JsonProcessingException;
    @GetMapping("/matiere-repetition/all/byrepetition/{id}")
    ResponseEntity<List<MatiereRepetition>> findAllMatiereRepetitionByRepetition(@PathVariable Integer id);
    @GetMapping("/matiere-repetition/allmatiere/byreptition/{id}")
    ResponseEntity<List<Matiere>> findAllMatiereByOffreRepetition(@PathVariable Integer id);
    @GetMapping("/matiere-repetition/allmatiere/byeleve/{id}")
    ResponseEntity<List<Matiere>> findAllMatiereForEleve(@PathVariable Integer id);
    //Horaire Repetition
    @PostMapping("/horaire-repetition/create")
    ResponseEntity<ServerResponse> createHoraireRepetition(@RequestParam("horairerepetition") String horairerepetition) throws JsonProcessingException;
    @GetMapping("/horaire-repetition/all/byrepetition/{id}")
    ResponseEntity<List<HoraireRepetition>> findAllHoraireByRepetition(@PathVariable Integer id);

    //Offre
    @PostMapping("/offre/create")
    ResponseEntity<Integer> createOffreRepetition(@RequestParam("offrerepetition") String offrerepetition) throws JsonProcessingException;
    @PostMapping("/offre/update")
    ResponseEntity<ServerResponse> updateOffreRepetition(@RequestParam("offrerepetition") String offrerepetition) throws JsonProcessingException;
    @GetMapping("/offre/all")
    ResponseEntity<List<OffreRepetition>> findAllRepetitionOffre();
    @GetMapping("/offre/findbyid/{id}")
    ResponseEntity<OffreRepetition> findRepetitionOffreById(@PathVariable Integer id);
    @GetMapping("/offre/findByCode/{code}")
    ResponseEntity<OffreRepetition> findRepetitionOffreByCode(@PathVariable String code);
    @GetMapping("/offre/delete/")
    ResponseEntity<ServerResponse> deleteRepetitionOffre(@PathVariable Integer id);
    @GetMapping("/offre/all/byparent/{id}")
    ResponseEntity<List<OffreRepetition>> findAllRepetitionOffreByParent(@PathVariable Integer id);


    //Matiere Offre de Repetition

    @GetMapping("/offre/matiere-offre/allbyoffre/{id}")
    ResponseEntity<List<MatiereOffre>> findAllMatiereOffreRepetition(@PathVariable Integer id);
    @PostMapping("/offre/matiere-offre/create")
    ResponseEntity<ServerResponse> createMatiereOffreRepetition(@RequestParam("matiere") String matiere) throws JsonProcessingException;

}
