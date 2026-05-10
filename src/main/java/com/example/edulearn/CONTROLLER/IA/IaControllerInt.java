package com.example.edulearn.CONTROLLER.IA;

import com.example.edulearn.DTO.IA.MatchinResult;
import com.example.edulearn.ENTITY.IA.MatchingDB;
import com.example.edulearn.DTO.IA.ScoreMatch;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/edulearn/api/ia/")
@CrossOrigin("*")
public interface IaControllerInt {
    @GetMapping("/assistant")
    ResponseEntity<String> assistantIA();
    @PostMapping("/assistant-textuel")
    ResponseEntity<String> assistanceTextuelle(@RequestParam("prompt") String prompt) throws JsonProcessingException;

    @GetMapping("/score")
    ResponseEntity<String> getScoreCorrespondance();

    @GetMapping("/test/score")
    ResponseEntity<ScoreMatch> getTestScoreCorrespondance() throws Exception;
    @GetMapping("/test/score/multiple")
    ResponseEntity<List<ScoreMatch>> getTestScoreCorrespondanceMultiple() throws Exception;


    //Fonctions finales
    @GetMapping("/matching/offre-multienseignant/{id}")
    ResponseEntity<List<MatchinResult>> matchingOffreAndMultipleEnseignant(@PathVariable Integer id);

    //Sauvegarde des matchings
    @GetMapping("/matching/db/findbyoffre/{id}")
    ResponseEntity<List<MatchingDB>> findAllByOffre(@PathVariable Integer id);
    @PostMapping("/matching/db/create")
    public ResponseEntity<ServerResponse> createMatching(@RequestParam("matchings") String matchings) throws JsonProcessingException;
    @GetMapping("/matching/db/findbyenseignant/{id}")
    ResponseEntity<List<MatchingDB>> findAllEnseignant(@PathVariable Integer id);

}
