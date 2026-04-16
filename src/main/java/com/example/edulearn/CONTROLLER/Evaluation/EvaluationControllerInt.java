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
    @GetMapping("/composition/nonarchived/byeleve/{id}")
    ResponseEntity<List<Composition>> findAllCompositionNonArchivedByEleve(@PathVariable Integer id);
    @GetMapping("/composition/nonarchived/bymatiere/{id}")
    ResponseEntity<List<Composition>> findAllCompositionNonArchivedByMatiere(@PathVariable Integer id);
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
    @GetMapping("/reponse-possible/istrue/byquestion/{id}")
    ResponseEntity<ReponsePossible> findReponsePossibleIsTrue(@PathVariable Integer id);
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
    @GetMapping("/tentative-evaluation/all/bycomposition/{id}")
    ResponseEntity<Evaluation> findAllEvaluationByComposition(@PathVariable Integer id);
    @PostMapping("/tentative-evaluation/create")
    ResponseEntity<Integer> creationEvaluation(@RequestParam("evaluation") String evaluation) throws JsonProcessingException;
    @PostMapping("/tentative-evaluation/update")
    ResponseEntity<ServerResponse> updateEvaluation(@RequestParam("evaluation") String evaluation);
    @GetMapping("/tentative-evaluation/delete/{id}")
    ResponseEntity<ServerResponse> deleteEvaluation(@PathVariable Integer id);
    //Note finale de la composition
    @GetMapping("/tentative-evaluation/notefinale/bytentative/{id}")
    ResponseEntity<Double> calculDeLanoteFinale(@PathVariable Integer id);
    @GetMapping("/tentative-evaluation/nettoyage/{id}")
    ResponseEntity<ServerResponse> nettoyageTentativeEvaluation(@PathVariable Integer id);
    @GetMapping("/tentative-evaluation/findby/eleve/matiere/{idE}/{idM}")
    ResponseEntity<List<Evaluation>> findEvaluationForEleveAndMatiere(@PathVariable Integer idE , @PathVariable Integer idM);
    //CRUD Reponse Eleve
    @GetMapping("/reponse-eleve/all/byEvaluation/{id}")
    ResponseEntity<List<ReponseEleve>> findAllReponseEleveByEvaluation(@PathVariable Integer id);
    @GetMapping("/reponse-eleve/byquestion/{id}")
    ResponseEntity<ReponseEleve> findReponseEleveByQuestion(@PathVariable Integer id);

    @PostMapping("/reponse-eleve/create")
    ResponseEntity<ServerResponse> creationReponseEleve(@RequestParam("reponseeleve") String reponseeleve) throws JsonProcessingException;
    @PostMapping("/reponse-eleve/update")
    ResponseEntity<ServerResponse> updateReponseEleve(@RequestParam("reponseeleve") String reponseeleve) throws JsonProcessingException;
    @GetMapping("/reponse-eleve/delete/{id}")
    ResponseEntity<ServerResponse> deleteReponseEleve(@PathVariable Integer id);

  }
