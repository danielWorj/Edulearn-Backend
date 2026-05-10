from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer, util
from flask_cors import CORS
import PyPDF2
import docx
import os
import re
from typing import Dict, List

app = Flask(__name__)
CORS(app)

# Charger le modèle de transformers
model = SentenceTransformer('OrdalieTech/Solon-embeddings-large-0.1')

# Dossier contenant les CVs
CV_FOLDER = 'cvs'

# ============================================================================
# FONCTIONS D'EXTRACTION DE TEXTE
# ============================================================================

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

# ============================================================================
# SYSTÈME D'EXTRACTION DE CRITÈRES CLÉS
# ============================================================================

def normalize_text(text: str) -> str:
    """
    Normaliser le texte pour faciliter la comparaison
    Unifie les variations d'orthographe et les synonymes
    """
    text = text.lower()
    
    # Dictionnaire de normalisation exhaustif
    replacements = {
        # Variations d'orthographe générales
        'répéter': 'répétition',
        'repeter': 'répétition',
        'repet': 'répétition',
        'makèpe': 'makepe',
        'yaounde': 'yaoundé',
        'bonamoussadi': 'bonamoussadi',
        
        # PHYSIQUE
        'physique-chimie': 'physique',
        'sciences physiques': 'physique',
        'physiques': 'physique',
        'pc': 'physique',
        
        # MATHÉMATIQUES
        'mathématiques': 'mathematiques',
        'mathématique': 'mathematiques',
        'mathematique': 'mathematiques',
        'maths': 'mathematiques',
        'math': 'mathematiques',
        
        # CHIMIE
        'chimie': 'chimie',
        'chemistry': 'chimie',
        
        # SVT / BIOLOGIE
        'svt': 'biologie',
        'sciences de la vie et de la terre': 'biologie',
        'sciences vie terre': 'biologie',
        'biologie': 'biologie',
        'sciences naturelles': 'biologie',
        'sciences nat': 'biologie',
        
        # PHILOSOPHIE
        'philosophie': 'philosophie',
        'philo': 'philosophie',
        'philosophy': 'philosophie',
        
        # LITTÉRATURE / FRANÇAIS
        'littérature': 'litterature',
        'litterature': 'litterature',
        'français': 'francais',
        'francais': 'francais',
        'langue française': 'francais',
        'lettres': 'litterature',
        'lettres modernes': 'litterature',
        'fle': 'francais',
        
        # ANGLAIS
        'anglais': 'anglais',
        'english': 'anglais',
        'langue anglaise': 'anglais',
        
        # HISTOIRE-GÉOGRAPHIE
        'histoire-géographie': 'histoire',
        'histoire-geographie': 'histoire',
        'histoire géographie': 'histoire',
        'histoire geographie': 'histoire',
        'géographie': 'geographie',
        'geographie': 'geographie',
        'geo': 'geographie',
        
        # ÉCONOMIE
        'économie': 'economie',
        'economie': 'economie',
        'sciences économiques': 'economie',
        'sciences economiques': 'economie',
        'eco': 'economie',
        
        # INFORMATIQUE
        'informatique': 'informatique',
        'computer science': 'informatique',
        'programmation': 'informatique',
        'coding': 'informatique',
        'ict': 'informatique',
        'tic': 'informatique',
        
        # LANGUES VIVANTES
        'allemand': 'allemand',
        'espagnol': 'espagnol',
        'italien': 'italien',
        'chinois': 'chinois',
        'arabe': 'arabe',
        
        # ÉDUCATION PHYSIQUE
        'éducation physique': 'eps',
        'education physique': 'eps',
        'eps': 'eps',
        'sport': 'eps',
        
        # ARTS
        'arts plastiques': 'arts',
        'dessin': 'arts',
        'musique': 'musique',
        'éducation musicale': 'musique',
        'education musicale': 'musique',
        
        # COMPTABILITÉ / GESTION
        'comptabilité': 'comptabilite',
        'comptabilite': 'comptabilite',
        'gestion': 'gestion',
        'management': 'gestion',
        
        # DROIT
        'droit': 'droit',
        'sciences juridiques': 'droit',
        
        # SOCIOLOGIE
        'sociologie': 'sociologie',
        'sciences sociales': 'sociologie',
        
        # LATIN / GREC
        'latin': 'latin',
        'grec': 'grec',
        'langues anciennes': 'langues_anciennes',
        
        # TECHNOLOGIE
        'technologie': 'technologie',
        'sciences de l\'ingénieur': 'technologie',
        'sciences ingenieur': 'technologie',
        'si': 'technologie',
    }
    
    # Appliquer les remplacements
    for old, new in replacements.items():
        text = text.replace(old, new)
    
    return text

