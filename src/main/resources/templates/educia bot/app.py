from flask import Flask, request, jsonify, render_template_string
from sentence_transformers import SentenceTransformer, util
import PyPDF2
import docx
import re
import io
from werkzeug.utils import secure_filename
import numpy as np

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # Max 16MB

# Chargement du modèle de transformers (une seule fois au démarrage)
print("Chargement du modèle Sentence-BERT...")
model = SentenceTransformer('OrdalieTech/Solon-embeddings-large-0.1')
print("Modèle chargé avec succès!")

class CVParser:
    """Extraction des sections du CV"""
    
    @staticmethod
    def extraire_texte_pdf(file):
        """Extrait le texte d'un PDF"""
        try:
            pdf_reader = PyPDF2.PdfReader(file)
            texte = ""
            for page in pdf_reader.pages:
                texte += page.extract_text() + "\n"
            return texte
        except Exception as e:
            raise Exception(f"Erreur lecture PDF: {str(e)}")
    
    @staticmethod
    def extraire_texte_docx(file):
        """Extrait le texte d'un DOCX"""
        try:
            doc = docx.Document(file)
            texte = "\n".join([paragraph.text for paragraph in doc.paragraphs])
            return texte
        except Exception as e:
            raise Exception(f"Erreur lecture DOCX: {str(e)}")
    
    @staticmethod
    def extraire_texte(file):
        """Détecte le format et extrait le texte"""
        filename = secure_filename(file.filename).lower()
        
        if filename.endswith('.pdf'):
            return CVParser.extraire_texte_pdf(file)
        elif filename.endswith('.docx'):
            return CVParser.extraire_texte_docx(file)
        elif filename.endswith('.txt'):
            return file.read().decode('utf-8')
        else:
            raise Exception("Format non supporté. Utilisez PDF, DOCX ou TXT")
    
    @staticmethod
    def extraire_section(texte, mots_cles):
        """Extrait une section basée sur des mots-clés"""
        texte_lower = texte.lower()
        lignes = texte.split('\n')
        
        debut_idx = -1
        for i, ligne in enumerate(lignes):
            ligne_lower = ligne.lower()
            if any(mc in ligne_lower for mc in mots_cles):
                debut_idx = i
                break
        
        if debut_idx == -1:
            return ""
        
        # Sections de fin possibles
        sections_fin = [
            'expérience', 'experience', 'compétence', 'competence',
            'formation', 'diplôme', 'diplome', 'éducation', 'education',
            'langues', 'centres d\'intérêt', 'loisirs', 'références'
        ]
        
        fin_idx = len(lignes)
        for i in range(debut_idx + 1, len(lignes)):
            ligne_lower = lignes[i].lower().strip()
            if any(sf in ligne_lower for sf in sections_fin) and len(ligne_lower) < 50:
                fin_idx = i
                break
        
        contenu = '\n'.join(lignes[debut_idx:fin_idx])
        return contenu.strip()
    
    @staticmethod
    def parser_cv(texte_cv):
        """Parse le CV et extrait les 3 sections"""
        sections = {
            'experience': CVParser.extraire_section(texte_cv, [
                'expérience professionnelle', 'experience professionnelle',
                'expériences', 'experiences', 'parcours professionnel',
                'expérience', 'experience'
            ]),
            'competence': CVParser.extraire_section(texte_cv, [
                'compétences', 'competences', 'compétence', 'competence',
                'savoir-faire', 'skills', 'aptitudes'
            ]),
            'diplome': CVParser.extraire_section(texte_cv, [
                'formation', 'diplômes', 'diplomes', 'diplôme', 'diplome',
                'éducation', 'education', 'parcours académique', 'études'
            ])
        }
        
        return sections


