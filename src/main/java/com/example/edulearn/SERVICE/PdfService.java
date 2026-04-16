package com.example.edulearn.SERVICE;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfService {
    public String extractAndCleanText(MultipartFile file) throws IOException {
        // 1. Vérification si le fichier est vide
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide.");
        }

        // 2. Vérification du type MIME (Doit être un PDF)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Le fichier doit être au format PDF.");
        }

        // 3. Chargement du document et extraction
        // On utilise try-with-resources pour s'assurer que le document est bien fermé après usage
        try (PDDocument document = PDDocument.load(file.getInputStream())) {

            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document);
            //String allText = stripper.get
            //System.out.println(rawText);

            // 4. Nettoyage du texte (Enlever les caractères spéciaux)
            return rawText;
        }
    }

    private static final String STORAGE_DIRECTORY = "D:/10 Apps/EduOnline/Edu Learn/edulearn/src/main/resources/templates/platform/public/assets/file";

    //private static final String STORAGE_DIRECTORY = "C:\\Users\\Utilisateur\\Documents\\Emploi";
    public String extractTextFromLocalFile(String filename) throws IOException {

        // 1. Sécurité anti "Path Traversal" :
        // On s'assure que le nom de fichier ne contient pas de chemin relatif (../)
        // pour empêcher l'accès aux fichiers système.
        Path storagePath = Paths.get(STORAGE_DIRECTORY).toAbsolutePath().normalize();
        System.out.println("path file"+ storagePath);
        Path targetPath = storagePath.resolve(filename).normalize();

        if (!targetPath.startsWith(storagePath)) {
            throw new IllegalArgumentException("Nom de fichier invalide ou tentative d'accès non autorisé.");
        }

        File file = targetPath.toFile();

        // 2. Vérifications de base
        if (!file.exists()) {
            throw new IOException("Le fichier n'existe pas : " + filename);
        }
        if (!file.isFile()) {
            throw new IOException("Le chemin ne correspond pas à un fichier.");
        }
        // Vérification basique de l'extension
        if (!filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Le fichier cible n'est pas un PDF.");
        }

        // 3. Extraction (Même logique robuste que précédemment)
        try (PDDocument document = PDDocument.load(file)) {

            if (document.isEncrypted()) {
                throw new IOException("Le fichier PDF est protégé par mot de passe.");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());

            String rawText = stripper.getText(document);

            return cleanText(rawText);
        }
    }

    private String cleanText(String text) {
        if (text == null) return "";

        // Remplacer les sauts de ligne par des espaces
        String oneLine = text.replaceAll("\\r\\n|\\r|\\n", " ");

        // Regex: Garder uniquement les lettres (a-z, A-Z), les chiffres (0-9) et les espaces.
        // Tout le reste (ponctuation, symboles) est supprimé.
        String cleaned = oneLine.replaceAll("[^a-zA-Z0-9\\sàâäéèêëîïôöùûüç]", "");


        // Remplacer les espaces multiples par un seul espace et trimmer
        return cleaned.replaceAll("\\s+", " ").trim();

    }
}