def extract_key_criteria(text: str) -> Dict:
    """Extraire les critères clés du texte"""
    text_normalized = normalize_text(text)
    
    criteria = {
        'subject': [],        # Matière
        'level': [],          # Niveau (Terminale, 1ère, etc.)
        'location': [],       # Ville/Quartier
        'job_type': [],       # Type (répétition, enseignement, etc.)
        'target_public': []   # Public cible (difficulté, soutien, etc.)
    }
    
    # 1. MATIÈRE - Liste exhaustive des matières scolaires
    subjects = {
        # Sciences
        'physique': ['physique', 'physics'],
        'mathematiques': ['mathematiques', 'maths', 'math'],
        'chimie': ['chimie', 'chemistry'],
        'biologie': ['biologie', 'svt', 'sciences naturelles'],
        'informatique': ['informatique', 'computer science', 'programmation', 'tic'],
        'technologie': ['technologie', 'sciences ingenieur'],
        
        # Lettres et Langues
        'francais': ['francais', 'langue française', 'fle'],
        'litterature': ['litterature', 'lettres'],
        'philosophie': ['philosophie', 'philo'],
        'anglais': ['anglais', 'english'],
        'allemand': ['allemand', 'german'],
        'espagnol': ['espagnol', 'spanish'],
        'italien': ['italien', 'italian'],
        'chinois': ['chinois', 'chinese'],
        'arabe': ['arabe', 'arabic'],
        'latin': ['latin'],
        'grec': ['grec'],
        
        # Sciences Humaines
        'histoire': ['histoire', 'history'],
        'geographie': ['geographie', 'geo', 'geography'],
        'economie': ['economie', 'sciences economiques'],
        'sociologie': ['sociologie', 'sciences sociales'],
        'droit': ['droit', 'sciences juridiques'],
        
        # Gestion et Commerce
        'comptabilite': ['comptabilite', 'accounting'],
        'gestion': ['gestion', 'management'],
        
        # Arts et Sport
        'arts': ['arts', 'dessin', 'arts plastiques'],
        'musique': ['musique', 'education musicale'],
        'eps': ['eps', 'sport', 'education physique'],
    }
    
    for subject, keywords in subjects.items():
        if any(kw in text_normalized for kw in keywords):
            criteria['subject'].append(subject)
    
    # 2. NIVEAU SCOLAIRE
    # Terminale
    if re.search(r'terminale\s*[cdeafg]?', text_normalized) or 'baccalauréat' in text_normalized or re.search(r'\bbac\b', text_normalized):
        criteria['level'].append('terminale')
    # Première
    if re.search(r'premi[èe]re\s*[cdeafg]?', text_normalized) or 'probatoire' in text_normalized:
        criteria['level'].append('première')
    # Seconde
    if 'seconde' in text_normalized or '2nde' in text_normalized:
        criteria['level'].append('seconde')
    # Collège
    if any(word in text_normalized for word in ['collège', 'college', '6ème', '5ème', '4ème', '3ème', 'bepc']):
        criteria['level'].append('collège')
    # Primaire
    if any(word in text_normalized for word in ['primaire', 'primary', 'cp', 'ce1', 'ce2', 'cm1', 'cm2']):
        criteria['level'].append('primaire')
    
    # 3. LOCALISATION - Villes et quartiers du Cameroun
    locations_map = {
        # Douala et quartiers
        'douala': ['douala'],
        'makepe': ['makepe', 'makèpe'],
        'bonapriso': ['bonapriso'],
        'akwa': ['akwa'],
        'bonanjo': ['bonanjo'],
        'bali': ['bali'],
        'bonaberi': ['bonaberi', 'bonabéri'],
        'deido': ['deido', 'deïdo'],
        'new_bell': ['new bell', 'newbell'],
        'pk': ['pk', 'pk8', 'pk10', 'pk12', 'pk14'],
        'bonamoussadi': ['bonamoussadi'],
        
        # Yaoundé et quartiers
        'yaounde': ['yaoundé', 'yaounde'],
        'bastos': ['bastos'],
        'nlongkak': ['nlongkak'],
        'odza': ['odza'],
        'etoudi': ['etoudi', 'étoudi'],
        'essos': ['essos'],
        'mvan': ['mvan'],
        'mokolo': ['mokolo'],
        
        # Autres villes
        'bafoussam': ['bafoussam'],
        'bamenda': ['bamenda'],
        'garoua': ['garoua'],
        'maroua': ['maroua'],
        'ngaoundere': ['ngaoundéré', 'ngaoundere'],
        'limbe': ['limbe'],
        'buea': ['buea'],
        'kribi': ['kribi'],
        'edea': ['edea', 'édéa'],
    }
    
    for location, keywords in locations_map.items():
        if any(kw in text_normalized for kw in keywords):
            criteria['location'].append(location)
    
    # 4. TYPE DE POSTE
    # Répétition/Soutien
    repetition_keywords = ['répétition', 'soutien', 'coach', 'accompagnement', 'domicile', 'particulier', 'privé', 'tuteur', 'répétiteur']
    if any(kw in text_normalized for kw in repetition_keywords):
        criteria['job_type'].append('répétition')
    
    # Enseignement en établissement
    enseignement_keywords = ['enseignant', 'professeur', 'teacher', 'lycée', 'collège', 'établissement', 'école']
    if any(kw in text_normalized for kw in enseignement_keywords):
        criteria['job_type'].append('enseignement')
    
    # 5. PUBLIC CIBLE (élèves en difficulté, préparation examen, etc.)
    target_keywords = {
        'difficulté': ['difficulté', 'difficultés', 'lacune', 'lacunes', 'remise à niveau', 'rattrapage'],
        'examen': ['examen', 'probatoire', 'baccalauréat', 'bepc', 'préparation', 'concours'],
        'soutien': ['soutien', 'aide', 'accompagnement', 'encadrement']
    }
    for target, keywords in target_keywords.items():
        if any(kw in text_normalized for kw in keywords):
            criteria['target_public'].append(target)
    
    return criteria