class MatchingService:
    """Service de calcul de similarité avec Transformers"""
    
    def __init__(self, model):
        self.model = model
    
    def calculer_similarite(self, texte1, texte2):
        """
        Calcule la similarité cosinus entre deux textes
        Retourne un score entre 0 et 100
        """
        if not texte1.strip() or not texte2.strip():
            return 0.0
        
        # Génère les embeddings
        embedding1 = self.model.encode(texte1, convert_to_tensor=True)
        embedding2 = self.model.encode(texte2, convert_to_tensor=True)
        
        # Calcule la similarité cosinus
        similarite = util.cos_sim(embedding1, embedding2).item()
        
        # Convertit en score sur 100
        score = max(0, min(100, similarite * 100))
        
        return round(score, 2)
    
    def calculer_matching_cv_offre(self, sections_cv, offre):
        """
        Calcule le matching entre CV et offre
        
        Args:
            sections_cv: dict avec 'experience', 'competence', 'diplome'
            offre: dict avec les mêmes clés
        
        Returns:
            dict avec scores détaillés
        """
        # Calcul des scores individuels
        score_competence = self.calculer_similarite(
            sections_cv['competence'], 
            offre['competence']
        )
        
        score_experience = self.calculer_similarite(
            sections_cv['experience'], 
            offre['experience']
        )
        
        score_diplome = self.calculer_similarite(
            sections_cv['diplome'], 
            offre['diplome']
        )
        
        # Formule de scoring final
        score_final = (
            score_competence * 0.5 + 
            score_experience * 0.3 + 
            score_diplome * 0.2
        )
        
        return {
            'score_final': round(score_final, 2),
            'scores_details': {
                'competence': score_competence,
                'experience': score_experience,
                'diplome': score_diplome
            },
            'ponderations': {
                'competence': 50,
                'experience': 30,
                'diplome': 20
            }
        }


# Initialisation du service
matching_service = MatchingService(model)


@app.route('/')
def index():
    """Page HTML de test"""
    return render_template_string(HTML_TEMPLATE)


