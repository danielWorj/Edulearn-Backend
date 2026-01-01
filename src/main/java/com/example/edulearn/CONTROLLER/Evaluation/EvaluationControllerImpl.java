package com.example.edulearn.CONTROLLER.Evaluation;

import com.example.edulearn.DTO.Evaluation.*;
import com.example.edulearn.ENTITY.Evaluation.Eleve.Evaluation;
import com.example.edulearn.ENTITY.Evaluation.Eleve.ReponseEleve;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Question;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.ReponsePossible;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.REPOSITORY.Academie.MatiereRepository;
import com.example.edulearn.REPOSITORY.Evaluation.*;
import com.example.edulearn.REPOSITORY.Utilisateur.EleveRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.EnseignantRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Controller
public class EvaluationControllerImpl implements EvaluationControllerInt{
    @Autowired
    private CompositionRepository compositionRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ReponsePossibleRepository reponsePossibleRepository;
    @Autowired
    private EvaluationRepository evaluationRepository;
    @Autowired
    private ReponseEleveRepository reponseEleveRepository;
    @Autowired
    private EnseignantRepository enseignantRepository;
    @Autowired
    private EleveRepository eleveRepository;
    @Autowired
    private MatiereRepository matiereRepository;
    @Autowired
    private TypeEvaluationRepository typeEvaluationRepository;