def calculate_criteria_match(job_criteria: Dict, cv_criteria: Dict) -> Dict:
    """
    Calculer le score de matching sur les critères clés
    AVEC LOGIQUE ÉLIMINATOIRE sur la matière
    """
    
    # Poids de chaque critère (total = 100%)
    weights = {
        'subject': 0.50,        # 50% - La matière est CRITIQUE
        'level': 0.20,          # 20% - Le niveau est important
        'location': 0.10,       # 10% - La proximité compte
        'job_type': 0.15,       # 15% - Le type de poste
        'target_public': 0.05   # 5% - Le public cible
    }
    
    detailed_scores = {}
    total_score = 0
    subject_mismatch = False  # Indicateur de non-correspondance de matière
    
    for criterion, weight in weights.items():
        job_values = set(job_criteria.get(criterion, []))
        cv_values = set(cv_criteria.get(criterion, []))
        
        # Si le critère n'est pas mentionné dans l'offre, on considère que c'est OK
        if not job_values:
            match_score = 50  # Score neutre
        else:
            # Calculer le pourcentage de match
            intersection = job_values.intersection(cv_values)
            if intersection:
                match_score = 100  # Match parfait
            else:
                match_score = 0    # Pas de match
                
                # ⚠️ LOGIQUE ÉLIMINATOIRE : Si la matière ne correspond pas
                if criterion == 'subject':
                    subject_mismatch = True
        
        weighted_score = match_score * weight
        total_score += weighted_score
        
        detailed_scores[criterion] = {
            'match_score': match_score,
            'weighted_score': round(weighted_score, 2),
            'job_values': list(job_values),
            'cv_values': list(cv_values),
            'matched': list(job_values.intersection(cv_values)) if job_values else []
        }
    
    # ⚠️ PÉNALITÉ DRASTIQUE si la matière ne correspond pas
    if subject_mismatch:
        # Réduire le score total drastiquement (division par 3)
        # Un prof de français pour un poste de physique ne peut pas avoir plus de 20-25%
        penalty_factor = 0.35  # On garde seulement 35% du score
        penalized_score = total_score * penalty_factor
        
        return {
            'total': round(penalized_score, 2),
            'details': detailed_scores,
            'subject_mismatch': True,
            'penalty_applied': True,
            'original_score': round(total_score, 2),
            'penalty_factor': penalty_factor
        }
    
    return {
        'total': round(total_score, 2),
        'details': detailed_scores,
        'subject_mismatch': False,
        'penalty_applied': False
    }

