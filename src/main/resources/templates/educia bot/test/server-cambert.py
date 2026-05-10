from flask import Flask, request, jsonify, render_template_string
from sentence_transformers import SentenceTransformer, util
from flask_cors import CORS
import PyPDF2
import docx
import os

app = Flask(__name__)
CORS(app) 

# Modèles recommandés selon le contexte :
# Pour CV francophones généraux (MEILLEUR CHOIX) :
#model = SentenceTransformer('dangvantuan/sentence-camembert-large')
# Le plus performant pour le français
#model = SentenceTransformer('Sahajtomar/french_semantic')
#model = SentenceTransformer('OrdalieTech/Solon-embeddings-large-0.1')
model = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
# Alternative multilingue rapide :
# model = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')

# Pour documents juridiques uniquement :
# model = SentenceTransformer('OrdalieTech/Solon-embeddings-large-0.1')

CV_FOLDER = 'cvs'

# Template HTML
HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CV Matcher - Analyse de Correspondance</title>
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
            font-size: 1.1em;
            opacity: 0.9;
        }
        
        .content {
            padding: 40px;
        }
        
        .form-group {
            margin-bottom: 30px;
        }
        
        label {
            display: block;
            font-weight: 600;
            margin-bottom: 10px;
            color: #333;
            font-size: 1.1em;
        }
        
        textarea {
            width: 100%;
            padding: 15px;
            border: 2px solid #e0e0e0;
            border-radius: 10px;
            font-size: 1em;
            font-family: inherit;
            resize: vertical;
            transition: border-color 0.3s;
        }
        
        textarea:focus {
            outline: none;
            border-color: #667eea;
        }
        
        .file-upload {
            position: relative;
            display: inline-block;
            width: 100%;
        }
        
        .file-upload input[type="file"] {
            display: none;
        }
        
        .file-upload-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            background: #f8f9fa;
            border: 2px dashed #667eea;
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .file-upload-btn:hover {
            background: #e7e9fc;
            border-color: #764ba2;
        }
        
        .file-upload-btn i {
            font-size: 2em;
            margin-right: 15px;
            color: #667eea;
        }
        
        .file-name {
            margin-top: 10px;
            padding: 10px;
            background: #e7f3ff;
            border-radius: 5px;
            color: #0066cc;
            display: none;
        }
        
        .submit-btn {
            width: 100%;
            padding: 18px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 1.2em;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        
        .submit-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
        }
        
        .submit-btn:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            transform: none;
        }
        
        .result {
            margin-top: 30px;
            padding: 30px;
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            border-radius: 15px;
            display: none;
        }
        
        .score-display {
            text-align: center;
            margin-bottom: 20px;
        }
        
        .score-circle {
            display: inline-block;
            width: 150px;
            height: 150px;
            border-radius: 50%;
            background: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 3em;
            font-weight: bold;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            margin: 20px auto;
        }
        
        .interpretation {
            text-align: center;
            font-size: 1.5em;
            font-weight: 600;
            margin-top: 15px;
        }
        
        .details {
            margin-top: 20px;
            padding: 20px;
            background: white;
            border-radius: 10px;
        }
        
        .detail-item {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid #e0e0e0;
        }
        
        .detail-item:last-child {
            border-bottom: none;
        }
        
        .error {
            background: #fee;
            color: #c33;
            padding: 20px;
            border-radius: 10px;
            margin-top: 20px;
            display: none;
        }
        
        .loader {
            display: none;
            text-align: center;
            margin-top: 20px;
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
        
        .score-excellent { color: #28a745; }
        .score-good { color: #17a2b8; }
        .score-average { color: #ffc107; }
        .score-low { color: #dc3545; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎯 CV Matcher</h1>
            <p>Analysez la correspondance entre une offre d'emploi et un CV</p>
        </div>
        
        <div class="content">
            <form id="matchForm" enctype="multipart/form-data">
                <div class="form-group">
                    <label for="jobDescription">📋 Description de l'offre d'emploi</label>
                    <textarea 
                        id="jobDescription" 
                        name="job_description" 
                        rows="8" 
                        placeholder="Collez ici la description complète de l'offre d'emploi (compétences requises, expérience, responsabilités...)"
                        required
                    ></textarea>
                </div>
                
                <div class="form-group">
                    <label>📄 Fichier CV</label>
                    <div class="file-upload">
                        <input type="file" id="cvFile" name="cv_file" accept=".pdf,.docx,.txt" required>
                        <label for="cvFile" class="file-upload-btn">
                            <span style="font-size: 2em;">📎</span>
                            <span>Cliquez pour sélectionner un CV (PDF, DOCX, TXT)</span>
                        </label>
                    </div>
                    <div class="file-name" id="fileName"></div>
                </div>
                
                <button type="submit" class="submit-btn">
                    Analyser la correspondance
                </button>
            </form>
            
            <div class="loader" id="loader">
                <div class="spinner"></div>
                <p style="margin-top: 15px; color: #667eea; font-weight: 600;">
                    Analyse en cours...
                </p>
            </div>
            
            <div class="error" id="error"></div>
            
            <div class="result" id="result">
                <div class="score-display">
                    <div class="score-circle" id="scoreCircle">-</div>
                    <div class="interpretation" id="interpretation"></div>
                </div>
                
                <div class="details">
                    <div class="detail-item">
                        <span><strong>Fichier CV:</strong></span>
                        <span id="cvFilename">-</span>
                    </div>
                    <div class="detail-item">
                        <span><strong>Longueur CV:</strong></span>
                        <span id="cvLength">-</span>
                    </div>
                    <div class="detail-item">
                        <span><strong>Longueur offre:</strong></span>
                        <span id="jobLength">-</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        const form = document.getElementById('matchForm');
        const fileInput = document.getElementById('cvFile');
        const fileName = document.getElementById('fileName');
        const loader = document.getElementById('loader');
        const result = document.getElementById('result');
        const error = document.getElementById('error');
        
        // Afficher le nom du fichier sélectionné
        fileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                fileName.textContent = `📄 ${e.target.files[0].name}`;
                fileName.style.display = 'block';
            }
        });
        
        // Soumettre le formulaire
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            // Réinitialiser l'affichage
            result.style.display = 'none';
            error.style.display = 'none';
            loader.style.display = 'block';
            
            const formData = new FormData();
            formData.append('job_description', document.getElementById('jobDescription').value);
            formData.append('cv_file', fileInput.files[0]);
            
            try {
                const response = await fetch('/match', {
                    method: 'POST',
                    body: formData
                });
                
                const data = await response.json();
                
                loader.style.display = 'none';
                
                if (response.ok) {
                    // Afficher les résultats
                    displayResults(data);
                } else {
                    // Afficher l'erreur
                    error.textContent = data.error || 'Une erreur est survenue';
                    error.style.display = 'block';
                }
            } catch (err) {
                loader.style.display = 'none';
                error.textContent = 'Erreur de connexion au serveur';
                error.style.display = 'block';
            }
        });
        
        function displayResults(data) {
            const score = data.match_score;
            const scoreCircle = document.getElementById('scoreCircle');
            const interpretation = document.getElementById('interpretation');
            
            // Afficher le score
            scoreCircle.textContent = score + '%';
            
            // Déterminer la couleur selon le score
            scoreCircle.className = 'score-circle';
            interpretation.className = 'interpretation';
            
            if (score >= 80) {
                scoreCircle.classList.add('score-excellent');
                interpretation.classList.add('score-excellent');
            } else if (score >= 60) {
                scoreCircle.classList.add('score-good');
                interpretation.classList.add('score-good');
            } else if (score >= 40) {
                scoreCircle.classList.add('score-average');
                interpretation.classList.add('score-average');
            } else {
                scoreCircle.classList.add('score-low');
                interpretation.classList.add('score-low');
            }
            
            interpretation.textContent = data.interpretation;
            
            // Afficher les détails
            document.getElementById('cvFilename').textContent = data.cv_filename;
            document.getElementById('cvLength').textContent = data.cv_length + ' caractères';
            document.getElementById('jobLength').textContent = data.job_description_length + ' caractères';
            
            result.style.display = 'block';
        }
    </script>
