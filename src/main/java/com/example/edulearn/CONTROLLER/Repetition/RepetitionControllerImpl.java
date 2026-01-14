package com.example.edulearn.CONTROLLER.Repetition;

import com.example.edulearn.DTO.Repetition.HoraireRepetitionDTO;
import com.example.edulearn.DTO.Repetition.MatiereRepetitionDTO;
import com.example.edulearn.DTO.Repetition.OffreRepetitionDTO;
import com.example.edulearn.DTO.Repetition.RepetitionDTO;
import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Repetition.HoraireRepetition;
import com.example.edulearn.ENTITY.Repetition.MatiereRepetition;
import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import com.example.edulearn.ENTITY.Repetition.Repetition;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.example.edulearn.REPOSITORY.Academie.MatiereRepository;
import com.example.edulearn.REPOSITORY.Repetition.HoraireRepetitionRepository;
import com.example.edulearn.REPOSITORY.Repetition.MatiereRepetitionRepository;
import com.example.edulearn.REPOSITORY.Repetition.OffreRepetitionRepository;
import com.example.edulearn.REPOSITORY.Repetition.RepetitionRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.EleveRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.EnseignantRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.ParentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Controller
public class RepetitionControllerImpl implements RepetitionController {
    @Autowired
    private OffreRepetitionRepository offreRepetitionRepository;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private EnseignantRepository enseignantRepository;
    @Autowired
    private MatiereRepository matiereRepository;
    @Autowired
    private EleveRepository eleveRepository;
    @Autowired
    private RepetitionRepository repetitionRepository;
    @Autowired
    private MatiereRepetitionRepository matiereRepetitionRepository;
    @Autowired
    private HoraireRepetitionRepository horaireRepetitionRepository;

    @Override
    public ResponseEntity<Integer> createRepetition(String repetition) throws JsonProcessingException {
        RepetitionDTO repetitionDTO = new ObjectMapper().readValue(repetition, RepetitionDTO.class);

        System.out.println(repetitionDTO);

        Repetition repetitionDb = new Repetition();

        repetitionDb.setOffreRepetition(
                this.offreRepetitionRepository.findById(repetitionDTO.getOffreRepetition()).orElse(null)
        );

        repetitionDb.setEnseignant(
                this.enseignantRepository.findById(repetitionDTO.getEnseignant()).orElse(null)
        );

        repetitionDb.setMontant(repetitionDTO.getMontant());

        System.out.println("repetition to save "+ repetitionDb.toString());

        this.repetitionRepository.save(repetitionDb);

        Repetition lastRepetitiom = this.repetitionRepository.findTopByOrderByIdDesc();

        System.out.println(lastRepetitiom.toString());

        return ResponseEntity.ok(lastRepetitiom.getId());
    }

