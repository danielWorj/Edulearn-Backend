package com.example.edulearn.CONTROLLER.Evaluation;

import com.example.edulearn.ENTITY.Evaluation.Eleve.Evaluation;
import com.example.edulearn.ENTITY.Evaluation.Eleve.ReponseEleve;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Question;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.ReponsePossible;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.TypeEvaluation;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/edulearn/api/evaluation")
@CrossOrigin("*")
public interface EvaluationControllerInt {
    //CRUD Type Evaluation
    @GetMapping("/type-evaluation/all")
    ResponseEntity<List<TypeEvaluation>> findAllTypeEvaluation();
    @PostMapping("/type-evaluation/create")
    ResponseEntity<ServerResponse> creationTypeEvaluation(@RequestParam("typeevaluation") String typeevaluation) throws JsonProcessingException;

    //CRUD Compositions
    @GetMapping("/composition/all/byenseignant/{id}")
    ResponseEntity<List<Composition>> findAllCompositionByEnseignant(@PathVariable Integer id);
    @GetMapping("/composition/all/byrepetition/{id}")
    ResponseEntity<List<Composition>> findAllCompositionByRepetition(@PathVariable Integer id);
    @GetMapping("/composition/all/bymatiere/{id}")
    ResponseEntity<List<Composition>> findAllCompositionByMatiere(@PathVariable Integer id);

    @PostMapping("/composition/create")
    ResponseEntity<Integer> creationComposition(@RequestParam("composition") String composition) throws JsonProcessingException;
    @PostMapping("/composition/update")
    ResponseEntity<ServerResponse> updateComposition(@RequestParam("composition") String composition) throws JsonProcessingException;
    @GetMapping("/composition/delete/{id}")
    ResponseEntity<ServerResponse> deleteComposition(@PathVariable Integer id);

    //CRUD Questions
    @GetMapping("/question/all/bycomposition/{id}")
    ResponseEntity<List<Question>> findAllQuestionByComposition(@PathVariable Integer id);
    @PostMapping("/question/create")
    ResponseEntity<Integer> creationQuestion(@RequestParam("question") String question) throws JsonProcessingException;
    @PostMapping("/question/update")
    ResponseEntity<ServerResponse> updateQuestion(@RequestParam("question") String question);
    @GetMapping("/question/delete/{id}")
    ResponseEntity<ServerResponse> deleteQuestion(@PathVariable Integer id);

    //CRUD Reponses Possible

    @GetMapping("/reponse-possible/all/byquestion/{id}")
    ResponseEntity<List<ReponsePossible>> findAllReponsePossibleByQuestion(@PathVariable Integer id);
    @PostMapping("/reponse-possible/create")
    ResponseEntity<ServerResponse> creationReponsePossible(@RequestParam("reponsepossible") String reponsepossible) throws JsonProcessingException;
    @PostMapping("/reponse-possible/update")
    ResponseEntity<ServerResponse> updateReponsePossible(@RequestParam("reponsepossible") String reponsepossible);
    @GetMapping("/reponse-possible/validate/{id}")
    ResponseEntity<ServerResponse> validateReponse(@PathVariable Integer id);
    @GetMapping("/reponse-possible/delete/{id}")
    ResponseEntity<ServerResponse> deleteReponse(@PathVariable Integer id);

    //CRUD Evaluation

    @GetMapping("/tentative-evaluation/all/byeleve/{id}")
    ResponseEntity<List<Evaluation>> findAllEvaluationByEleve(@PathVariable Integer id);
    @PostMapping("/tentative-evaluation/create")
    ResponseEntity<Integer> creationEvaluation(@RequestParam("evaluation") String evaluation) throws JsonProcessingException;
    @PostMapping("/tentative-evaluation/update")
    ResponseEntity<ServerResponse> updateEvaluation(@RequestParam("evaluation") String evaluation);
    @GetMapping("/tentative-evaluation/delete/{id}")
    ResponseEntity<ServerResponse> deleteEvaluation(@PathVariable Integer id);
    //Note finale de la composition
    @GetMapping("/tentative-evaluation/notefinale/bytentative/{id}")
    ResponseEntity<Double> calculDeLanoteFinale(@PathVariable Integer id);

    //CRUD Reponse Eleve
    @GetMapping("/reponseeleve/all/byEvaluation/{id}")
    ResponseEntity<List<ReponseEleve>> findAllReponseEleveByEvaluation(@PathVariable Integer id);
    @PostMapping("/reponseeleve/create")
    ResponseEntity<ServerResponse> creationReponseEleve(@RequestParam("reponseeleve") String reponseeleve) throws JsonProcessingException;
    @PostMapping("/reponseeleve/update")
    ResponseEntity<ServerResponse> updateReponseEleve(@RequestParam("reponseeleve") String reponseeleve) throws JsonProcessingException;
    @GetMapping("/reponseeleve/delete/{id}")
    ResponseEntity<ServerResponse> deleteReponseEleve(@PathVariable Integer id);

  }
