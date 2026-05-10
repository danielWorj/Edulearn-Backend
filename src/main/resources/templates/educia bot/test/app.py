from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer, util
from flask_cors import CORS
import PyPDF2
import docx
import os

app = Flask(__name__)
CORS(app) 
# Charger le modèle de transformers (multilangue pour supporter le français)
#model = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
#model = SentenceTransformer('dangvantuan/sentence-camembert-large')
model = SentenceTransformer('OrdalieTech/Solon-embeddings-large-0.1')

# Dossier contenant les CVs
CV_FOLDER = 'cvs'

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

def get_cv_text(cv_filename):
    """Récupérer le texte du CV selon son extension"""
    cv_path = os.path.join(CV_FOLDER, cv_filename)
    
    if not os.path.exists(cv_path):
        return None
    
    _, ext = os.path.splitext(cv_filename)
    ext = ext.lower()
    
    if ext == '.pdf':
        return extract_text_from_pdf(cv_path)
    elif ext == '.docx':
        return extract_text_from_docx(cv_path)
    elif ext == '.txt':
        return extract_text_from_txt(cv_path)
    else:
        return None

def calculate_match_score(job_description, cv_text):
    """Calculer le score de correspondance entre l'offre et le CV"""
    # Générer les embeddings
    job_embedding = model.encode(job_description, convert_to_tensor=True)
    cv_embedding = model.encode(cv_text, convert_to_tensor=True)
    
    # Calculer la similarité cosinus
    similarity = util.cos_sim(job_embedding, cv_embedding)
    
    # Convertir en score de 0 à 100
    score = float(similarity[0][0]) * 100
    
    return round(score, 2)

@app.route('/match', methods=['POST'])
def match_cv_to_job():
    """
    Endpoint POST pour calculer le score de correspondance
    
    Body JSON attendu:
    {
        "job_description": "Description complète de l'offre d'emploi",
        "cv_filename": "nom_du_cv.pdf"
    }
    """
    try:
        data = request.get_json()
        print(data)
        # Validation des données
        if not data:
            return jsonify({
                'error': 'Aucune donnée JSON fournie'
            }), 400
        
        job_description = data.get('job_description')
        cv_filename = data.get('cv_filename')
        
        if not job_description or not cv_filename:
            return jsonify({
                'error': 'Les champs "job_description" et "cv_filename" sont requis'
            }), 400
        
        # Récupérer le texte du CV
        cv_text = get_cv_text(cv_filename)
        
        if cv_text is None:
            return jsonify({
                'error': f'CV "{cv_filename}" introuvable ou format non supporté. Formats acceptés: .pdf, .docx, .txt'
            }), 404
        
        if not cv_text.strip():
            return jsonify({
                'error': 'Le CV est vide ou le texte n\'a pas pu être extrait'
            }), 400
        print('calcul du score'); 
        # Calculer le score de correspondance
        match_score = calculate_match_score(job_description, cv_text)
        
        return jsonify({
            'cv_filename': cv_filename,
            'match_score': match_score,
            'interpretation': get_score_interpretation(match_score),
            'cv_length': len(cv_text),
            'job_description_length': len(job_description)
        }), 200
        
    except Exception as e:
        return jsonify({
            'error': f'Erreur serveur: {str(e)}'
        }), 500

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

@app.route('/health', methods=['GET'])
def health_check():
    """Endpoint pour vérifier que l'API fonctionne"""
    return jsonify({
        'status': 'ok',
        'model': 'paraphrase-multilingual-MiniLM-L12-v2',
        'cv_folder': CV_FOLDER
    }), 200

if __name__ == '__main__':
    # Créer le dossier CVs s'il n'existe pas
    os.makedirs(CV_FOLDER, exist_ok=True)
    
    # Lancer l'application
    app.run(debug=True)