from flask import Flask, request, jsonify, render_template_string
from sentence_transformers import CrossEncoder
from flask_cors import CORS
import PyPDF2
import docx
import os
import re

app = Flask(__name__)
CORS(app) 

# CROSS-ENCODER pour meilleure précision
# Modèles disponibles :
# - 'cross-encoder/ms-marco-MiniLM-L-12-v2' (multilingue, rapide)
# - 'cross-encoder/mmarco-mMiniLMv2-L12-H384-v1' (multilingue optimisé)
model = CrossEncoder('cross-encoder/mmarco-mMiniLMv2-L12-H384-v1')

CV_FOLDER = 'cvs'

# Template HTML (identique au précédent)
HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CV Matcher - Cross-Encoder</title>
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
        
        .badge {
            display: inline-block;
            background: rgba(255,255,255,0.2);
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 0.9em;
            margin-top: 10px;
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
            <h1>🎯 CV Matcher PRO</h1>
            <p>Analyse avancée avec Cross-Encoder</p>
            <span class="badge">⚡ Précision optimale</span>
        </div>
        
        <div class="content">
            <form id="matchForm" enctype="multipart/form-data">
                <div class="form-group">
                    <label for="jobDescription">📋 Description de l'offre d'emploi</label>
                    <textarea 
                        id="jobDescription" 
                        name="job_description" 
                        rows="8" 
                        placeholder="Collez ici la description complète de l'offre d'emploi..."
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
                    Analyse en cours avec Cross-Encoder...
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
        
        fileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                fileName.textContent = `📄 ${e.target.files[0].name}`;
                fileName.style.display = 'block';
            }
        });
        
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
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
                    displayResults(data);
                } else {
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
            
            scoreCircle.textContent = score + '%';
            
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
        print(f"Erreur PDF: {e}")
    return text

def extract_text_from_docx(docx_path):
    """Extraire le texte d'un fichier DOCX"""
    text = ""
    try:
        doc = docx.Document(docx_path)
        for para in doc.paragraphs:
            text += para.text + "\n"
    except Exception as e:
        print(f"Erreur DOCX: {e}")
    return text

def extract_text_from_txt(txt_path):
    """Extraire le texte d'un fichier TXT"""
    try:
        with open(txt_path, 'r', encoding='utf-8') as file:
            return file.read()
    except Exception as e:
        print(f"Erreur TXT: {e}")
        return ""

def preprocess_text(text):
    """Nettoyer le texte"""
    text = re.sub(r'[•▪▫◦●○]', '', text)
    text = re.sub(r'\n\s*\n', '\n', text)
    text = ' '.join(text.split())
    return text

def calculate_match_score(job_description, cv_text):
    """
    Calculer le score avec Cross-Encoder
    Le cross-encoder prend les DEUX textes ensemble
    """
    # Prétraiter
    job_clean = preprocess_text(job_description)
    cv_clean = preprocess_text(cv_text)
    
    # CROSS-ENCODER : encode les deux textes ensemble
    # Retourne un score de similarité directement
    score = model.predict([(job_clean, cv_clean)])[0]
    
    # Normaliser le score (cross-encoder retourne souvent des valeurs négatives)
    # On utilise une fonction sigmoïde pour ramener entre 0 et 100
    import math
    normalized_score = 100 / (1 + math.exp(-score))
    
    return round(normalized_score, 2)

def get_score_interpretation(score):
    """Interpréter le score"""
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
    """Page d'accueil"""
    return render_template_string(HTML_TEMPLATE)

@app.route('/match', methods=['POST'])
def match_cv_to_job():
    """Endpoint de matching"""
    try:
        job_description = request.form.get('job_description')
        cv_file = request.files.get('cv_file')
        
        if not job_description or not cv_file:
            return jsonify({
                'error': 'Description et CV requis'
            }), 400
        
        # Sauvegarder le fichier
        filename = cv_file.filename
        cv_path = os.path.join(CV_FOLDER, filename)
        cv_file.save(cv_path)
        
        # Extraire le texte
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
                'error': 'Format non supporté'
            }), 400
        
        if not cv_text.strip():
            return jsonify({
                'error': 'CV vide ou illisible'
            }), 400
        
        # Calculer le score avec CROSS-ENCODER
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
            'error': f'Erreur: {str(e)}'
        }), 500

@app.route('/health', methods=['GET'])
def health_check():
    """Health check"""
    return jsonify({
        'status': 'ok',
        'model': 'cross-encoder/mmarco-mMiniLMv2-L12-H384-v1',
        'type': 'Cross-Encoder (Haute Précision)'
    }), 200

if __name__ == '__main__':
    os.makedirs(CV_FOLDER, exist_ok=True)
    app.run(debug=True, host='0.0.0.0', port=5000)