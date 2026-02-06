package com.example.edulearn.CONTROLLER.General;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.example.edulearn.DTO.Academie.CategorieMatiereDTO;
import com.example.edulearn.DTO.Academie.FiliereDTO;
import com.example.edulearn.DTO.Academie.MatiereDTO;
import com.example.edulearn.DTO.Academie.NiveauDTO;
import com.example.edulearn.ENTITY.Academie.*;
import com.example.edulearn.ENTITY.Utilisateur.Administrateur;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Diplome;
import com.example.edulearn.REPOSITORY.Academie.*;
import com.example.edulearn.REPOSITORY.Utilisateur.AdminRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.StatusEnseignantRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.ENTITY.Status.StatusEnseignant;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.ProfilEnseignant;
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
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private CategorieMatiereRepository categorieMatiereRepository;




//    @Override
//    public ResponseEntity<ServerResponse> pushSection() {
//        Section section1 = new Section("Section Francophone");
//        Section section2 = new Section("Section Anglophone");
//
//        this.sectionRepository.save(section1);
//        this.sectionRepository.save(section2);
//
//        return ResponseEntity.ok(new ServerResponse("Sections poussées avec succès", true));
//    }
//
//    @Override
//    public ResponseEntity<ServerResponse> pushMatiere() {
//        return null;
//    }
//
//    @Override
//    public ResponseEntity<ServerResponse> pushCategorieMatiere() {
//        CategorieMatiere categorieMatiere1 = new CategorieMatiere("Matieres Scientifiques", this.sectionRepository.findById(1).orElse(null));
//        return ResponseEntity.ok(new ServerResponse("Catégories de matières poussées avec succès", true));
//    }
//
//    @Override
//    public ResponseEntity<ServerResponse> pushProfilEnseignant() {
//        return null;
//    }
//
//    @Override
//    public ResponseEntity<ServerResponse> pushStatusEnseignant() {
//        return null;
//    }
//
//    @Override
//    public ResponseEntity<ServerResponse> pushDiplome() {
//        return null;
//    }
//
//    @Override
//    public ResponseEntity<ServerResponse> pushNiveau() {
//        return null;
//    }
//
//    @Override
//    public ResponseEntity<ServerResponse> pushFiliere() {
//        return null;
//    }

    @Override
    public ResponseEntity<ServerResponse> pushAdmin() {
        Administrateur admin = new Administrateur();
        admin.setNomComplet("Super Admin");
        admin.setNiveau("Niveau max");
        admin.setEmail("admin@gmail.com");
        admin.setRole(1);
        admin.setPassword("admin123");
        admin.setDateInscription(LocalDate.now());
        admin.setTelephone("678453456");
        admin.setStatus(true);

        this.adminRepository.save(admin);

        return ResponseEntity.ok(new ServerResponse("Admin poussé avec succès", true));
    }

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
    public ResponseEntity<ServerResponse> createNiveau(String niveau) throws JsonProcessingException {
        NiveauDTO niveauDTO = new  ObjectMapper().readValue(niveau, NiveauDTO.class);

        Niveau niveauDB = new Niveau();

        niveauDB.setIntitule(niveauDTO.getIntitule());
        niveauDB.setSection(this.sectionRepository.findById(niveauDTO.getSection()).orElse(null));

        this.niveauRepository.save(niveauDB);
        return ResponseEntity.ok(new ServerResponse("Niveau ajoutée avec succes", true));
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
    public ResponseEntity<ServerResponse> createFiliere(String filiere) throws JsonProcessingException {
        FiliereDTO filiereDTO = new ObjectMapper().readValue(filiere,FiliereDTO.class);

        Filiere filiereDB = new Filiere();

        filiereDB.setSection(this.sectionRepository.findById(filiereDTO.getSection()).orElse(null));
        filiereDB.setIntitule(filiereDTO.getIntitule());

        this.filiereRepository.save(filiereDB);

        return ResponseEntity.ok(new ServerResponse("Nouvelle filiere", true));
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

    @Override
    public ResponseEntity<ServerResponse> createMatiere(String matiere) throws JsonProcessingException {
        MatiereDTO matiereDTO = new ObjectMapper().readValue(matiere, MatiereDTO.class);

        Matiere matiereDB = new Matiere();

        matiereDB.setIntitule(matiereDTO.getIntitule());
        matiereDB.setCategorieMatiere(this.categorieMatiereRepository.findById(matiereDTO.getCategorieMatiere()).orElse(null));

        this.matiereRepository.save(matiereDB);

        return ResponseEntity.ok(new ServerResponse("Nouvelle categorie matiere", true));
    }

    @Override
    public ResponseEntity<List<CategorieMatiere>> findAllCatMatiere() {
        return ResponseEntity.ok(this.categorieMatiereRepository.findAll());
    }

    @Override
    public ResponseEntity<ServerResponse> createCategorieMatiere(String categorie) throws JsonProcessingException {
        CategorieMatiereDTO categorieMatiereDTO = new ObjectMapper().readValue(categorie, CategorieMatiereDTO.class);

        CategorieMatiere categorieMatiere = new CategorieMatiere();
        categorieMatiere.setIntitule(categorieMatiereDTO.getIntitule());
        categorieMatiere.setSection(this.sectionRepository.findById(categorieMatiereDTO.getSection()).orElse(null));

        this.categorieMatiereRepository.save(categorieMatiere);
        return ResponseEntity.ok(new ServerResponse("nouvelle categorie matiere", true));
    }

}