</body>
</html>
"""

def extract_text_from_pdf(pdf_path):
    """Extraire le texte d'un fichier PDF"""
    text = ""
    try:
        with open(pdf_path, 'rb') as file:
            pdf_reader = PyPDF2.PdfReader(file)
            for page in pdf_reader.pages:
                text += page.extract_text()
    except Exception as e:
        print(f"Erreur lors de la lecture du PDF: {e}")
    return text

def extract_text_from_docx(docx_path):
    """Extraire le texte d'un fichier DOCX"""
    text = ""
    try:
        doc = docx.Document(docx_path)
        for para in doc.paragraphs:
            text += para.text + "\n"
    except Exception as e:
        print(f"Erreur lors de la lecture du DOCX: {e}")
    return text

def extract_text_from_txt(txt_path):
    """Extraire le texte d'un fichier TXT"""
    try:
        with open(txt_path, 'r', encoding='utf-8') as file:
            return file.read()
    except Exception as e:
        print(f"Erreur lors de la lecture du TXT: {e}")
        return ""

def calculate_keyword_match(job_description, cv_text):
    """Calculer un score basé sur les mots-clés communs"""
    from collections import Counter
    import re
    
    # Normaliser et tokeniser
    job_words = set(re.findall(r'\b\w{4,}\b', job_description.lower()))
    cv_words = set(re.findall(r'\b\w{4,}\b', cv_text.lower()))
    
    # Mots communs
    common_words = job_words.intersection(cv_words)
    
    # Score de Jaccard
    if len(job_words.union(cv_words)) == 0:
        return 0
    
    jaccard_score = len(common_words) / len(job_words.union(cv_words))
    return jaccard_score * 100