    @Override
    public ResponseEntity<List<Composition>> findAllCompositionByEnseignant(Integer id) {
        return ResponseEntity.ok(
                this.compositionRepository.findByEnseignant(
                        this.enseignantRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<List<Composition>> findAllCompositionByMatiere(Integer id) {
        return ResponseEntity.ok(
                this.compositionRepository.findByMatiere(
                        this.matiereRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<Integer> creationComposition(String composition) throws JsonProcessingException {
        CompositionDTO compositionDTO = new ObjectMapper().readValue(composition, CompositionDTO.class);

        Composition compositionDB = new Composition();

        compositionDB.setEnseignant(this.enseignantRepository.findById(compositionDTO.getEnseignant()).orElse(null));
        compositionDB.setDescription(compositionDTO.getDescription());
        compositionDB.setMatiere(this.matiereRepository.findById(compositionDTO.getMatiere()).orElse(null));
        compositionDB.setTypeEvaluation(this.typeEvaluationRepository.findById(compositionDTO.getTypeEvaluation()).orElse(null));
        compositionDB.setDuree(LocalTime.parse(compositionDTO.getDuree()));
        compositionDB.setActive(true);

        this.compositionRepository.save(compositionDB);

        Composition compositionSaved = this.compositionRepository.findTopByOrderByIdDesc();
        return ResponseEntity.ok(compositionSaved.getId());
    }

    @Override
    public ResponseEntity<ServerResponse> updateComposition(String composition) {
        return null;
    }

    @Override
    public ResponseEntity<ServerResponse> deleteComposition(Integer id) {
        return null;
    }

    @Override
    public ResponseEntity<List<Question>> findAllQuestionByComposition(Integer id) {
        return ResponseEntity.ok(
                this.questionRepository.findByComposition(
                        this.compositionRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<ServerResponse> creationQuestion(String question) throws JsonProcessingException {
        QuestionDTO questionDTO = new ObjectMapper().readValue(question, QuestionDTO.class);
        Question questionDB = new Question();

        questionDB.setComposition(this.compositionRepository.findById(questionDTO.getComposition()).orElse(null));
        questionDB.setEnonce(questionDTO.getEnonce());
        questionDB.setPoints(questionDTO.getPoints());

        this.questionRepository.save(questionDB);

        return ResponseEntity.ok(new ServerResponse("Question created successfully", true));
    }

    @Override
    public ResponseEntity<ServerResponse> updateQuestion(String question) {
        return null;
    }

    @Override
    public ResponseEntity<ServerResponse> deleteQuestion(Integer id) {

        return null;
    }

    @Override
    public ResponseEntity<List<ReponsePossible>> findAllReponsePossibleByQuestion(Integer id) {
        return ResponseEntity.ok(
                this.reponsePossibleRepository.findByQuestion(
                        this.questionRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<ServerResponse> creationReponsePossible(String reponsepossible) throws JsonProcessingException {
        ReponsePossibleDTO reponsePossibleDTO = new ObjectMapper().readValue(reponsepossible, ReponsePossibleDTO.class);

        ReponsePossible reponsePossibleDB = new ReponsePossible();

        reponsePossibleDB.setReponse(reponsePossibleDTO.getReponse());
        reponsePossibleDB.setCorrecte(reponsePossibleDTO.getCorrecte());
        reponsePossibleDB.setQuestion(this.questionRepository.findById(reponsePossibleDTO.getQuestion()).orElse(null));

        this.reponsePossibleRepository.save(reponsePossibleDB);
        return ResponseEntity.ok(new ServerResponse("Reponse possible created successfully", true));
    }

    @Override
    public ResponseEntity<ServerResponse> updateReponsePossible(String reponsepossible) {
        return null;
    }

    @Override
    public ResponseEntity<ServerResponse> deleteReponse(Integer id) {
        return null;
    }

    @Override
    public ResponseEntity<List<Evaluation>> findAllEvaluationByEleve(Integer id) {
        return ResponseEntity.ok(
                this.evaluationRepository.findByEleve(
                        this.eleveRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<Integer> creationEvaluation(String evaluation) throws JsonProcessingException {
        EvaluationDTO evaluationDTO = new ObjectMapper().readValue(evaluation, EvaluationDTO.class);
        Evaluation evaluationDB = new Evaluation();
        evaluationDB.setComposition(this.compositionRepository.findById(evaluationDTO.getComposition()).orElse(null));
        evaluationDB.setEleve(this.eleveRepository.findById(evaluationDTO.getEleve()).orElse(null));
        evaluationDB.setNote(evaluationDTO.getNote());
        evaluationDB.setStartTime(evaluationDTO.getStartTime());
        evaluationDB.setEndTime(evaluationDTO.getEndTime());
        evaluationDB.setCompleted(false);
        this.evaluationRepository.save(evaluationDB);
        Evaluation evaluationSaved = this.evaluationRepository.findTopByOrderByIdDesc();
        return ResponseEntity.ok(evaluationSaved.getId());
    }

    @Override
    public ResponseEntity<ServerResponse> updateEvaluation(String evaluation) {
        return null;
    }

    @Override
    public ResponseEntity<ServerResponse> deleteEvaluation(Integer id) {
        return null;
    }

    @Override
    public ResponseEntity<List<ReponseEleve>> findAllReponseEleveByEvaluation(Integer id) {
        return ResponseEntity.ok(
                this.reponseEleveRepository.findByEvaluation(
                        this.evaluationRepository.findById(id).orElse(null)
                )
        );

    }

    @Override
    public ResponseEntity<ServerResponse> creationReponseEleve(String reponseeleve) throws JsonProcessingException {
        ReponseEleveDTO reponseEleveDTO = new ObjectMapper().readValue(reponseeleve, ReponseEleveDTO.class);

        ReponseEleve reponseEleveDB = new ReponseEleve();
        reponseEleveDB.setEvaluation(this.evaluationRepository.findById(reponseEleveDTO.getEvaluation()).orElse(null));
        reponseEleveDB.setReponseChoisie(this.reponsePossibleRepository.findById(reponseEleveDTO.getReponseChoisie()).orElse(null));
        reponseEleveDB.setQuestion(this.questionRepository.findById(reponseEleveDTO.getQuestion()).orElse(null));

        this.reponseEleveRepository.save(reponseEleveDB);

        //ReponseEleve reponseEleveSaved = this.reponseEleveRepository.findTopByOrderByIdDesc();

        return ResponseEntity.ok(new ServerResponse("Reponse eleve created successfully",true));
    }

    @Override
    public ResponseEntity<ServerResponse> updateReponseEleve(String reponseeleve) throws JsonProcessingException {
        ReponseEleveDTO reponseEleveDTO = new ObjectMapper().readValue(reponseeleve, ReponseEleveDTO.class);

        ReponseEleve oldReponse = this.reponseEleveRepository.findById(reponseEleveDTO.getId()).orElse(null);

        if (Objects.nonNull(oldReponse)){
            //Cette reponse existe deja donc on fait une mise a jour
            ReponseEleve reponseEleveDB = new ReponseEleve();

            reponseEleveDB.setId(oldReponse.getId());
            reponseEleveDB.setEvaluation(this.evaluationRepository.findById(reponseEleveDTO.getEvaluation()).orElse(null));
            reponseEleveDB.setReponseChoisie(this.reponsePossibleRepository.findById(reponseEleveDTO.getReponseChoisie()).orElse(null));
            reponseEleveDB.setQuestion(this.questionRepository.findById(reponseEleveDTO.getQuestion()).orElse(null));

            this.reponseEleveRepository.save(reponseEleveDB);

        }else{
            System.out.println("Reponse eleve with id " + reponseEleveDTO.getId() + " not found.");
        }



        return ResponseEntity.ok(new ServerResponse("Reponse eleve updated successfully",true));
    }

    @Override
    public ResponseEntity<ServerResponse> deleteReponseEleve(Integer id) {
        this.reponseEleveRepository.deleteById(id);
        return ResponseEntity.ok(new ServerResponse("Reponse eleve deleted successfully",true));
    }
}
