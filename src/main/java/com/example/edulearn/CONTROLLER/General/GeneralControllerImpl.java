package com.example.edulearn.CONTROLLER.General;

import java.util.List;
import java.util.Objects;

import com.example.edulearn.ENTITY.Academie.Filiere;
import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Academie.Niveau;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Diplome;
import com.example.edulearn.REPOSITORY.Academie.FiliereRepository;
import com.example.edulearn.REPOSITORY.Academie.MatiereRepository;
import com.example.edulearn.REPOSITORY.Academie.NiveauRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.StatusEnseignantRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.example.edulearn.ENTITY.Academie.Section;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.ENTITY.Status.StatusEnseignant;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.ProfilEnseignant;
import com.example.edulearn.REPOSITORY.Academie.SectionRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.DiplomeRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.ProfilEnseignantRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class GeneralControllerImpl implements GeneralControllerInt {
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private ProfilEnseignantRepository profilEnseignantRepository;
    @Autowired
    private StatusEnseignantRepository statusEnseignantRepository;
    @Autowired
    private DiplomeRepository diplomeRepository;
    @Autowired
    private NiveauRepository niveauRepository;
    @Autowired
    private FiliereRepository filiereRepository;
    @Autowired
    private MatiereRepository matiereRepository;
    @Override
    public ResponseEntity<List<Section>> findAllSection() {
        
        return ResponseEntity.ok(sectionRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }
    @Override
    public ResponseEntity<ServerResponse> createSection(String section) throws JsonProcessingException {
        Section sectionDB = new ObjectMapper().readValue(section, Section.class);
        sectionRepository.save(sectionDB);
        return ResponseEntity.ok(new ServerResponse("Section créée avec succès", true));
    }
    @Override
    public ResponseEntity<ServerResponse> updateSection(String section) throws JsonProcessingException {
        Section sectionDB = new ObjectMapper().readValue(section, Section.class);
        Section existingSection = sectionRepository.findById(sectionDB.getId()).orElse(null);

        if (Objects.nonNull(existingSection)) {
            existingSection.setIntitule(section);       
            sectionRepository.save(existingSection);        
            return ResponseEntity.ok(new ServerResponse("Section mise à jour avec succès", true));
        } else {
            return ResponseEntity.ok(new ServerResponse("Section non trouvée", false));
        }


    }
    @Override
    public ResponseEntity<ServerResponse> deleteSection(Integer id) {
        sectionRepository.deleteById(id); 
        return ResponseEntity.ok(new ServerResponse("Section supprimée avec succès", true));
    }
    @Override
    public ResponseEntity<List<ProfilEnseignant>> findAllProfilEnseignant() {
        return ResponseEntity.ok(profilEnseignantRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }
    @Override
    public ResponseEntity<ServerResponse> createProfilEnseignant(String profil) throws JsonProcessingException {
        ProfilEnseignant profilDB = new ObjectMapper().readValue(profil, ProfilEnseignant.class);
        profilEnseignantRepository.save(profilDB);
        return ResponseEntity.ok(new ServerResponse("Profil Enseignant créé avec succès", true));
    }
    @Override
    public ResponseEntity<ServerResponse> deleteProfilEnseignant(Integer id) {
        profilEnseignantRepository.deleteById(id); 
        return ResponseEntity.ok(new ServerResponse("Profil Enseignant supprimé avec succès", true));   
    }


    @Override
    public ResponseEntity<List<StatusEnseignant>> findAllStatusEnseignant() {
        return ResponseEntity.ok(statusEnseignantRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }
    @Override
    public ResponseEntity<ServerResponse> createStatusEnseignant(String status) throws JsonProcessingException {
        StatusEnseignant statusDB = new ObjectMapper().readValue(status, StatusEnseignant.class);
        statusEnseignantRepository.save(statusDB);  
        return ResponseEntity.ok(new ServerResponse("Status Enseignant créé avec succès", true));
    }
    @Override
    public ResponseEntity<ServerResponse> deleteStatusEnseignant(Integer id) {
        this.sectionRepository.deleteById(id);
        return ResponseEntity.ok(new ServerResponse("Status Enseignant supprimé avec succès", true));
    }

    @Override
    public ResponseEntity<List<Diplome>> findAllDiplome() {
        return ResponseEntity.ok(diplomeRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }

    @Override
    public ResponseEntity<ServerResponse> createDiplome(String diplome) throws JsonProcessingException {
        Diplome diplomeDB = new ObjectMapper().readValue(diplome, Diplome.class);
        diplomeRepository.save(diplomeDB);  
        return ResponseEntity.ok(new ServerResponse("Diplome créé avec succès", true));  
    }

    @Override
    public ResponseEntity<ServerResponse> deleteDiplome(Integer id) {
        diplomeRepository.deleteById(id); 
        return ResponseEntity.ok(new ServerResponse("Diplome supprimé avec succès", true));
    }

    @Override
    public ResponseEntity<List<Niveau>> findAllNiveau() {
        return ResponseEntity.ok(
                this.niveauRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    @Override
    public ResponseEntity<List<Niveau>> findAllNiveauBySection(Integer id) {
        return ResponseEntity.ok(
                this.niveauRepository.findBySection(
                        this.sectionRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<List<Filiere>> findAllFiliereBySection(Integer id) {
        return ResponseEntity.ok(
                this.filiereRepository.findBySection(
                        this.sectionRepository.findById(id).orElse(null)
                )
        );
    }



    @Override
    public ResponseEntity<List<Filiere>> findAllFiliere() {
        return ResponseEntity.ok(
                this.filiereRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    @Override
    public ResponseEntity<List<Matiere>> findAllMatiereBySection(Integer id) {
        System.out.println("Liste de matiere by section");

        System.out.println(this.matiereRepository.findMatiereBySection(
                this.sectionRepository.findById(id).orElse(null)
        ));
        return ResponseEntity.ok(
                this.matiereRepository.findMatiereBySection(
                        this.sectionRepository.findById(id).orElse(null)
                )
        );
    }

}