@app.route('/api/matching', methods=['POST'])
def matching_cv_offre():
    """
    API POST pour calculer le matching CV/Offre
    
    Form-data:
        - cv_file: Fichier CV (PDF, DOCX, TXT)
        - offre_competence: Texte des compétences requises
        - offre_experience: Texte de l'expérience requise
        - offre_diplome: Texte du diplôme requis
    """
    try:
        # Vérification du fichier CV
        if 'cv_file' not in request.files:
            return jsonify({'error': 'Fichier CV manquant'}), 400
        
        cv_file = request.files['cv_file']
        if cv_file.filename == '':
            return jsonify({'error': 'Aucun fichier sélectionné'}), 400
        
        # Extraction du texte du CV
        texte_cv = CVParser.extraire_texte(cv_file)
        
        # Parsing des sections du CV
        sections_cv = CVParser.parser_cv(texte_cv)
        
        # Récupération de l'offre
        offre = {
            'competence': request.form.get('offre_competence', '').strip(),
            'experience': request.form.get('offre_experience', '').strip(),
            'diplome': request.form.get('offre_diplome', '').strip()
        }
        
        # Validation de l'offre
        if not any(offre.values()):
            return jsonify({'error': 'Au moins un critère d\'offre requis'}), 400
        
        # Calcul du matching
        resultats = matching_service.calculer_matching_cv_offre(sections_cv, offre)
        
        # Ajout des sections extraites pour info
        resultats['sections_cv_extraites'] = {
            'competence': sections_cv['competence'][:200] + '...' if len(sections_cv['competence']) > 200 else sections_cv['competence'],
            'experience': sections_cv['experience'][:200] + '...' if len(sections_cv['experience']) > 200 else sections_cv['experience'],
            'diplome': sections_cv['diplome'][:200] + '...' if len(sections_cv['diplome']) > 200 else sections_cv['diplome']
        }
        
        # Interprétation du score
        score_final = resultats['score_final']
        if score_final >= 80:
            interpretation = "Excellent match - Candidat fortement recommandé"
        elif score_final >= 60:
            interpretation = "Bon match - Candidat qualifié"
        elif score_final >= 40:
            interpretation = "Match moyen - À considérer avec réserve"
        else:
            interpretation = "Faible match - Candidat peu adapté"
        
        resultats['interpretation'] = interpretation
        
        return jsonify({
            'success': True,
            'data': resultats
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/api/health', methods=['GET'])
def health_check():
    """Endpoint de santé"""
    return jsonify({
        'status': 'healthy',
        'model': 'paraphrase-multilingual-MiniLM-L12-v2',
        'version': '1.0'
    })


# Template HTML pour tester l'API
HTML_TEMPLATE = '''
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test API Matching CV/Offre</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 900px;
            margin: 0 auto;
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            overflow: hidden;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .header p {
            opacity: 0.9;
            font-size: 1.1em;
        }
        
        .content {
            padding: 40px;
        }
        
        .section {
            margin-bottom: 30px;
        }
        
        .section h2 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 1.5em;
            border-bottom: 2px solid #667eea;
            padding-bottom: 10px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        label {
            display: block;
            font-weight: 600;
            margin-bottom: 8px;
            color: #333;
        }
        
        input[type="file"] {
            width: 100%;
            padding: 12px;
            border: 2px dashed #667eea;
            border-radius: 8px;
            background: #f8f9ff;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        input[type="file"]:hover {
            border-color: #764ba2;
            background: #f0f1ff;
        }
        
        textarea {
            width: 100%;
            padding: 12px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-family: inherit;
            font-size: 14px;
            resize: vertical;
            transition: border-color 0.3s;
        }
        
        textarea:focus {
            outline: none;
            border-color: #667eea;
        }
        
        button {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 15px 40px;
            font-size: 1.1em;
            font-weight: 600;
            border-radius: 8px;
            cursor: pointer;
            width: 100%;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        
        button:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
        }
        
        button:active {
            transform: translateY(0);
        }
        
        button:disabled {
            background: #ccc;
            cursor: not-allowed;
            transform: none;
        }
        
        .loader {
            display: none;
            text-align: center;
            margin: 20px 0;
        }
        
        .loader.active {
            display: block;
        }
        
        .spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #667eea;
            border-radius: 50%;
            width: 50px;
            height: 50px;
            animation: spin 1s linear infinite;
            margin: 0 auto;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        
        .results {
            display: none;
            margin-top: 30px;
            padding: 25px;
            background: #f8f9ff;
            border-radius: 12px;
            border-left: 5px solid #667eea;
        }
        
        .results.show {
            display: block;
            animation: slideIn 0.5s ease;
        }
        
        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .score-final {
            text-align: center;
            padding: 30px;
            background: white;
            border-radius: 12px;
            margin-bottom: 20px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }
        
        .score-number {
            font-size: 4em;
            font-weight: bold;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        
        .interpretation {
            font-size: 1.2em;
            color: #555;
            margin-top: 10px;
        }
        
        .scores-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 15px;
            margin: 20px 0;
        }
        
        .score-card {
            background: white;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
            box-shadow: 0 3px 10px rgba(0,0,0,0.1);
        }
        
        .score-card h3 {
            color: #667eea;
            font-size: 0.9em;
            margin-bottom: 10px;
            text-transform: uppercase;
        }
        
        .score-card .score {
            font-size: 2em;
            font-weight: bold;
            color: #333;
        }
        
        .score-card .weight {
            font-size: 0.9em;
            color: #999;
            margin-top: 5px;
        }
        
        .error {
            background: #fee;
            border-left: 5px solid #f44;
            padding: 15px;
            border-radius: 8px;
            color: #c33;
            display: none;
        }
        
        .error.show {
            display: block;
        }
        
        .sections-extraites {
            margin-top: 20px;
        }
        
        .section-preview {
            background: white;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        
        .section-preview h4 {
            color: #667eea;
            margin-bottom: 8px;
        }
        
        .section-preview p {
            color: #666;
            font-size: 0.9em;
            line-height: 1.5;
        }
        
        @media (max-width: 768px) {
            .scores-grid {
                grid-template-columns: 1fr;
            }
            
            .header h1 {
                font-size: 1.8em;
            }
            
            .content {
                padding: 20px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎯 Matching CV / Offre</h1>
            <p>Analysez la correspondance entre un CV et une offre d'emploi</p>
        </div>
        
        <div class="content">
            <form id="matchingForm" enctype="multipart/form-data">
                <div class="section">
                    <h2>📄 CV du Candidat</h2>
                    <div class="form-group">
                        <label for="cv_file">Télécharger le CV (PDF, DOCX, TXT)</label>
                        <input type="file" id="cv_file" name="cv_file" accept=".pdf,.docx,.txt" required>
                    </div>
                </div>
                
                <div class="section">
                    <h2>💼 Critères de l'Offre</h2>
                    
                    <div class="form-group">
                        <label for="offre_competence">Compétences requises (50%)</label>
                        <textarea 
                            id="offre_competence" 
                            name="offre_competence" 
                            rows="4" 
                            placeholder="Ex: Python, Flask, Machine Learning, NLP, Transformers, PostgreSQL, REST API..."
                        ></textarea>
                    </div>
                    
                    <div class="form-group">
                        <label for="offre_experience">Expérience requise (30%)</label>
                        <textarea 
                            id="offre_experience" 
                            name="offre_experience" 
                            rows="4" 
                            placeholder="Ex: 3 ans d'expérience en développement backend, projets ML en production, travail en équipe agile..."
                        ></textarea>
                    </div>
                    
                    <div class="form-group">
                        <label for="offre_diplome">Formation / Diplôme requis (20%)</label>
                        <textarea 
                            id="offre_diplome" 
                            name="offre_diplome" 
                            rows="3" 
                            placeholder="Ex: Master en Informatique, Ingénieur Data Science, formation en Intelligence Artificielle..."
                        ></textarea>
                    </div>
                </div>
                
                <button type="submit" id="submitBtn">Analyser le Matching</button>
            </form>
            
            <div class="loader" id="loader">
                <div class="spinner"></div>
                <p>Analyse en cours...</p>
            </div>
            
            <div class="error" id="error"></div>
            
            <div class="results" id="results">
                <div class="score-final">
                    <div class="score-number" id="scoreFinal">--</div>
                    <div class="interpretation" id="interpretation"></div>
                </div>
                
                <h3 style="color: #667eea; margin-bottom: 15px;">Détail des Scores</h3>
                <div class="scores-grid">
                    <div class="score-card">
                        <h3>Compétences</h3>
                        <div class="score" id="scoreCompetence">--</div>
                        <div class="weight">Poids: 50%</div>
                    </div>
                    <div class="score-card">
                        <h3>Expérience</h3>
                        <div class="score" id="scoreExperience">--</div>
                        <div class="weight">Poids: 30%</div>
                    </div>
                    <div class="score-card">
                        <h3>Diplôme</h3>
                        <div class="score" id="scoreDiplome">--</div>
                        <div class="weight">Poids: 20%</div>
                    </div>
                </div>
                
                <div class="sections-extraites">
                    <h3 style="color: #667eea; margin-bottom: 15px;">Sections Extraites du CV</h3>
                    <div class="section-preview">
                        <h4>💼 Compétences</h4>
                        <p id="extractCompetence">...</p>
                    </div>
                    <div class="section-preview">
                        <h4>📊 Expérience</h4>
                        <p id="extractExperience">...</p>
                    </div>
                    <div class="section-preview">
                        <h4>🎓 Diplôme</h4>
                        <p id="extractDiplome">...</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        const form = document.getElementById('matchingForm');
        const loader = document.getElementById('loader');
        const results = document.getElementById('results');
        const error = document.getElementById('error');
        const submitBtn = document.getElementById('submitBtn');
        
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            // Reset
            results.classList.remove('show');
            error.classList.remove('show');
            loader.classList.add('active');
            submitBtn.disabled = true;
            
            // Prépare FormData
            const formData = new FormData(form);
            
            try {
                const response = await fetch('/api/matching', {
                    method: 'POST',
                    body: formData
                });
                
                const data = await response.json();
                
                if (response.ok && data.success) {
                    afficherResultats(data.data);
                } else {
                    afficherErreur(data.error || 'Erreur inconnue');
                }
            } catch (err) {
                afficherErreur('Erreur de connexion: ' + err.message);
            } finally {
                loader.classList.remove('active');
                submitBtn.disabled = false;
            }
        });
        
        function afficherResultats(data) {
            // Score final
            document.getElementById('scoreFinal').textContent = data.score_final + '/100';
            document.getElementById('interpretation').textContent = data.interpretation;
            
            // Scores détaillés
            document.getElementById('scoreCompetence').textContent = data.scores_details.competence;
            document.getElementById('scoreExperience').textContent = data.scores_details.experience;
            document.getElementById('scoreDiplome').textContent = data.scores_details.diplome;
            
            // Sections extraites
            document.getElementById('extractCompetence').textContent = 
                data.sections_cv_extraites.competence || 'Aucune section trouvée';
            document.getElementById('extractExperience').textContent = 
                data.sections_cv_extraites.experience || 'Aucune section trouvée';
            document.getElementById('extractDiplome').textContent = 
                data.sections_cv_extraites.diplome || 'Aucune section trouvée';
            
            results.classList.add('show');
        }
        
        function afficherErreur(message) {
            error.textContent = '❌ ' + message;
            error.classList.add('show');
        }
    </script>
</body>
</html>
'''

if __name__ == '__main__':
    print("\n" + "="*60)
    print("🚀 API Matching CV/Offre avec Transformers")
    print("="*60)
    print("📍 URL: http://localhost:5000")
    print("📍 API: POST http://localhost:5000/api/matching")
    print("📍 Test: http://localhost:5000")
    print("="*60 + "\n")
    
    app.run(debug=True, host='0.0.0.0', port=5000)