# ============================================================================
# CALCUL DE SCORE HYBRIDE
# ============================================================================

def calculate_semantic_score(job_description: str, cv_text: str) -> float:
    """Calculer le score de similarité sémantique"""
    job_embedding = model.encode(job_description, convert_to_tensor=True)
    cv_embedding = model.encode(cv_text, convert_to_tensor=True)
    similarity = util.cos_sim(job_embedding, cv_embedding)
    score = float(similarity[0][0]) * 100
    return round(score, 2)

def calculate_hybrid_score(job_description: str, cv_text: str) -> Dict:
    """
    Score hybride combinant :
    - Similarité sémantique (embeddings)
    - Matching sur critères clés (avec logique éliminatoire)
    """
    
    # 1. Score sémantique
    semantic_score = calculate_semantic_score(job_description, cv_text)
    
    # 2. Extraction des critères
    job_criteria = extract_key_criteria(job_description)
    cv_criteria = extract_key_criteria(cv_text)
    
    # 3. Score sur critères clés (avec pénalité si matière ne correspond pas)
    criteria_result = calculate_criteria_match(job_criteria, cv_criteria)
    criteria_score = criteria_result['total']
    
    # 4. Score final pondéré
    # 30% sémantique + 70% critères
    final_score = (0.30 * semantic_score) + (0.70 * criteria_score)
    
    return {
        'final_score': round(final_score, 2),
        'semantic_score': semantic_score,
        'criteria_score': criteria_score,
        'criteria_details': criteria_result['details'],
        'subject_mismatch': criteria_result.get('subject_mismatch', False),
        'penalty_applied': criteria_result.get('penalty_applied', False),
        'original_criteria_score': criteria_result.get('original_score'),
        'job_criteria': job_criteria,
        'cv_criteria': cv_criteria
    }

def get_score_interpretation(score: float, subject_mismatch: bool = False) -> str:
    """Interpréter le score de correspondance"""
    if subject_mismatch:
        return "❌ Mauvaise correspondance - Matière enseignée différente"
    
    if score >= 85:
        return "✅ Excellente correspondance - Candidat idéal"
    elif score >= 70:
        return "✅ Très bonne correspondance - Candidat fortement recommandé"
    elif score >= 55:
        return "⚠️ Bonne correspondance - Candidat à considérer"
    elif score >= 40:
        return "⚠️ Correspondance moyenne - À évaluer selon contexte"
    else:
        return "❌ Faible correspondance - Profil peu adapté"

# ============================================================================
# ROUTES API
# ============================================================================

@app.route('/match', methods=['POST'])
def match_cv_to_job():
    """
    Endpoint POST pour calculer le score de correspondance avec système hybride
    
    Body JSON attendu:
    {
        "job_description": "Description complète de l'offre d'emploi",
        "cv_filename": "nom_du_cv.pdf"
    }
    """
    try:
        data = request.get_json()
        print(f"Requête reçue: {data}")
        
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
        
        print('Calcul du score hybride...')
        
        # Calculer le score hybride
        result = calculate_hybrid_score(job_description, cv_text)
        
        # Construire la réponse
        response = {
            'cv_filename': cv_filename,
            'final_score': result['final_score'],
            'semantic_score': result['semantic_score'],
            'criteria_score': result['criteria_score'],
            'interpretation': get_score_interpretation(
                result['final_score'], 
                result.get('subject_mismatch', False)
            ),
            'subject_mismatch': result.get('subject_mismatch', False),
            'penalty_applied': result.get('penalty_applied', False),
            'original_criteria_score': result.get('original_criteria_score'),
            'criteria_details': result['criteria_details'],
            'job_criteria': result['job_criteria'],
            'cv_criteria': result['cv_criteria'],
            'metadata': {
                'cv_length': len(cv_text),
                'job_description_length': len(job_description),
                'model_used': 'OrdalieTech/Solon-embeddings-large-0.1',
                'scoring_method': 'hybrid (30% semantic + 70% criteria) with subject penalty'
            }
        }
        
        print(f"Score final calculé: {result['final_score']}%")
        if result.get('subject_mismatch'):
            print("⚠️ ATTENTION: Matière enseignée ne correspond pas - Pénalité appliquée")
        
        return jsonify(response), 200
        
    except Exception as e:
        print(f"Erreur serveur: {str(e)}")
        return jsonify({
            'error': f'Erreur serveur: {str(e)}'
        }), 500

