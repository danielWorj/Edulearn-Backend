package com.example.edulearn.CONTROLLER.IA;

import com.example.edulearn.DTO.IA.MatchinResult;
import com.example.edulearn.DTO.IA.ScoreMatch;
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
    ResponseEntity<List<ScoreMatch>> matchingOffreAndMultipleEnseignant(@PathVariable Integer id);
}
