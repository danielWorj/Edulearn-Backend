package com.example.edulearn.SERVICE;



import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class IaService {
    public final ChatClient chatClient;

    public IaService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateResumeForJOffre(OffreRepetition offre) {

        String synthesizedData = synthesizeOffreForPrompt(offre);

        String fullPrompt = synthesizedData +
                "\n\nPeux-tu me faire un résumé concis et attractif de cette offre de repetition " +
                "en mettant en avant les points clés qui intéresseraient un enseignant  " +
                "Le résumé doit faire environ 5-6 phrases.";

        var completion = Map.of("completion", Objects.requireNonNull(chatClient.prompt().user(fullPrompt).call().content()));

        return completion.get("completion");


    }

    public String getScoreMatchingOffreEnseignant(OffreRepetition job, String cvText){
        String synthesizedData = synthesizeOffreForPrompt(job);

        String fullPrompt =  synthesizedData.toString()+
                "\n\n Donne moi le score pour que ce cv: + " + cvText +
                " soit un bon cv pour cet offre de repetition : "+ synthesizedData +". Tu me renvoi un nombre entre 0 et 100.   ";

        var completion = Map.of("completion", Objects.requireNonNull(chatClient.prompt().user(fullPrompt).call().content()));

        return completion.get("completion");
    }



    public String pointsAmeliorationCvAndOffre(OffreRepetition job, String cvText){
        String synthesizedData = synthesizeOffreForPrompt(job);

        String fullPrompt =  synthesizedData.toString()+
                "\n\n Donne moi une liste de 05 points a ameliorer sur le cv suivant : + " + cvText +
                " Pour que ce cv soit un bon cv pour cet offre d'emploi. Tu me renvoi un Json de la liste de ces points    " +
                "L'objet pointAmelioration a un seul attribut intitule. Il n'y a pas de texte avant ou apres le json de la reponse. Mets juste le contenu du json. ";

        var completion = Map.of("completion", Objects.requireNonNull(chatClient.prompt().user(fullPrompt).call().content()));

        return completion.get("completion");
    }

    public String assistantTextuel(String prompt){
        var completion = Map.of("completion", Objects.requireNonNull(chatClient.prompt().user(prompt).call().content()));

        return completion.get("completion");
    }

    public String getScoreMatching(OffreRepetition job , Enseignant enseignant){
        String synthesizedOffre = synthesizeOffreForPrompt(job);
        String synthesizedEnseignant = synthesizeEnseignantForPrompt(enseignant);

        String fullPrompt = synthesizedOffre +
                "\n\nTu es un système de matching pour une plateforme de cours à domicile. \n" +
                "Analyse la compatibilité entre le profil de l'enseignant "+synthesizedEnseignant +"et l'offre de cours." + synthesizedEnseignant
                +"Tu me renvoies juste le pourcentage de correspondance . Par exemple 75. Seulement le chiffre correspondant au pourcentage. ";
        var completion = Map.of("completion", Objects.requireNonNull(chatClient.prompt().user(fullPrompt).call().content()));

        return completion.get("completion");

    }

    public String synthesizeOffreForPrompt(OffreRepetition job) {

        StringBuilder prompt = new StringBuilder();

        // 1. Informations principales
        prompt.append("Voici une offre de repetition à analyser :\n\n");
        prompt.append("**Titre du poste :** ").append(job.getIntitule()).append("\n");

        // 2. Description
        prompt.append("**Description du poste :**\n");
        prompt.append(job.getBio());

        // 3. Salaires
        prompt.append("**Salaires min et max :**\n");
        prompt.append(job.getSalaireMin());
        prompt.append(job.getSalaireMax());

        return prompt.toString();
    }

    public String synthesizeEnseignantForPrompt(Enseignant enseignant) {

        StringBuilder prompt = new StringBuilder();

        // 1. Informations principales
        prompt.append("Voici un enseignant à analyser :\n\n");
        prompt.append("**Bio:** \n").append(enseignant.getBio()).append("\n");
        prompt.append("**Diplome:**\n").append(enseignant.getDiplome().getIntitule());
        prompt.append("**Specialite du diplome**\n").append(enseignant.getSpecialite());


        // 2. Description
        prompt.append("**Tarif horaire :**\n");
        prompt.append(enseignant.getTarifHoraire());
        prompt.append("**Annee d'experience**\n");
        prompt.append(enseignant.getAnneeexperience());


        return prompt.toString();
    }


    // Dans votre service Angular ou composant


}