@app.route('/match/simple', methods=['POST'])
def match_cv_to_job_simple():
    """
    Endpoint POST pour calculer UNIQUEMENT le score sémantique (ancien système)
    """
    try:
        data = request.get_json()
        
        job_description = data.get('job_description')
        cv_filename = data.get('cv_filename')
        
        if not job_description or not cv_filename:
            return jsonify({
                'error': 'Les champs "job_description" et "cv_filename" sont requis'
            }), 400
        
        cv_text = get_cv_text(cv_filename)
        
        if cv_text is None:
            return jsonify({
                'error': f'CV "{cv_filename}" introuvable'
            }), 404
        
        # Score sémantique uniquement
        semantic_score = calculate_semantic_score(job_description, cv_text)
        
        return jsonify({
            'cv_filename': cv_filename,
            'semantic_score': semantic_score,
            'interpretation': get_score_interpretation(semantic_score),
            'method': 'semantic_only'
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
        'cv_folder': CV_FOLDER,
        'scoring_methods': ['hybrid_with_penalty', 'semantic_only'],
        'supported_formats': ['.pdf', '.docx', '.txt'],
        'supported_subjects': [
            'physique', 'mathematiques', 'chimie', 'biologie', 'informatique',
            'francais', 'litterature', 'philosophie', 'anglais', 'allemand', 
            'espagnol', 'histoire', 'geographie', 'economie', 'comptabilite',
            'gestion', 'arts', 'musique', 'eps'
        ],
        'features': [
            'Subject mismatch penalty (65% reduction)',
            'Eliminatory logic for wrong subject'
        ]
    }), 200

@app.route('/analyze', methods=['POST'])
def analyze_text():
    """
    Endpoint pour analyser les critères extraits d'un texte
    """
    try:
        data = request.get_json()
        text = data.get('text')
        
        if not text:
            return jsonify({
                'error': 'Le champ "text" est requis'
            }), 400
        
        criteria = extract_key_criteria(text)
        
        return jsonify({
            'text_length': len(text),
            'normalized_text': normalize_text(text)[:200] + '...',
            'criteria': criteria
        }), 200
        
    except Exception as e:
        return jsonify({
            'error': f'Erreur serveur: {str(e)}'
        }), 500

# ============================================================================
# LANCEMENT DE L'APPLICATION
# ============================================================================

if __name__ == '__main__':
    # Créer le dossier CVs s'il n'existe pas
    os.makedirs(CV_FOLDER, exist_ok=True)
    
    print("="*60)
    print("API de Matching CV - Système Hybride avec Pénalité")
    print("="*60)
    print(f"Modèle: OrdalieTech/Solon-embeddings-large-0.1")
    print(f"Dossier CVs: {CV_FOLDER}")
    print(f"Méthode: Hybrid (30% sémantique + 70% critères)")
    print(f"✨ Nouvelle feature: Pénalité de 65% si mauvaise matière")
    print("="*60)
    print("Matières supportées:")
    print("  Sciences: Physique, Maths, Chimie, SVT, Informatique")
    print("  Langues: Français, Anglais, Allemand, Espagnol, etc.")
    print("  Humaines: Histoire, Géo, Philo, Économie, Sociologie")
    print("  Autres: Comptabilité, Gestion, Arts, Musique, EPS")
    print("="*60)
    
    # Lancer l'application
    app.run(debug=True, host='0.0.0.0', port=5000)