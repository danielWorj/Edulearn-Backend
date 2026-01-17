package com.example.edulearn.SERVICE;

import com.example.edulearn.DTO.IA.ScoreMatch;
import com.example.edulearn.ENTITY.Repetition.MatiereOffre;
import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchingConfirmService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String PYTHON_API_URL = "http://localhost:5000/api/matching/enseignant";
    private static String folderFile = System.getProperty("user.dir")+"/src/main/resources/templates/dashboard/public/assets/file"; //chemin a déinir


    public MatchingConfirmService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Appelle l'API Python de matching et retourne le score
     *
     * @return ScoreMatch contenant les résultats du matching
     * @throws Exception si l'appel échoue
     */
    public ScoreMatch calculerMatchingEnseignant(MatiereOffre matiereOffre , Enseignant enseignant) throws Exception {
        // Données en dur
        String matiere = matiereOffre.getMatiere().getIntitule();
        String niveau = matiereOffre.getOffreRepetition().getEleve().getNiveau().getIntitule();
//        String description = "Mon enfant a besoin d'aide en Physique pour préparer le baccalauréat. "
//                + "Il a des difficultés particulières en mécanique et en électricité. "
//                + "Nous recherchons un enseignant expérimenté capable de l'aider à combler ses lacunes "
//                + "et de le préparer efficacement aux épreuves.";
        String description = matiereOffre.getOffreRepetition().getBio();

        String besoins = "Préparation intensive au Baccalauréat, remise à niveau en mécanique";
        //String cheminCV = "D:/10 Apps/EduOnline/Edu Learn/edulearn/src/main/resources/templates/dashboard/public/assets/file/quitus.pdf";
        String cheminCV = "templates/dashboard/public/assets/file/"+enseignant.getCv();

        // Chargement du fichier CV depuis le dossier templates/cv
        Resource cvResource = new ClassPathResource(cheminCV);

        if (!cvResource.exists()) {
            throw new IOException("Fichier CV introuvable : " + cheminCV);
        }

        // Préparation de la requête multipart/form-data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("cv_enseignant", cvResource);
        body.add("matiere", matiere);
        body.add("niveau", niveau);
        body.add("description", description);
        body.add("besoins", besoins);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // Appel de l'API Python
            ResponseEntity<String> response = restTemplate.exchange(
                    PYTHON_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Parsing de la réponse JSON
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            if (rootNode.has("success") && rootNode.get("success").asBoolean()) {
                JsonNode data = rootNode.get("data");

                // Extraction des données
                double scoreRecupere = data.get("score_final").asDouble();
                String nomEnseignant = extraireNomDepuisCV(cheminCV);

                // Construction de l'objet de retour
                return new ScoreMatch(nomEnseignant, matiere, description, scoreRecupere);

            } else {
                String errorMsg = rootNode.has("error") ? rootNode.get("error").asText() : "Erreur inconnue";
                throw new Exception("Erreur API Python : " + errorMsg);
            }

        } catch (Exception e) {
            throw new Exception("Échec de l'appel à l'API de matching : " + e.getMessage(), e);
        }
    }

    /**
     * Appelle l'API Python de matching batch pour plusieurs enseignants
     *
     * @param matiereOffre L'offre de répétition avec matière et niveau
     * @param enseignants Liste des enseignants à matcher
     * @return Liste de ScoreMatch triée par score décroissant
     * @throws Exception si l'appel échoue
     */
    public List<ScoreMatch> calculerMatchingMultipleEnseignant(
            MatiereOffre matiereOffre,
            List<Enseignant> enseignants
    ) throws Exception {

        // Données de l'offre
        String matiere = matiereOffre.getMatiere().getIntitule();
        String niveau = matiereOffre.getOffreRepetition().getEleve().getNiveau().getIntitule();
        String description = matiereOffre.getOffreRepetition().getBio();
        String besoins = "Préparation intensive, remise à niveau"; // Adapter selon besoin

        // Préparation de la requête multipart/form-data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Ajout des paramètres de l'offre
        body.add("matiere", matiere);
        body.add("niveau", niveau);
        body.add("description", description);
        body.add("besoins", besoins);

        // Construction de la liste des noms pour l'API Python
        List<String> nomsEnseignants = new ArrayList<>();

        // Ajout des fichiers CV et noms
        for (Enseignant enseignant : enseignants) {
            String cheminCV = "templates/dashboard/public/assets/file/" + enseignant.getCv();

            Resource cvResource = new ClassPathResource(cheminCV);

            if (!cvResource.exists()) {
                System.err.println("⚠️ CV introuvable pour " + enseignant.getNomComplet() + " : " + cheminCV);
                // Continue avec les autres même si un CV manque
                continue;
            }

            // Ajout du fichier CV (clé identique pour tous : "cv_files")
            body.add("cv_files", cvResource);

            // Ajout du nom complet de l'enseignant
            String nomComplet = enseignant.getNomComplet();
            nomsEnseignants.add(nomComplet);
        }

        // Vérification qu'au moins un CV est disponible
        if (nomsEnseignants.isEmpty()) {
            throw new Exception("Aucun CV valide trouvé pour les enseignants");
        }

        // Ajout de la liste des noms (format: "Nom1,Nom2,Nom3")
        String nomsConcat = String.join(",", nomsEnseignants);
        body.add("noms_enseignants", nomsConcat);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // Appel de l'API Python batch
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:5000/api/matching/batch",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Parsing de la réponse JSON
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            if (rootNode.has("success") && rootNode.get("success").asBoolean()) {
                JsonNode resultatsNode = rootNode.get("resultats");

                List<ScoreMatch> scores = new ArrayList<>();

                // Parcours des résultats
                for (JsonNode resultat : resultatsNode) {
                    String nomEnseignant = resultat.get("nom_enseignant").asText() ;
                    String matiereResult = resultat.get("matiere").asText();
                    double score = resultat.get("score").asDouble();
                    String interpretation = resultat.get("interpretation").asText();

                    // Création de l'objet ScoreMatch
                    ScoreMatch scoreMatch = new ScoreMatch(
                            nomEnseignant,
                            matiereResult,
                            interpretation,
                            score
                    );

                    scores.add(scoreMatch);
                }

                // Les résultats sont déjà triés par l'API Python (ordre décroissant)
                System.out.println("✅ Matching batch réussi : " + scores.size() + " enseignants analysés");

                return scores;

            } else {
                String errorMsg = rootNode.has("error") ?
                        rootNode.get("error").asText() : "Erreur inconnue";
                throw new Exception("Erreur API Python batch : " + errorMsg);
            }

        } catch (Exception e) {
            throw new Exception("Échec de l'appel à l'API de matching batch : " + e.getMessage(), e);
        }
    }
    /**
     * Extrait le nom de l'enseignant depuis le nom du fichier CV
     */
    private String extraireNomDepuisCV(String cheminCV) {
        String nomFichier = cheminCV.substring(cheminCV.lastIndexOf("/") + 1);
        nomFichier = nomFichier.replaceAll("\\.(pdf|docx|txt)$", "");
        nomFichier = nomFichier.replace("_", " ");
        nomFichier = nomFichier.replace(" CONTRACT", "").trim();

        return nomFichier;
    }
}

// Classe ScoreMatch
