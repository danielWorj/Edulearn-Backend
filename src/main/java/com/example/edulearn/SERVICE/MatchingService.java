package com.example.edulearn.SERVICE;



import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;

import com.example.edulearn.REPOSITORY.Repetition.OffreRepetitionRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.EnseignantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MatchingService {
    public final ChatClient chatClient;

    public MatchingService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    /**
     * Génère le prompt de matching entre un enseignant et une offre
     */
    public String generateMatchingPrompt(Enseignant enseignant, OffreRepetition offre) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Tu es un expert en matching pour des offres de répétition scolaire. ");
        prompt.append("Analyse la compatibilité entre cet enseignant et cette offre de répétition, ");
        prompt.append("puis donne UNIQUEMENT un score de compatibilité sous forme de nombre entier ");
        prompt.append("entre 0 et 100 (sans le symbole %).\n\n");

        // INFORMATIONS ENSEIGNANT
        prompt.append("ENSEIGNANT:\n");
        prompt.append("- Expérience: ").append(enseignant.getAnneeexperience() != null ? enseignant.getAnneeexperience() + " ans" : "Non spécifiée").append("\n");
        prompt.append("- Tarif horaire: ").append(enseignant.getTarifHoraire() != null ? enseignant.getTarifHoraire() + " FCFA" : "Non spécifié").append("\n");
        prompt.append("- Spécialité: ").append(enseignant.getSpecialite() != null ? enseignant.getSpecialite() : "Non spécifiée").append("\n");
        prompt.append("- Section: ").append(enseignant.getSection() != null ? enseignant.getSection().getIntitule() : "Non spécifiée").append("\n");
        prompt.append("- Diplôme: ").append(enseignant.getDiplome() != null ? enseignant.getDiplome().getIntitule() : "Non spécifié").append("\n");
        prompt.append("- Bio: ").append(enseignant.getBio() != null ? enseignant.getBio() : "Non renseignée").append("\n");
        prompt.append("- Profil enseignant: ").append(enseignant.getProfilEnseignant() != null ? enseignant.getProfilEnseignant().getIntitule() : "Non spécifié").append("\n");
        prompt.append("- Statut: ").append(enseignant.getStatusEnseignant() != null ? enseignant.getStatusEnseignant().getIntitule() : "Actif").append("\n\n");

        // INFORMATIONS OFFRE
        prompt.append("OFFRE DE RÉPÉTITION:\n");
        prompt.append("- Titre: ").append(offre.getIntitule()).append("\n");
        prompt.append("- Description: ").append(offre.getBio() != null ? offre.getBio() : "Non renseignée").append("\n");
        prompt.append("- Salaire minimum: ").append(offre.getSalaireMin() != null ? offre.getSalaireMin() + " FCFA" : "Non spécifié").append("\n");
        prompt.append("- Salaire maximum: ").append(offre.getSalaireMax() != null ? offre.getSalaireMax() + " FCFA" : "Non spécifié").append("\n");
        prompt.append("- Fréquence: ").append(offre.getFrequence() != null ? offre.getFrequence() + " fois par semaine" : "Non spécifiée").append("\n");
        prompt.append("- Durée: ").append(offre.getDuree() != null ? offre.getDuree() : "Non spécifiée").append("\n");

        prompt.append("- Code: ").append(offre.getCode()).append("\n\n");

        // CRITÈRES D'ÉVALUATION
        prompt.append("CRITÈRES D'ÉVALUATION:\n");
        prompt.append("1. Compatibilité tarifaire (le tarif de l'enseignant est-il dans la fourchette proposée?)\n");
        prompt.append("2. Expérience et expertise (l'enseignant a-t-il l'expérience et les compétences requises?)\n");
        prompt.append("3. Spécialité et section (correspondent-elles aux besoins de l'offre?)\n");
        prompt.append("4. Disponibilité et fréquence (l'enseignant peut-il assurer la fréquence demandée?)\n");
        prompt.append("5. Adéquation du profil général\n\n");

        // INSTRUCTIONS IMPORTANTES
        prompt.append("IMPORTANT:\n");
        prompt.append("- Réponds UNIQUEMENT avec un nombre entier entre 0 et 100\n");
        prompt.append("- N'ajoute AUCUN texte, explication, symbole ou caractère supplémentaire\n");
        prompt.append("- Exemple de réponse valide: 85\n");
        prompt.append("- Exemple de réponse INVALIDE: 85%, Score: 85, Le score est de 85");

        return prompt.toString();
    }

    /**
     * Calcule le score de matching entre un enseignant et une offre
     */
    public Integer calculateMatching(Enseignant enseignant, OffreRepetition offre) {

        // Générer le prompt
        String prompt = generateMatchingPrompt(enseignant, offre);

        //log.debug("Prompt généré: {}", prompt);

        // Appeler l'IA pour obtenir le score
        var completion = Map.of("completion", Objects.requireNonNull(chatClient.prompt().user(prompt).call().content()));
        String iaResponse = completion.get("completion");

        //log.debug("Réponse IA brute: {}", iaResponse);

        // Extraire le score de la réponse
        Integer score = extractScore(iaResponse);

        //log.info("Score de matching calculé: {}", score);

        // Créer la réponse
//        MatchingResponse response = new MatchingResponse();
//        response.setEnseignantId(enseignantId);
//        response.setEnseignantNom(enseignant.getNom() + " " + enseignant.getPrenom());
//        response.setOffreId(offreId);
//        response.setOffreIntitule(offre.getIntitule());
//        response.setScore(score);
//        response.setNiveau(determineNiveau(score));
//        response.setMessage(generateMessage(score));

        return score;
    }

    /**
     * Extrait le score numérique de la réponse de l'IA
     */
    private Integer extractScore(String response) {
        if (response == null || response.trim().isEmpty()) {
            System.out.println("Réponse IA vide, retour du score par défaut: 0");
            return 0;
        }

        // Nettoyer la réponse
        String cleaned = response.trim().replaceAll("%", "");

        // Extraire le premier nombre trouvé
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(cleaned);

        if (matcher.find()) {
            int score = Integer.parseInt(matcher.group());
            // S'assurer que le score est entre 0 et 100
            score = Math.min(Math.max(score, 0), 100);
            return score;
        }

        System.out.println("Impossible d'extraire le score de la réponse: {}"+response);
        return 0;
    }

    /**
     * Détermine le niveau de matching selon le score
     */
    private String determineNiveau(Integer score) {
        if (score >= 80) {
            return "EXCELLENT";
        } else if (score >= 60) {
            return "BON";
        } else if (score >= 40) {
            return "MOYEN";
        } else {
            return "FAIBLE";
        }
    }

    /**
     * Génère un message selon le score
     */
    private String generateMessage(Integer score) {
        if (score >= 80) {
            return "Excellent match! Cet enseignant est très compatible avec cette offre.";
        } else if (score >= 60) {
            return "Bon match. Cet enseignant correspond bien aux critères de l'offre.";
        } else if (score >= 40) {
            return "Match moyen. Quelques points de compatibilité existent.";
        } else {
            return "Faible compatibilité avec cette offre.";
        }
    }
}