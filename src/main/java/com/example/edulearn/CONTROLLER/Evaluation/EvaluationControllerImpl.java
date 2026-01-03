package com.example.edulearn.CONTROLLER.Evaluation;

import com.example.edulearn.DTO.Evaluation.*;
import com.example.edulearn.ENTITY.Evaluation.Eleve.Evaluation;
import com.example.edulearn.ENTITY.Evaluation.Eleve.ReponseEleve;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Question;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.ReponsePossible;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.TypeEvaluation;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.REPOSITORY.Academie.MatiereRepository;
import com.example.edulearn.REPOSITORY.Evaluation.*;
import com.example.edulearn.REPOSITORY.Repetition.RepetitionRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.EleveRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.EnseignantRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
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
    private RepetitionRepository repetitionRepository;
    @Autowired
    private EleveRepository eleveRepository;
    @Autowired
    private MatiereRepository matiereRepository;
    @Autowired
    private TypeEvaluationRepository typeEvaluationRepository;

    @Override
    public ResponseEntity<List<TypeEvaluation>> findAllTypeEvaluation() {
        return ResponseEntity.ok(
                this.typeEvaluationRepository.findAll()
        );
    }

    @Override
    public ResponseEntity<ServerResponse> creationTypeEvaluation(String typeevaluation) throws JsonProcessingException {
        TypeEvaluation typeEvaluation = new ObjectMapper().readValue(typeevaluation, TypeEvaluation.class);
        this.typeEvaluationRepository.save(typeEvaluation);
        return ResponseEntity.ok(new ServerResponse("Type evaluation created successfully", true));
    }

    @Override
    public ResponseEntity<List<Composition>> findAllCompositionByEnseignant(Integer id) {
        return ResponseEntity.ok(
                this.compositionRepository.findByEnseignant(
                        this.enseignantRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<List<Composition>> findAllCompositionByRepetition(Integer id) {
        return ResponseEntity.ok(
                this.compositionRepository.findByRepetition(
                        this.repetitionRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<Integer> creationComposition(String composition) throws JsonProcessingException {
        CompositionDTO compositionDTO = new ObjectMapper().readValue(composition, CompositionDTO.class);

        Composition compositionDB = new Composition();

        compositionDB.setRepetition(this.repetitionRepository.findById(compositionDTO.getRepetition()).orElse(null));
        compositionDB.setDescription(compositionDTO.getDescription());
        compositionDB.setTypeEvaluation(this.typeEvaluationRepository.findById(compositionDTO.getTypeEvaluation()).orElse(null));
        compositionDB.setDuree(compositionDTO.getDuree());
        compositionDB.setActive(true);
        compositionDB.setArchived(false); //Par defaut , nom archivé
        compositionDB.setDateCreation(LocalDate.now());

        this.compositionRepository.save(compositionDB);

        Composition compositionSaved = this.compositionRepository.findTopByOrderByIdDesc();
        return ResponseEntity.ok(compositionSaved.getId());
    }

    @Override
    public ResponseEntity<ServerResponse> updateComposition(String composition) throws JsonProcessingException {
        CompositionDTO compositionDTO = new ObjectMapper().readValue(composition, CompositionDTO.class);

        Composition compositionSaved = this.compositionRepository.findById(compositionDTO.getId()).orElse(null);

        if (Objects.nonNull(compositionSaved)){

            Composition compositionDB = new Composition();

            compositionDB.setId(compositionSaved.getId());
            compositionDB.setRepetition(this.repetitionRepository.findById(compositionDTO.getRepetition()).orElse(null));
            compositionDB.setDescription(compositionDTO.getDescription());
            compositionDB.setTypeEvaluation(this.typeEvaluationRepository.findById(compositionDTO.getTypeEvaluation()).orElse(null));
            compositionDB.setDuree(compositionDTO.getDuree());
            compositionDB.setActive(true);
            compositionDB.setArchived(false); //Par defaut , nom archivé
            compositionDB.setDateCreation(LocalDate.now());

            this.compositionRepository.save(compositionDB);

            return ResponseEntity.ok(new ServerResponse("Compisition has been updated", true));

        }else {
            return ResponseEntity.ok(new ServerResponse("Compistiion not found ", false));

        }
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
    public ResponseEntity<Integer> creationQuestion(String question) throws JsonProcessingException {
        QuestionDTO questionDTO = new ObjectMapper().readValue(question, QuestionDTO.class);
        Question questionDB = new Question();

        questionDB.setComposition(this.compositionRepository.findById(questionDTO.getComposition()).orElse(null));
        questionDB.setEnonce(questionDTO.getEnonce());
        questionDB.setPoints(questionDTO.getPoints());

        this.questionRepository.save(questionDB);

        Question questionSaved = this.questionRepository.findTopByOrderByIdDesc();

        return ResponseEntity.ok(questionSaved.getId());
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
        reponsePossibleDB.setCorrecte(false);
        reponsePossibleDB.setQuestion(this.questionRepository.findById(reponsePossibleDTO.getQuestion()).orElse(null));

        this.reponsePossibleRepository.save(reponsePossibleDB);
        return ResponseEntity.ok(new ServerResponse("Reponse possible created successfully", true));
    }

    @Override
    public ResponseEntity<ServerResponse> updateReponsePossible(String reponsepossible) {
        return null;
    }

    @Override
    public ResponseEntity<ServerResponse> validateReponse(Integer id) {
        ReponsePossible reponsePossible = this.reponsePossibleRepository.findById(id).orElse(null);
        if (Objects.nonNull(reponsePossible)){
            if (reponsePossible.getCorrecte()){
                return ResponseEntity.ok(new ServerResponse("Reponse possible is already validated", false));
            }else {
                //On doit d'abord invalider les autres reponses possibles de la question
                List<ReponsePossible> reponsePossibles = this.reponsePossibleRepository.findByQuestion(reponsePossible.getQuestion());
                for (ReponsePossible rp : reponsePossibles){
                    rp.setCorrecte(false);
                    this.reponsePossibleRepository.save(rp);
                }
            }
            reponsePossible.setCorrecte(true);
            this.reponsePossibleRepository.save(reponsePossible);

            return ResponseEntity.ok(new ServerResponse("Reponse possible validated successfully", true));
        }else {
            return ResponseEntity.ok(new ServerResponse("Reponse possible not found", false));
        }
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
        evaluationDB.setNote(evaluationDTO.getNote());
        evaluationDB.setStartTime(evaluationDTO.getStartTime());
        evaluationDB.setEndTime(evaluationDTO.getEndTime());
        evaluationDB.setCompleted(false);
        this.evaluationRepository.save(evaluationDB);
        Evaluation evaluationSaved = this.evaluationRepository.findTopByOrderByIdDesc();

        //Modifie l'etat de la composition pour signaler qu'on a deja compose
        Composition compositionCorrespondante = this.compositionRepository.findById(evaluationDTO.getComposition()).orElse(null);
        if (Objects.nonNull(compositionCorrespondante)){
            compositionCorrespondante.setArchived(true);
            this.compositionRepository.save(compositionCorrespondante);
        }

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