    @Override
    public ResponseEntity<List<Repetition>> findAllSessionRepetitionByEnseignant(Integer id) {
        return ResponseEntity.ok(
                this.repetitionRepository.findByEnseignant(
                        this.enseignantRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<ServerResponse> createMatiereRepetition(String matiererepetition) throws JsonProcessingException {
        MatiereRepetitionDTO matiereRepetitionDTO = new ObjectMapper().readValue(matiererepetition, MatiereRepetitionDTO.class);

        MatiereRepetition matiereRepetitionDB = new MatiereRepetition();

        matiereRepetitionDB.setRepetition(
                this.repetitionRepository.findById(matiereRepetitionDTO.getRepetition()).orElse(null)
        );

        matiereRepetitionDB.setMatiere(
                this.matiereRepository.findById(matiereRepetitionDTO.getMatiere()).orElse(null)
        );

        this.matiereRepetitionRepository.save(matiereRepetitionDB);

        return ResponseEntity.ok(new ServerResponse("La matiere a bien ete ajoute a cette session de repetition", true));
    }

    @Override
    public ResponseEntity<List<MatiereRepetition>> findAllMatiereRepetitionByRepetition(Integer id) {
        return ResponseEntity.ok(
                this.matiereRepetitionRepository.findByRepetition(
                        this.repetitionRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<List<Matiere>> findAllMatiereByOffreRepetition(Integer id) {
        return ResponseEntity.ok(this.matiereRepetitionRepository.findMatieraByRepetition(
                this.repetitionRepository.findById(id).orElse(null)
        ));
    }

    @Override
    public ResponseEntity<List<Matiere>> findAllMatiereForEleve(Integer id) {
        return ResponseEntity.ok(
                this.matiereRepetitionRepository.findMatiereForEleve(
                        this.eleveRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<ServerResponse> createHoraireRepetition(String horairerepetition) throws JsonProcessingException {
        HoraireRepetitionDTO horaireRepetitionDTO = new ObjectMapper().readValue(horairerepetition, HoraireRepetitionDTO.class);

        HoraireRepetition horaireRepetition = new HoraireRepetition();

        horaireRepetition.setJour(horaireRepetitionDTO.getJour());
        horaireRepetition.setTimeEnd(LocalTime.parse(horaireRepetitionDTO.getTimeEnd()));
        horaireRepetition.setTimeStart(LocalTime.parse(horaireRepetitionDTO.getTimeStart()));
        horaireRepetition.setRepetition(
                this.repetitionRepository.findById(horaireRepetitionDTO.getRepetition()).orElse(null)
        );

        this.horaireRepetitionRepository.save(horaireRepetition);

        return ResponseEntity.ok(new ServerResponse("Horaire de repetition sauvegarde", true));
    }

    @Override
    public ResponseEntity<List<HoraireRepetition>> findAllHoraireByRepetition(Integer id) {
        return ResponseEntity.ok(
                this.horaireRepetitionRepository.findByRepetition(
                        this.repetitionRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<List<Enseignant>> createOffreRepetition(String offrerepetition) throws JsonProcessingException {
        ServerResponse serverResponse = new ServerResponse();

        OffreRepetitionDTO offreRepetitionDTO = new ObjectMapper().readValue(offrerepetition, OffreRepetitionDTO.class);

        OffreRepetition offreRepetition = new OffreRepetition();

        offreRepetition.setBio(offreRepetitionDTO.getBio());
        offreRepetition.setIntitule(offreRepetitionDTO.getIntitule());
        offreRepetition.setSalaireMin(offreRepetitionDTO.getSalaireMin());
        offreRepetition.setSalaireMax(offreRepetitionDTO.getSalaireMax());
        offreRepetition.setDuree(offreRepetitionDTO.getDuree());
        offreRepetition.setFrequence(offreRepetition.getFrequence());
        offreRepetition.setDateCreation(LocalDate.now());

        offreRepetition.setEleve(
                this.eleveRepository.findById(offreRepetitionDTO.getEleve()).orElse(null)
        );

        offreRepetition.setCode(generateAlphanumericCode(offreRepetitionDTO.getIntitule()));

        this.offreRepetitionRepository.save(offreRepetition);

        //Apres la creation de l'offre , on recherche les enseignants qui peuvent y correspondre

        List<Enseignant> enseignantsByProfil = this.enseignantRepository.findByProfilEnseignant(offreRepetition.getProfilEnseignant());

        //serverResponse.setStatus(true);

        //serverResponse.setMessage("Offre de repetition cree");

        return ResponseEntity.ok(enseignantsByProfil);
    }

    @Override
    public ResponseEntity<ServerResponse> updateOffreRepetition(String offrerepetition) throws JsonProcessingException {

        ServerResponse serverResponse = new ServerResponse();

        OffreRepetitionDTO offreRepetitionDTO = new ObjectMapper().readValue(offrerepetition, OffreRepetitionDTO.class);
        OffreRepetition offreRepetitionSaved = this.offreRepetitionRepository.findById(offreRepetitionDTO.getId()).orElse(null);

        if (Objects.nonNull(offreRepetitionSaved)){

            OffreRepetition offreRepetition = new OffreRepetition();

            offreRepetition.setId(offreRepetitionSaved.getId());
            offreRepetition.setBio(offreRepetitionDTO.getBio());
            offreRepetition.setSalaireMin(offreRepetitionDTO.getSalaireMin());
            offreRepetition.setSalaireMax(offreRepetitionDTO.getSalaireMax());
            offreRepetition.setEleve(
                    this.eleveRepository.findById(offreRepetitionDTO.getEleve()).orElse(null)
            );

            this.offreRepetitionRepository.save(offreRepetition);

            serverResponse.setStatus(true);

            serverResponse.setMessage("Offre de repetition mise a jour : successfully ");
        }else{
            serverResponse.setStatus(false);
            serverResponse.setMessage("Offre de repetition mise a jour : failed");

        }

        return ResponseEntity.ok(serverResponse);
    }

    @Override
    public ResponseEntity<List<OffreRepetition>> findAllRepetitionOffre() {
        return ResponseEntity.ok(
                this.offreRepetitionRepository.findAll(Sort.by(Sort.Direction.DESC , "id"))
        );
    }

    @Override
    public ResponseEntity<OffreRepetition> findRepetitionOffreById(Integer id) {
        return ResponseEntity.ok(
                this.offreRepetitionRepository.findById(id).orElse(null)
        );
    }

    @Override
    public ResponseEntity<OffreRepetition> findRepetitionOffreByCode(String code) {
        return ResponseEntity.ok(
                this.offreRepetitionRepository.findByCode(code).orElse(null)
        );
    }

    @Override
    public ResponseEntity<ServerResponse> deleteRepetitionOffre(Integer id) {

        ServerResponse serverResponse = new ServerResponse();
        this.offreRepetitionRepository.deleteById(id);
        serverResponse.setStatus(true);
        serverResponse.setMessage("Offre repetition deleted : successfully");

        return ResponseEntity.ok(serverResponse);
    }

    @Override
    public ResponseEntity<List<OffreRepetition>> findAllRepetitionOffreByParent(Integer id) {
        return new ResponseEntity<>(
                this.offreRepetitionRepository.findByParent(
                        this.parentRepository.findById(id).orElse(null)
                ),
                HttpStatus.OK
        );
    }


    public static String generateAlphanumericCode(String input) {
        if (input == null || input.isEmpty()) {
            return "ALN-" + System.currentTimeMillis();
        }

        StringBuilder code = new StringBuilder();

        // 1. Transformation caractère par caractère
        for (int i = 0; i < Math.min(input.length(), 4); i++) {
            char c = input.charAt(i);
            if (Character.isLetter(c)) {
                // Décaler la lettre de 3 positions
                char shifted = (char) (Character.toUpperCase(c) + 3);
                if (shifted > 'Z') {
                    shifted = (char) (shifted - 26);
                }
                code.append(shifted);
            } else if (Character.isDigit(c)) {
                code.append(c);
            }
        }

        code.append("-");

        // 2. Ajouter la longueur du string
        code.append(String.format("%03d", input.length()));

        // 3. Ajouter un checksum
        int checksum = 0;
        for (char c : input.toCharArray()) {
            checksum += (int) c;
        }
        code.append(checksum % 1000);

        return code.toString();
    }
}