def preprocess_text(text):
    """Nettoyer et normaliser le texte"""
    import re
    # Supprimer les caractères spéciaux excessifs
    text = re.sub(r'[•▪▫◦●○]', '', text)
    # Supprimer les lignes vides multiples
    text = re.sub(r'\n\s*\n', '\n', text)
    # Normaliser les espaces
    text = ' '.join(text.split())
    return text

def extract_keywords(text):
    """Extraire les mots-clés importants"""
    # Mots-clés à mettre en valeur
    keywords = ['expérience', 'compétence', 'formation', 'diplôme', 'master', 
                'licence', 'bac', 'enseignant', 'professeur', 'cours', 'physique',
                'chimie', 'mathématiques', 'pédagogie', 'évaluation']
    
    text_lower = text.lower()
    found_keywords = [kw for kw in keywords if kw in text_lower]
    return ', '.join(found_keywords)

def calculate_match_score(job_description, cv_text):
    """Calculer le score de correspondance (hybride: sémantique + mots-clés)"""
    # Prétraiter les textes
    job_clean = preprocess_text(job_description)
    cv_clean = preprocess_text(cv_text)
    
    # Score sémantique (60% du poids)
    job_embedding = model.encode(job_clean, convert_to_tensor=True)
    cv_embedding = model.encode(cv_clean, convert_to_tensor=True)
    semantic_score = float(util.cos_sim(job_embedding, cv_embedding)[0][0]) * 100
    
    # Score mots-clés (40% du poids)
    keyword_score = calculate_keyword_match(job_description, cv_text)
    
    # Score hybride pondéré
    final_score = (semantic_score * 0.6) + (keyword_score * 0.4)
    
    return round(final_score, 2)

def get_score_interpretation(score):
    """Interpréter le score de correspondance"""
    if score >= 80:
        return "Excellente correspondance"
    elif score >= 60:
        return "Bonne correspondance"
    elif score >= 40:
        return "Correspondance moyenne"
    else:
        return "Faible correspondance"

@app.route('/')
def index():
    """Page d'accueil avec le formulaire"""
    return render_template_string(HTML_TEMPLATE)

@app.route('/match', methods=['POST'])
def match_cv_to_job():
    """Endpoint POST pour calculer le score de correspondance"""
    try:
        # Récupérer les données du formulaire
        job_description = request.form.get('job_description')
        cv_file = request.files.get('cv_file')
        
        if not job_description or not cv_file:
            return jsonify({
                'error': 'La description de l\'offre et le fichier CV sont requis'
            }), 400
        
        # Sauvegarder le fichier temporairement
        filename = cv_file.filename
        cv_path = os.path.join(CV_FOLDER, filename)
        cv_file.save(cv_path)
        
        # Extraire le texte selon l'extension
        _, ext = os.path.splitext(filename)
        ext = ext.lower()
        
        if ext == '.pdf':
            cv_text = extract_text_from_pdf(cv_path)
        elif ext == '.docx':
            cv_text = extract_text_from_docx(cv_path)
        elif ext == '.txt':
            cv_text = extract_text_from_txt(cv_path)
        else:
            return jsonify({
                'error': 'Format de fichier non supporté. Utilisez .pdf, .docx ou .txt'
            }), 400
        
        if not cv_text.strip():
            return jsonify({
                'error': 'Le CV est vide ou le texte n\'a pas pu être extrait'
            }), 400
        
        # Calculer le score de correspondance
        match_score = calculate_match_score(job_description, cv_text)
        
        return jsonify({
            'cv_filename': filename,
            'match_score': match_score,
            'interpretation': get_score_interpretation(match_score),
            'cv_length': len(cv_text),
            'job_description_length': len(job_description)
        }), 200
        
    except Exception as e:
        return jsonify({
            'error': f'Erreur serveur: {str(e)}'
        }), 500

@app.route('/health', methods=['GET'])
def health_check():
    """Endpoint pour vérifier que l'API fonctionne"""
    return jsonify({
        'status': 'ok',
        'model': 'OrdalieTech/Solon-embeddings-large-0.1',
        'cv_folder': CV_FOLDER
    }), 200

if __name__ == '__main__':
    os.makedirs(CV_FOLDER, exist_ok=True)
    app.run(debug=True, host='0.0.0.0', port=5000)