from flask import Flask, request, jsonify, render_template_string
from sentence_transformers import SentenceTransformer, util
import PyPDF2
import docx
import re
import io
from werkzeug.utils import secure_filename
import numpy as np
from typing import List, Dict, Optional
from dataclasses import dataclass
from datetime import datetime

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # Max 16MB

# Chargement du modèle de transformers (une seule fois au démarrage)
print("Chargement du modèle Sentence-BERT...")
model = SentenceTransformer('OrdalieTech/Solon-embeddings-large-0.1')
print("Modèle chargé avec succès!")


@dataclass
class Mission:
    """Représente une mission de l'offre"""
    id: int
    description: str


@dataclass
class Competence:
    """Représente une compétence requise"""
    id: int
    nom: str
    niveau: Optional[str] = None


@dataclass
class Technologie:
    """Représente un outil/technologie"""
    id: int
    nom: str
    categorie: Optional[str] = None


class Offre:
    """
    Classe représentant une offre d'emploi complète
    Basée sur CompleteOffreConstruct
    """
    
    def __init__(
        self,
        id: int,
        intitule: str,
        description: str,
        entreprise: str,
        localisation: str,
        salaire_min: float,
        salaire_max: float,
        date: str,
        type_contrat: str,
        secteur: str,
        is_active: bool = True,
        missions: List[Mission] = None,
        competences: List[Competence] = None,
        tools: List[Technologie] = None
    ):
        self.id = id
        self.intitule = intitule
        self.description = description
        self.entreprise = entreprise
        self.localisation = localisation
        self.salaire_min = salaire_min
        self.salaire_max = salaire_max
        self.date = date
        self.type_contrat = type_contrat
        self.secteur = secteur
        self.is_active = is_active
        self.missions = missions or []
        self.competences = competences or []
        self.tools = tools or []
    
    @classmethod
    def from_dict(cls, data: Dict):
        """Crée une instance d'Offre à partir d'un dictionnaire"""
        missions = [
            Mission(id=m.get('id'), description=m.get('description'))
            for m in data.get('mission', [])
        ]
        
        competences = [
            Competence(
                id=c.get('id'),
                nom=c.get('nom'),
                niveau=c.get('niveau')
            )
            for c in data.get('competences', [])
        ]
        
        tools = [
            Technologie(
                id=t.get('id'),
                nom=t.get('nom'),
                categorie=t.get('categorie')
            )
            for t in data.get('tools', [])
        ]
        
        return cls(
            id=data.get('id'),
            intitule=data.get('intitule'),
            description=data.get('description'),
            entreprise=data.get('entreprise'),
            localisation=data.get('localisation'),
            salaire_min=data.get('salaireMin', 0),
            salaire_max=data.get('salaireMax', 0),
            date=data.get('date'),
            type_contrat=data.get('typeContrat'),
            secteur=data.get('secteur'),
            is_active=data.get('isActive', True),
            missions=missions,
            competences=competences,
            tools=tools
        )
    
    def generer_texte_missions(self) -> str:
        """Génère un texte consolidé des missions"""
        if not self.missions:
            return ""
        return " ".join([m.description for m in self.missions])
    
    def generer_texte_competences(self) -> str:
        """Génère un texte consolidé des compétences"""
        if not self.competences:
            return ""
        textes = []
        for c in self.competences:
            texte = c.nom
            if c.niveau:
                texte += f" (niveau {c.niveau})"
            textes.append(texte)
        return ", ".join(textes)
    
    def generer_texte_technologies(self) -> str:
        """Génère un texte consolidé des technologies"""
        if not self.tools:
            return ""
        textes = []
        for t in self.tools:
            texte = t.nom
            if t.categorie:
                texte += f" ({t.categorie})"
            textes.append(texte)
        return ", ".join(textes)
    
    def generer_texte_complet_competences(self) -> str:
        """Génère le texte complet pour matching des compétences"""
        parts = []
        
        # Compétences techniques
        if self.competences:
            parts.append("Compétences requises: " + self.generer_texte_competences())
        
        # Technologies/Outils
        if self.tools:
            parts.append("Technologies: " + self.generer_texte_technologies())
        
        return "\n".join(parts)
    
    def generer_texte_experience(self) -> str:
        """Génère le texte pour matching de l'expérience"""
        parts = []
        
        # Description générale
        if self.description:
            parts.append(self.description)
        
        # Missions
        if self.missions:
            parts.append("Missions: " + self.generer_texte_missions())
        
        # Type de contrat et secteur
        parts.append(f"Type de contrat: {self.type_contrat}")
        parts.append(f"Secteur: {self.secteur}")
        
        return "\n".join(parts)
    
    def generer_texte_formation(self) -> str:
        """Génère le texte pour matching de la formation"""
        # Dans la structure actuelle, pas de champ formation spécifique
        # On peut extraire depuis la description ou laisser vide
        # pour être enrichi ultérieurement
        parts = []
        
        # Tentative d'extraction de mentions de formation dans la description
        description_lower = self.description.lower()
        mots_cles_formation = [
            'diplôme', 'master', 'licence', 'bac', 'formation',
            'ingénieur', 'doctorat', 'certification'
        ]
        
        lignes_formation = []
        for ligne in self.description.split('\n'):
            if any(mc in ligne.lower() for mc in mots_cles_formation):
                lignes_formation.append(ligne.strip())
        
        if lignes_formation:
            parts.append("Formation recherchée: " + " ".join(lignes_formation))
        
        return "\n".join(parts)
    
    def to_dict(self) -> Dict:
        """Convertit l'offre en dictionnaire"""
        return {
            'id': self.id,
            'intitule': self.intitule,
            'description': self.description,
            'entreprise': self.entreprise,
            'localisation': self.localisation,
            'salaireMin': self.salaire_min,
            'salaireMax': self.salaire_max,
            'date': self.date,
            'typeContrat': self.type_contrat,
            'secteur': self.secteur,
            'isActive': self.is_active,
            'missions': [{'id': m.id, 'description': m.description} for m in self.missions],
            'competences': [
                {'id': c.id, 'nom': c.nom, 'niveau': c.niveau}
                for c in self.competences
            ],
            'tools': [
                {'id': t.id, 'nom': t.nom, 'categorie': t.categorie}
                for t in self.tools
            ]
        }


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
                'savoir-faire', 'skills', 'aptitudes', 'technologies'
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
    
    def calculer_matching_cv_offre_objet(self, sections_cv: Dict, offre: Offre):
        """
        Calcule le matching entre CV et offre (utilisant l'objet Offre)
        
        Args:
            sections_cv: dict avec 'experience', 'competence', 'diplome'
            offre: instance de la classe Offre
        
        Returns:
            dict avec scores détaillés
        """
        # Génération des textes d'offre
        texte_competences_offre = offre.generer_texte_complet_competences()
        texte_experience_offre = offre.generer_texte_experience()
        texte_formation_offre = offre.generer_texte_formation()
        
        # Calcul des scores individuels
        score_competence = self.calculer_similarite(
            sections_cv['competence'], 
            texte_competences_offre
        )
        
        score_experience = self.calculer_similarite(
            sections_cv['experience'], 
            texte_experience_offre
        )
        
        score_diplome = self.calculer_similarite(
            sections_cv['diplome'], 
            texte_formation_offre if texte_formation_offre else texte_experience_offre
        )
        
        # Formule de scoring final
        score_final = (
            score_competence * 0.5 + 
            score_experience * 0.3 + 
            score_diplome * 0.2
        )
        
        # Interprétation
        if score_final >= 80:
            interpretation = "🌟 Excellent match ! Candidat très qualifié"
        elif score_final >= 65:
            interpretation = "✅ Bon match - Candidat qualifié"
        elif score_final >= 50:
            interpretation = "⚠️ Match moyen - À examiner"
        else:
            interpretation = "❌ Match faible - Profil peu adapté"
        
        return {
            'score_final': round(score_final, 2),
            'interpretation': interpretation,
            'scores_details': {
                'competence': score_competence,
                'experience': score_experience,
                'diplome': score_diplome
            },
            'ponderations': {
                'competence': 50,
                'experience': 30,
                'diplome': 20
            },
            'textes_offre_utilises': {
                'competences': texte_competences_offre,
                'experience': texte_experience_offre,
                'formation': texte_formation_offre
            },
            'offre_info': {
                'intitule': offre.intitule,
                'entreprise': offre.entreprise,
                'localisation': offre.localisation,
                'type_contrat': offre.type_contrat
            }
        }
    
    def calculer_matching_cv_offre_dict(self, sections_cv: Dict, offre_dict: Dict):
        """
        Version legacy utilisant des dictionnaires simples
        """
        score_competence = self.calculer_similarite(
            sections_cv['competence'], 
            offre_dict.get('competence', '')
        )
        
        score_experience = self.calculer_similarite(
            sections_cv['experience'], 
            offre_dict.get('experience', '')
        )
        
        score_diplome = self.calculer_similarite(
            sections_cv['diplome'], 
            offre_dict.get('diplome', '')
        )

        
        score_final = (
            score_competence * 0.5 + 
            score_experience * 0.3 + 
            score_diplome * 0.2
        )
        
        if score_final >= 80:
            interpretation = "🌟 Excellent match ! Candidat très qualifié"
        elif score_final >= 65:
            interpretation = "✅ Bon match - Candidat qualifié"
        elif score_final >= 50:
            interpretation = "⚠️ Match moyen - À examiner"
        else:
            interpretation = "❌ Match faible - Profil peu adapté"
        
        return {
            'score_final': round(score_final, 2),
            'interpretation': interpretation,
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
    
    Accepte deux modes:
    1. Form-data (mode legacy):
        - cv_file: Fichier CV (PDF, DOCX, TXT)
        - offre_competence: Texte des compétences requises
        - offre_experience: Texte de l'expérience requise
        - offre_diplome: Texte du diplôme requis
    
    2. JSON + File (mode objet Offre):
        - cv_file: Fichier CV (PDF, DOCX, TXT)
        - offre_json: JSON de l'offre complète (CompleteOffreConstruct)
    """
    try:
        # Vérification du fichier CV
        if 'cv_file' not in request.files:
            return jsonify({'error': 'Fichier CV manquant'}), 400
        
        cv_file = request.files['cv_file']
        if cv_file.filename == '':
            return jsonify({'error': 'Aucun fichier sélectionné'}), 400
        
        # Extraction du CV
        texte_cv = CVParser.extraire_texte(cv_file)
        sections_cv = CVParser.parser_cv(texte_cv)
        
        # Détection du mode
        if 'offre_json' in request.form:
            # Mode objet Offre
            import json
            try:
                offre_data = json.loads(request.form['offre_json'])
                offre = Offre.from_dict(offre_data)
                
                # Calcul du matching
                resultats = matching_service.calculer_matching_cv_offre_objet(
                    sections_cv, 
                    offre
                )
                
            except json.JSONDecodeError:
                return jsonify({'error': 'JSON offre invalide'}), 400
            except Exception as e:
                return jsonify({'error': f'Erreur parsing offre: {str(e)}'}), 400
        
        else:
            # Mode legacy (form-data simple)
            offre_dict = {
                'competence': request.form.get('offre_competence', ''),
                'experience': request.form.get('offre_experience', ''),
                'diplome': request.form.get('offre_diplome', '')
            }
            
            # Calcul du matching
            resultats = matching_service.calculer_matching_cv_offre_dict(
                sections_cv,
                offre_dict
            )
        
        # Réponse
        return jsonify({
            'success': True,
            'data': {
                **resultats,
                'sections_cv_extraites': sections_cv
            }
        })
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/api/matching/offre-complete', methods=['POST'])
def matching_offre_complete():
    """
    API dédiée au matching avec objet Offre complet
    
    JSON Body:
    {
        "cv_file": "base64_encoded_file" OU utiliser multipart/form-data,
        "offre": { CompleteOffreConstruct }
    }
    """
    try:
        # Gestion du fichier CV
        if 'cv_file' in request.files:
            cv_file = request.files['cv_file']
        else:
            return jsonify({'error': 'Fichier CV manquant'}), 400
        
        if cv_file.filename == '':
            return jsonify({'error': 'Aucun fichier sélectionné'}), 400
        
        # Extraction du CV
        texte_cv = CVParser.extraire_texte(cv_file)
        sections_cv = CVParser.parser_cv(texte_cv)
        
        # Récupération de l'offre
        if request.content_type.startswith('multipart/form-data'):
            import json
            offre_data = json.loads(request.form.get('offre', '{}'))
        else:
            offre_data = request.json.get('offre', {})
        
        if not offre_data:
            return jsonify({'error': 'Données offre manquantes'}), 400
        
        # Création de l'objet Offre
        offre = Offre.from_dict(offre_data)
        
        # Calcul du matching
        resultats = matching_service.calculer_matching_cv_offre_objet(
            sections_cv,
            offre
        )
        
        return jsonify({
            'success': True,
            'data': {
                **resultats,
                'sections_cv_extraites': sections_cv
            }
        })
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


# Template HTML (identique à l'original)
HTML_TEMPLATE = '''
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🎯 Matching CV / Offre avec Transformers</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 900px;
            margin: 0 auto;
        }
        
        .header {
            text-align: center;
            color: white;
            margin-bottom: 30px;
        }
        
        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
        }
        
        .header p {
            font-size: 1.1em;
            opacity: 0.95;
        }
        
        .content {
            background: white;
            border-radius: 20px;
            padding: 40px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
        }
        
        .section {
            margin-bottom: 30px;
        }
        
        .section h2 {
            color: #667eea;
            margin-bottom: 20px;
            font-size: 1.5em;
            border-bottom: 2px solid #667eea;
            padding-bottom: 10px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 600;
        }
        
        .form-group input[type="file"] {
            width: 100%;
            padding: 12px;
            border: 2px dashed #667eea;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .form-group input[type="file"]:hover {
            border-color: #764ba2;
            background: #f8f9ff;
        }
        
        .form-group textarea {
            width: 100%;
            padding: 12px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 14px;
            font-family: inherit;
            resize: vertical;
            transition: border-color 0.3s;
        }
        
        .form-group textarea:focus {
            outline: none;
            border-color: #667eea;
        }
        
        button[type="submit"] {
            width: 100%;
            padding: 15px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        
        button[type="submit"]:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(102, 126, 234, 0.4);
        }
        
        button[type="submit"]:active {
            transform: translateY(0);
        }
        
        button[type="submit"]:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
        
        .loader {
            display: none;
            text-align: center;
            padding: 40px;
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
            margin: 0 auto 20px;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        
        .error {
            display: none;
            background: #fee;
            color: #c33;
            padding: 15px;
            border-radius: 8px;
            margin: 20px 0;
            border-left: 4px solid #c33;
        }
        
        .error.show {
            display: block;
        }
        
        .results {
            display: none;
            margin-top: 30px;
        }
        
        .results.show {
            display: block;
        }
        
        .score-final {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 15px;
            text-align: center;
            margin-bottom: 30px;
            box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
        }
        
        .score-number {
            font-size: 4em;
            font-weight: bold;
            margin-bottom: 10px;
        }
        
        .interpretation {
            font-size: 1.3em;
            opacity: 0.95;
        }
        
        .scores-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .score-card {
            background: #f8f9ff;
            padding: 20px;
            border-radius: 12px;
            text-align: center;
            border: 2px solid #e0e7ff;
        }
        
        .score-card h3 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 1.1em;
        }
        
        .score-card .score {
            font-size: 2.5em;
            font-weight: bold;
            color: #333;
            margin-bottom: 10px;
        }
        
        .score-card .weight {
            color: #666;
            font-size: 0.9em;
        }
        
        .sections-extraites {
            background: #f8f9ff;
            padding: 25px;
            border-radius: 12px;
            border: 2px solid #e0e7ff;
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
    print("📍 API Legacy: POST http://localhost:5000/api/matching")
    print("📍 API Offre Complète: POST http://localhost:5000/api/matching/offre-complete")
    print("📍 Test: http://localhost:5000")
    print("="*60 + "\n")
    
    app.run(debug=True, host='0.0.0.0', port=5000)
