package com.example.edulearn.CONTROLLER.Repetition;

import com.example.edulearn.ENTITY.Repetition.HoraireRepetition;
import com.example.edulearn.ENTITY.Repetition.MatiereRepetition;
import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import com.example.edulearn.ENTITY.Repetition.Repetition;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/edulearn/api/repetition")
@CrossOrigin("*")
public interface RepetitionController {
    //Repetition
    @PostMapping("/create")
    ResponseEntity<Integer> createRepetition(@RequestParam("repetition") String repetition);
    @GetMapping("/allsession/byenseignant/{id}")
    ResponseEntity<List<Repetition>> findAllSessionRepetitionByEnseignant(@PathVariable Integer id);


    //Matiere repetition
    @PostMapping("/matiere-repetition/create")
    ResponseEntity<ServerResponse> createMatiereRepetition(@RequestParam("matiererepetition") String matiererepetition);
    @GetMapping("/matiere-repetition/all/byrepetition/{id}")
    ResponseEntity<List<MatiereRepetition>> findAllMatiereRepetitionByRepetition(@PathVariable Integer id);

    //Horaire Repetition
    @PostMapping("/horaire-repetition/create")
    ResponseEntity<ServerResponse> createHoraireRepetition(@RequestParam("horairerepetition") String horairerepetition);
    @GetMapping("/horaire-repetition/all/byrepetition/{id}")
    ResponseEntity<List<HoraireRepetition>> findAllHoraireByRepetition(@PathVariable Integer id);

    //Offre
    @PostMapping("/offre/create")
    ResponseEntity<ServerResponse> createOffreRepetition(@RequestParam("offrerepetition") String offrerepetition);
    @PostMapping("/offre/update")
    ResponseEntity<ServerResponse> updateOffreRepetition(@RequestParam("offrerepetition") String offrerepetition);
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
}
