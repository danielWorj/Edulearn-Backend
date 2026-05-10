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

# Chargement du modèle de transformers
print("Chargement du modèle Sentence-BERT optimisé pour le français...")
model = SentenceTransformer('OrdalieTech/Solon-embeddings-large-0.1')
print("Modèle chargé avec succès!")


class CVEnseignantParser:
    """Extraction des sections du CV enseignant"""
    
    
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
            return CVEnseignantParser.extraire_texte_pdf(file)
        elif filename.endswith('.docx'):
            return CVEnseignantParser.extraire_texte_docx(file)
        elif filename.endswith('.txt'):
            return file.read().decode('utf-8')
        else:
            raise Exception("Format non supporté. Utilisez PDF, DOCX ou TXT")
    
    @staticmethod
    def extraire_section(texte, mots_cles):
        """Extrait une section basée sur des mots-clés """
        lignes = texte.split('\n')
        
        # Recherche du début de section (plus flexible)
        debut_idx = -1
        for i, ligne in enumerate(lignes):
            ligne_lower = ligne.lower().strip()
            # Ignore les lignes trop courtes ou vides
            if len(ligne_lower) < 3:
                continue
            
            # Cherche les mots-clés
            for mc in mots_cles:
                if mc in ligne_lower:
                    debut_idx = i
                    break
            if debut_idx != -1:
                break
        
        if debut_idx == -1:
            # Si pas trouvé, retourner tout le texte (fallback)
            return texte
        
        # Sections de fin possibles (plus exhaustif)
        sections_fin = [
            'expérience', 'experience', 'formation', 'diplôme', 'diplome',
            'éducation', 'education', 'compétence', 'competence',
            'langues', 'centres d\'intérêt', 'loisirs', 'références',
            'profil', 'objectif', 'contact', 'coordonnées'
        ]
        
        # Cherche la fin de section
        fin_idx = len(lignes)
        section_actuelle = mots_cles[0].split()[0]  # Premier mot de la section actuelle
        
        for i in range(debut_idx + 1, len(lignes)):
            ligne_lower = lignes[i].lower().strip()
            
            # Ligne vide ou trop courte : continue
            if len(ligne_lower) < 3:
                continue
            
            # Détecte une nouvelle section (titre en majuscules ou mots-clés)
            if any(sf in ligne_lower for sf in sections_fin):
                # Vérifie que ce n'est pas la même section
                if section_actuelle not in ligne_lower:
                    fin_idx = i
                    break
        
        contenu = '\n'.join(lignes[debut_idx:fin_idx])
        return contenu.strip()
    

    
    @staticmethod
    def parser_cv_enseignant(texte_cv):
        """Parse le CV enseignant - VERSION AMÉLIORÉE avec fallback"""
        
        # Normalisation du texte
        texte_cv = texte_cv.replace('\r\n', '\n').replace('\r', '\n')
        
        # Tentative d'extraction des sections
        experience = CVEnseignantParser.extraire_section(texte_cv, [
            'expérience professionnelle', 'experience professionnelle',
            'expériences professionnelles', 'experiences professionnelles',
            'expérience', 'experience', 'parcours professionnel',
            'expérience d\'enseignement', 'experience d\'enseignement',
            'expérience pédagogique', 'experience pedagogique',
            'carrière', 'carriere'
        ])
        
        diplome = CVEnseignantParser.extraire_section(texte_cv, [
            'formation', 'formations', 'diplômes', 'diplomes', 'diplôme', 'diplome',
            'éducation', 'education', 'parcours académique', 'études', 'etudes',
            'certifications', 'qualifications', 'formation académique'
        ])
        
        # FALLBACK : Si sections vides, utiliser tout le CV
        if not experience.strip() or len(experience.strip()) < 50:
            print("⚠️ Section EXPÉRIENCE non détectée, utilisation du CV complet")
            experience = texte_cv
        
        if not diplome.strip() or len(diplome.strip()) < 30:
            print("⚠️ Section DIPLÔME non détectée, utilisation du CV complet")
            diplome = texte_cv
        
        # Affichage pour debug
        print(f"\n📊 Sections extraites:")
        print(f"   - Expérience: {len(experience)} caractères")
        print(f"   - Diplôme: {len(diplome)} caractères")
        
        return {
            'experience': experience,
            'diplome': diplome
        }


class MatchingEnseignantService:
    """Service de matching Offre de répétition / Enseignant"""
    
    def __init__(self, model):
        self.model = model
    
    def calculer_similarite(self, texte1, texte2):
        """
        Calcule la similarité cosinus entre deux textes 
        Retourne un score entre 0 et 100
        """
        # la fonction strip() permet de nettoyer la chaine de caracteres
        if not texte1.strip() or not texte2.strip():
            return 0.0
        
        # Prétraitement : nettoyer les textes
        texte1 = ' '.join(texte1.split())  # Normalise les espaces
        texte2 = ' '.join(texte2.split())
        
        # Génère les embeddings
        embedding1 = self.model.encode(texte1, convert_to_tensor=True)
        embedding2 = self.model.encode(texte2, convert_to_tensor=True)
        
        # Calcule la similarité cosinus
        similarite = util.cos_sim(embedding1, embedding2).item()
        
        # Convertit en score sur 100 avec boost pour compenser
        # Le modèle donne souvent des scores entre 0.2-0.6 pour du bon matching

        # Cette ligne borne le score de 0 a 100 car en multipliant la similarite * 150 
        # ca peut depasser 100
        score = max(0, min(100, (similarite * 150)))  # Boost de 50%
        
        # retour du score avec 2 chiffres apres la virgule. 
        return round(score, 2)
    
    def calculer_matching_enseignant(self, cv_enseignant, offre_repetition):
        """
        Calcule le matching CV enseignant et offre 
        """
        # Construction du texte de l'offre
        matiere = offre_repetition.get('matiere', '').lower()
        niveau = offre_repetition.get('niveau', '').lower()
        
        offre_texte = f"""
        Matière: {offre_repetition.get('matiere', '')}
        Niveau: {offre_repetition.get('niveau', '')}
        Description: {offre_repetition.get('description', '')}
        Besoins spécifiques: {offre_repetition.get('besoins', '')}
        """
        
        # Calcul des scores de base

        # calcul de la similarite entre l'experience de l'enseignant et l'offre de repetition 
        score_experience = self.calculer_similarite(
            cv_enseignant['experience'], 
            offre_texte
        )
        
        # calcul de la similarite entre l'experience de l'enseignant et l'offre de repetition 
        score_diplome = self.calculer_similarite(
            cv_enseignant['diplome'], 
            offre_texte
        )
        
        # Détection directe de mots-clés dans le CV
        cv_complet = (cv_enseignant['experience'] + ' ' + cv_enseignant['diplome']).lower()
        
        bonus = 0
        # Le systeme du bonus c'est d'ajouter un nombre de points quand on detecte les points reecherche par le parent 
        # dans le cv de l'enseignant. 


        # Bonus si la matière exacte est mentionnée
        if matiere and matiere in cv_complet:
            bonus += 15
            print(f"✅ BONUS +15: Matière '{matiere}' trouvée dans le CV")
        
        # Bonus pour années d'expérience
        import re
        annees_match = re.findall(r'(\d+)\s*an[s]?', cv_complet)
        if annees_match:
            max_annees = max([int(a) for a in annees_match])
            if max_annees >= 5:
                bonus += 10
                print(f"✅ BONUS +10: {max_annees} ans d'expérience détectés")
            elif max_annees >= 2:
                bonus += 5
                print(f"✅ BONUS +5: {max_annees} ans d'expérience détectés")
        
        # Bonus pour niveau d'étude (Master, Licence, etc.)
        if 'master' in cv_complet or 'm2' in cv_complet or 'm1' in cv_complet:
            bonus += 10
            print("✅ BONUS +10: Master détecté")
        elif 'licence' in cv_complet or 'l3' in cv_complet:
            bonus += 5
            print("✅ BONUS +5: Licence détecté")
        
        # Bonus pour mots-clés pédagogiques
        mots_pedagogiques = ['enseignant', 'professeur', 'répétiteur', 'pédagogie', 'cours']
        if any(mot in cv_complet for mot in mots_pedagogiques):
            bonus += 5
            print("✅ BONUS +5: Profil enseignant confirmé")
        
        print(f"\n📈 Scores bruts: Expérience={score_experience}, Diplôme={score_diplome}, Bonus={bonus}")
        
        # Application du bonus
        score_experience = min(100, score_experience + bonus * 0.6)
        score_diplome = min(100, score_diplome + bonus * 0.4)
        
        # Formule finale : 60% expérience + 40% diplôme
        score_final = (
            score_experience * 0.6 + 
            score_diplome * 0.4
        )
        
        print(f"📊 Scores finaux: Expérience={score_experience}, Diplôme={score_diplome}")
        print(f"🎯 SCORE FINAL: {score_final}/100\n")
        
        interpretation = self._interpreter_score(
            score_final, 
            offre_repetition.get('niveau', '')
        )
        
        return {
            'score_final': round(score_final, 2),
            'scores_details': {
                'experience': round(score_experience, 2),
                'diplome': round(score_diplome, 2),
                'bonus': bonus
            },
            'ponderations': {
                'experience': 60,
                'diplome': 40
            },
            'interpretation': interpretation,
            'recommandation': self._generer_recommandation(score_final)
        }
    
    def _interpreter_score(self, score, niveau):
        """Interprète le score selon le contexte"""
        if score >= 85:
            return f"EXCELLENT MATCH"
        elif score >= 70:
            return f"TRES BON MATCH"
        elif score >= 55:
            return f"BON MATCH"
        elif score >= 40:
            return "MATCH MOYEN"
        else:
            return "FAIBLE CORRESPONDANCE"
    
    def _generer_recommandation(self, score):
        """Génère une recommandation d'action"""
        if score >= 70:
            return "Contactez cet enseignant rapidement"
        elif score >= 55:
            return "Enseignant qualifié, vérifiez sa disponibilité"
        elif score >= 40:
            return "Consultez son profil complet avant décision"
        else:
            return "Recherchez d'autres candidats plus qualifiés"


# Initialisation du service
matching_service = MatchingEnseignantService(model)


@app.route('/')
def index():
    """Page HTML de test"""
    return render_template_string(HTML_TEMPLATE)


@app.route('/api/matching/enseignant', methods=['POST'])
def matching_enseignant():
    """
    API POST pour calculer le matching Offre/Enseignant
    
    Form-data:
        - cv_enseignant: Fichier CV de l'enseignant (PDF, DOCX, TXT)
        - matiere: Matière demandée (ex: Mathématiques, Physique)
        - niveau: Niveau de l'élève (ex: Terminale S, 3ème)
        - description: Description détaillée de l'offre
        - besoins: Besoins spécifiques (optionnel)
    """
    try:
        # Vérification du fichier CV
        if 'cv_enseignant' not in request.files:
            return jsonify({'error': 'CV de l\'enseignant manquant'}), 400
        
        cv_file = request.files['cv_enseignant']
        if cv_file.filename == '':
            return jsonify({'error': 'Aucun fichier sélectionné'}), 400
        
        # Extraction du texte du CV
        texte_cv = CVEnseignantParser.extraire_texte(cv_file)
        
        # Parsing des sections du CV
        sections_cv = CVEnseignantParser.parser_cv_enseignant(texte_cv)
        
        # Récupération de l'offre de répétition
        offre = {
            'matiere': request.form.get('matiere', '').strip(),
            'niveau': request.form.get('niveau', '').strip(),
            'description': request.form.get('description', '').strip(),
            'besoins': request.form.get('besoins', '').strip()
        }
        
        # Validation de l'offre
        if not offre['matiere'] or not offre['niveau']:
            return jsonify({
                'error': 'Matière et niveau sont obligatoires'
            }), 400
        
        # Calcul du matching
        resultats = matching_service.calculer_matching_enseignant(
            sections_cv, 
            offre
        )
        
        # Ajout des sections extraites
        resultats['sections_cv_extraites'] = {
            'experience': sections_cv['experience'][:300] + '...' if len(sections_cv['experience']) > 300 else sections_cv['experience'] or "Section non trouvée dans le CV",
            'diplome': sections_cv['diplome'][:300] + '...' if len(sections_cv['diplome']) > 300 else sections_cv['diplome'] or "Section non trouvée dans le CV"
        }
        
        # Informations de l'offre
        resultats['offre_details'] = {
            'matiere': offre['matiere'],
            'niveau': offre['niveau'],
            'description': offre['description'],
            'besoins': offre['besoins']
        }
        
        return jsonify({
            'success': True,
            'data': resultats
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


# @app.route('/api/matching/batch', methods=['POST'])
# def matching_batch():
#     """
#     API pour matcher une offre avec plusieurs enseignants
    
#     JSON Body:
#     {
#         "offre": {
#             "matiere": "Mathématiques",
#             "niveau": "Terminale S",
#             "description": "...",
#             "besoins": "..."
#         },
#         "enseignants": [
#             {"id": 1, "experience": "...", "diplome": "..."},
#             {"id": 2, "experience": "...", "diplome": "..."}
#         ]
#     }
#     """
#     try:
#         data = request.get_json()
        
#         if not data or 'offre' not in data or 'enseignants' not in data:
#             return jsonify({'error': 'Format JSON invalide'}), 400
        
#         offre = data['offre']
#         enseignants = data['enseignants']
        
#         resultats = []
        
#         for enseignant in enseignants:
#             cv_data = {
#                 'experience': enseignant.get('experience', ''),
#                 'diplome': enseignant.get('diplome', '')
#             }
            
#             matching = matching_service.calculer_matching_enseignant(
#                 cv_data, 
#                 offre
#             )
            
#             resultats.append({
#                 'enseignant_id': enseignant.get('id'),
#                 'nom': enseignant.get('nom', 'N/A'),
#                 'score_final': matching['score_final'],
#                 'scores_details': matching['scores_details'],
#                 'interpretation': matching['interpretation'],
#                 'recommandation': matching['recommandation']
#             })
        
#         # Tri par score décroissant
#         resultats.sort(key=lambda x: x['score_final'], reverse=True)
        
#         return jsonify({
#             'success': True,
#             'offre': offre,
#             'resultats': resultats,
#             'meilleur_candidat': resultats[0] if resultats else None
#         }), 200
        
#     except Exception as e:
#         return jsonify({
#             'success': False,
#             'error': str(e)
#         }), 500
@app.route('/api/matching/batch', methods=['POST'])
def matching_batch():
    """
    API pour matcher une offre avec plusieurs enseignants via leurs CVs
    
    Form-data:
        - matiere: Matière demandée (ex: "Mathématiques")
        - niveau: Niveau de l'élève (ex: "Terminale")
        - description: Description de l'offre
        - besoins: Besoins spécifiques (optionnel)
        - cv_files: Liste de fichiers CV (multiple files avec même clé)
        - noms_enseignants: Liste des noms (séparés par virgule ou JSON array)
    
    Returns:
        JSON avec liste des résultats triés par score
    """
    try:
        # Récupération de l'offre
        matiere = request.form.get('matiere', '').strip()
        niveau = request.form.get('niveau', '').strip()
        description = request.form.get('description', '').strip()
        besoins = request.form.get('besoins', '').strip()
        
        # Validation
        if not matiere or not niveau:
            return jsonify({
                'success': False,
                'error': 'Matière et niveau sont obligatoires'
            }), 400
        
        # Récupération des fichiers CV
        cv_files = request.files.getlist('cv_files')
        
        if not cv_files or len(cv_files) == 0:
            return jsonify({
                'success': False,
                'error': 'Aucun CV fourni'
            }), 400
        
        # Récupération des noms des enseignants
        noms_raw = request.form.get('noms_enseignants', '')
        
        # Parse les noms (format: "Nom1,Nom2,Nom3" ou JSON array)
        try:
            if noms_raw.startswith('['):
                import json
                noms_enseignants = json.loads(noms_raw)
            else:
                noms_enseignants = [nom.strip() for nom in noms_raw.split(',')]
        except:
            # Si pas de noms fournis, utilise les noms de fichiers
            noms_enseignants = [f.filename.rsplit('.', 1)[0] for f in cv_files]
        
        # Vérification de cohérence
        if len(noms_enseignants) != len(cv_files):
            # Complète avec des noms par défaut si manquants
            while len(noms_enseignants) < len(cv_files):
                noms_enseignants.append(f"Enseignant_{len(noms_enseignants) + 1}")
        
        # Construction de l'offre
        offre = {
            'matiere': matiere,
            'niveau': niveau,
            'description': description,
            'besoins': besoins
        }
        
        print(f"\n{'='*60}")
        print(f"🎯 MATCHING BATCH: {len(cv_files)} enseignants pour {matiere} - {niveau}")
        print(f"{'='*60}\n")
        
        resultats = []
        
        # Traite chaque CV
        for idx, cv_file in enumerate(cv_files):
            nom_enseignant = noms_enseignants[idx]
            
            try:
                print(f"📄 Traitement: {nom_enseignant} ({cv_file.filename})")
                
                # Extraction du texte
                texte_cv = CVEnseignantParser.extraire_texte(cv_file)
                
                # Parsing des sections
                sections_cv = CVEnseignantParser.parser_cv_enseignant(texte_cv)
                
                # Calcul du matching
                matching = matching_service.calculer_matching_enseignant(
                    sections_cv, 
                    offre
                )
                
                # Ajout au résultat
                resultats.append({
                    'matiere': matiere,
                    'nom_enseignant': nom_enseignant,
                    'score': matching['score_final'],
                    'scores_details': {
                        'experience': matching['scores_details']['experience'],
                        'diplome': matching['scores_details']['diplome'],
                        'bonus': matching['scores_details'].get('bonus', 0)
                    },
                    'interpretation': matching['interpretation'],
                    'recommandation': matching['recommandation']
                })
                
                print(f"   ✅ Score: {matching['score_final']}/100")
                
            except Exception as e:
                print(f"   ❌ Erreur pour {nom_enseignant}: {str(e)}")
                
                # Ajoute quand même avec score 0
                resultats.append({
                    'matiere': matiere,
                    'nom_enseignant': nom_enseignant,
                    'score': 0,
                    'scores_details': {
                        'experience': 0,
                        'diplome': 0,
                        'bonus': 0
                    },
                    'interpretation': 'Erreur de traitement du CV',
                    'recommandation': 'Vérifiez le format du CV',
                    'error': str(e)
                })
        
        # Tri par score décroissant
        resultats.sort(key=lambda x: x['score'], reverse=True)
        
        print(f"\n{'='*60}")
        print(f"✅ MATCHING TERMINÉ - Meilleur: {resultats[0]['nom_enseignant']} ({resultats[0]['score']}/100)")
        print(f"{'='*60}\n")
        
        return jsonify({
            'success': True,
            'offre': {
                'matiere': matiere,
                'niveau': niveau,
                'description': description,
                'besoins': besoins
            },
            'nombre_enseignants': len(resultats),
            'resultats': resultats,
            'meilleur_candidat': resultats[0] if resultats else None
        }), 200
        
    except Exception as e:
        print(f"\n❌ ERREUR BATCH: {str(e)}\n")
        return jsonify({
            'success': False,
            'error': f'Erreur lors du traitement batch: {str(e)}'
        }), 500

@app.route('/api/health', methods=['GET'])
def health_check():
    """Endpoint de santé"""
    return jsonify({
        'status': 'healthy',
        'service': 'matching-enseignant',
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
    <title>Matching Offre de Répétition - Enseignant</title>
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
            max-width: 1000px;
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
        
        .header .emoji {
            font-size: 3em;
            margin-bottom: 10px;
        }
        
        .header p {
            opacity: 0.95;
            font-size: 1.15em;
            line-height: 1.5;
        }
        
        .content {
            padding: 40px;
        }
        
        .section {
            margin-bottom: 35px;
        }
        
        .section h2 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 1.6em;
            border-bottom: 3px solid #667eea;
            padding-bottom: 10px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        label {
            display: block;
            font-weight: 600;
            margin-bottom: 8px;
            color: #333;
            font-size: 1.05em;
        }
        
        label .required {
            color: #e74c3c;
        }
        
        input[type="file"] {
            width: 100%;
            padding: 15px;
            border: 2px dashed #667eea;
            border-radius: 10px;
            background: #f8f9ff;
            cursor: pointer;
            transition: all 0.3s;
            font-size: 1em;
        }
        
        input[type="file"]:hover {
            border-color: #764ba2;
            background: #f0f1ff;
            transform: translateY(-2px);
        }
        
        input[type="text"],
        select,
        textarea {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-family: inherit;
            font-size: 1em;
            resize: vertical;
            transition: all 0.3s;
        }
        
        input[type="text"]:focus,
        select:focus,
        textarea:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
        
        select {
            cursor: pointer;
            background: white;
        }
        
        button {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 18px 40px;
            font-size: 1.15em;
            font-weight: 600;
            border-radius: 10px;
            cursor: pointer;
            width: 100%;
            transition: all 0.3s;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        button:hover {
            transform: translateY(-3px);
            box-shadow: 0 15px 30px rgba(102, 126, 234, 0.4);
        }
        
        button:active {
            transform: translateY(-1px);
        }
        
        button:disabled {
            background: #ccc;
            cursor: not-allowed;
            transform: none;
        }
        
        .loader {
            display: none;
            text-align: center;
            margin: 30px 0;
        }
        
        .loader.active {
            display: block;
        }
        
        .spinner {
            border: 5px solid #f3f3f3;
            border-top: 5px solid #667eea;
            border-radius: 50%;
            width: 60px;
            height: 60px;
            animation: spin 1s linear infinite;
            margin: 0 auto 15px;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        
        .results {
            display: none;
            margin-top: 40px;
            padding: 30px;
            background: #f8f9ff;
            border-radius: 15px;
            border: 3px solid #667eea;
        }
        
        .results.show {
            display: block;
            animation: slideIn 0.6s ease;
        }
        
        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .score-final {
            text-align: center;
            padding: 40px;
            background: white;
            border-radius: 15px;
            margin-bottom: 25px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.1);
        }
        
        .score-number {
            font-size: 5em;
            font-weight: bold;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 15px;
        }
        
        .interpretation {
            font-size: 1.3em;
            color: #555;
            margin-bottom: 10px;
            font-weight: 500;
        }
        
        .recommandation {
            font-size: 1.1em;
            color: #667eea;
            font-weight: 600;
            padding: 12px;
            background: #f0f1ff;
            border-radius: 8px;
            margin-top: 15px;
        }
        
        .scores-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 20px;
            margin: 25px 0;
        }
        
        .score-card {
            background: white;
            padding: 25px;
            border-radius: 12px;
            text-align: center;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            transition: transform 0.3s;
        }
        
        .score-card:hover {
            transform: translateY(-5px);
        }
        
        .score-card h3 {
            color: #667eea;
            font-size: 1.1em;
            margin-bottom: 15px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .score-card .score {
            font-size: 2.5em;
            font-weight: bold;
            color: #333;
            margin-bottom: 8px;
        }
        
        .score-card .weight {
            font-size: 1em;
            color: #999;
            font-weight: 500;
        }
        
        .error {
            background: #fee;
            border-left: 5px solid #e74c3c;
            padding: 20px;
            border-radius: 10px;
            color: #c0392b;
            display: none;
            margin: 20px 0;
            font-weight: 500;
        }
        
        .error.show {
            display: block;
        }
        
        .offre-resume {
            background: white;
            padding: 20px;
            border-radius: 12px;
            margin-bottom: 25px;
            box-shadow: 0 3px 10px rgba(0,0,0,0.1);
        }
        
        .offre-resume h3 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 1.3em;
        }
        
        .offre-item {
            margin-bottom: 10px;
            padding: 8px 0;
            border-bottom: 1px solid #eee;
        }
        
        .offre-item:last-child {
            border-bottom: none;
        }
        
        .offre-item strong {
            color: #555;
            display: inline-block;
            min-width: 100px;
        }
        
        .sections-extraites {
            margin-top: 25px;
        }
        
        .section-preview {
            background: white;
            padding: 20px;
            border-radius: 12px;
            margin-bottom: 15px;
            box-shadow: 0 3px 10px rgba(0,0,0,0.1);
        }
        
        .section-preview h4 {
            color: #667eea;
            margin-bottom: 12px;
            font-size: 1.2em;
        }
        
        .section-preview p {
            color: #666;
            font-size: 0.95em;
            line-height: 1.7;
        }
        
        @media (max-width: 768px) {
            .scores-grid {
                grid-template-columns: 1fr;
            }
            
            .header h1 {
                font-size: 1.8em;
            }
            
            .content {
                padding: 25px;
            }
            
            .score-number {
                font-size: 3.5em;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="emoji">👨‍🏫📚</div>
            <h1>Matching Enseignant</h1>
            <p>Trouvez l'enseignant parfait pour vos cours de répétition<br>
            Algorithme intelligent basé sur l'expérience et les diplômes</p>
        </div>
        
        <div class="content">
            <form id="matchingForm" enctype="multipart/form-data">
                <div class="section">
                    <h2>👨‍🏫 CV de l'Enseignant</h2>
                    <div class="form-group">
                        <label for="cv_enseignant">
                            Télécharger le CV <span class="required">*</span>
                        </label>
                        <input 
                            type="file" 
                            id="cv_enseignant" 
                            name="cv_enseignant" 
                            accept=".pdf,.docx,.txt" 
                            required
                        >
                    </div>
                </div>
                
                <div class="section">
                    <h2>📝 Offre de Répétition du Parent</h2>
                    
                    <div class="form-group">
                        <label for="matiere">
                            Matière demandée <span class="required">*</span>
                        </label>
                        <select id="matiere" name="matiere" required>
                            <option value="">-- Sélectionnez une matière --</option>
                            <optgroup label="Sciences">
                                <option value="Mathématiques">Mathématiques</option>
                                <option value="Physique">Physique</option>
                                <option value="Chimie">Chimie</option>
                                <option value="SVT">SVT (Sciences de la Vie)</option>
                                <option value="Informatique">Informatique</option>
                            </optgroup>
                            <optgroup label="Lettres">
                                <option value="Français">Français</option>
                                <option value="Philosophie">Philosophie</option>
                                <option value="Histoire-Géographie">Histoire-Géographie</option>
                                <option value="Anglais">Anglais</option>
                                <option value="Espagnol">Espagnol</option>
                                <option value="Allemand">Allemand</option>
                            </optgroup>
                            <optgroup label="Autres">
                                <option value="Économie">Économie</option>
                                <option value="Comptabilité">Comptabilité</option>
                                <option value="Droit">Droit</option>
                            </optgroup>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label for="niveau">
                            Niveau de l'élève <span class="required">*</span>
                        </label>
                        <select id="niveau" name="niveau" required>
                            <option value="">-- Sélectionnez un niveau --</option>
                            <optgroup label="Collège">
                                <option value="6ème">6ème</option>
                                <option value="5ème">5ème</option>
                                <option value="4ème">4ème</option>
                                <option value="3ème">3ème</option>
                            </optgroup>
                            <optgroup label="Lycée">
                                <option value="Seconde">Seconde</option>
                                <option value="Première">Première</option>
                                <option value="Terminale">Terminale</option>
                            </optgroup>
                            <optgroup label="Supérieur">
                                <option value="Licence 1">Licence 1</option>
                                <option value="Licence 2">Licence 2</option>
                                <option value="Licence 3">Licence 3</option>
                                <option value="Master">Master</option>
                            </optgroup>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label for="description">
                            Description de l'offre <span class="required">*</span>
                        </label>
                        <textarea 
                            id="description" 
                            name="description" 
                            rows="5" 
                            placeholder="Ex: Mon enfant a besoin d'aide en mathématiques pour préparer le bac. Il a des difficultés en analyse et en probabilités..."
                            required
                        ></textarea>
                    </div>
                    
                    <div class="form-group">
                        <label for="besoins">
                            Besoins spécifiques (optionnel)
                        </label>
                        <textarea 
                            id="besoins" 
                            name="besoins" 
                            rows="3" 
                            placeholder="Ex: Préparation examen, remise à niveau, approfondissement, méthodologie..."
                        ></textarea>
                    </div>
                </div>
                
                <button type="submit" id="submitBtn">
                    🔍 Analyser la Correspondance
                </button>
                </form>
            
            <div class="loader" id="loader">
                <div class="spinner"></div>
                <p>Analyse en cours... Veuillez patienter</p>
            </div>
            
            <div class="error" id="errorDiv"></div>
            
            <div class="results" id="results">
                <h2 style="color: #667eea; margin-bottom: 25px; font-size: 2em; text-align: center;">
                    📊 Résultats du Matching
                </h2>
                
                <div class="offre-resume" id="offreResume"></div>
                
                <div class="score-final">
                    <div class="score-number" id="scoreFinal">--</div>
                    <div style="font-size: 1.8em; color: #999; margin-bottom: 20px;">/ 100</div>
                    <div class="interpretation" id="interpretation"></div>
                    <div class="recommandation" id="recommandation"></div>
                </div>
                
                <div class="scores-grid">
                    <div class="score-card">
                        <h3>📚 Expérience</h3>
                        <div class="score" id="scoreExperience">--</div>
                        <div class="weight">Pondération: 60%</div>
                    </div>
                    
                    <div class="score-card">
                        <h3>🎓 Diplôme</h3>
                        <div class="score" id="scoreDiplome">--</div>
                        <div class="weight">Pondération: 40%</div>
                    </div>
                </div>
                
                <div class="sections-extraites">
                    <h3 style="color: #667eea; margin-bottom: 20px; font-size: 1.5em;">
                        📄 Sections extraites du CV
                    </h3>
                    
                    <div class="section-preview">
                        <h4>📚 Expérience Professionnelle</h4>
                        <p id="sectionExperience">--</p>
                    </div>
                    
                    <div class="section-preview">
                        <h4>🎓 Formation & Diplômes</h4>
                        <p id="sectionDiplome">--</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        const form = document.getElementById('matchingForm');
        const loader = document.getElementById('loader');
        const results = document.getElementById('results');
        const errorDiv = document.getElementById('errorDiv');
        const submitBtn = document.getElementById('submitBtn');
        
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            // Réinitialisation
            loader.classList.add('active');
            results.classList.remove('show');
            errorDiv.classList.remove('show');
            submitBtn.disabled = true;
            
            try {
                const formData = new FormData(form);
                
                const response = await fetch('/api/matching/enseignant', {
                    method: 'POST',
                    body: formData
                });
                
                const data = await response.json();
                
                if (data.success) {
                    displayResults(data.data);
                } else {
                    showError(data.error || 'Une erreur est survenue');
                }
                
            } catch (error) {
                showError('Erreur de connexion au serveur: ' + error.message);
            } finally {
                loader.classList.remove('active');
                submitBtn.disabled = false;
            }
        });
        
        function displayResults(data) {
            // Affichage du résumé de l'offre
            const offreHtml = `
                <h3>🎯 Détails de l'Offre</h3>
                <div class="offre-item">
                    <strong>Matière:</strong> ${data.offre_details.matiere}
                </div>
                <div class="offre-item">
                    <strong>Niveau:</strong> ${data.offre_details.niveau}
                </div>
                <div class="offre-item">
                    <strong>Description:</strong> ${data.offre_details.description}
                </div>
                ${data.offre_details.besoins ? `
                <div class="offre-item">
                    <strong>Besoins spécifiques:</strong> ${data.offre_details.besoins}
                </div>
                ` : ''}
            `;
            document.getElementById('offreResume').innerHTML = offreHtml;
            
            // Score final
            document.getElementById('scoreFinal').textContent = data.score_final;
            document.getElementById('interpretation').textContent = data.interpretation;
            document.getElementById('recommandation').textContent = '💡 ' + data.recommandation;
            
            // Scores détaillés
            document.getElementById('scoreExperience').textContent = data.scores_details.experience;
            document.getElementById('scoreDiplome').textContent = data.scores_details.diplome;
            
            // Sections extraites
            document.getElementById('sectionExperience').textContent = 
                data.sections_cv_extraites.experience || 'Aucune section trouvée';
            document.getElementById('sectionDiplome').textContent = 
                data.sections_cv_extraites.diplome || 'Aucune section trouvée';
            
            // Couleur du score selon le résultat
            const scoreElement = document.getElementById('scoreFinal');
            const score = data.score_final;
            
            if (score >= 85) {
                scoreElement.style.background = 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)';
            } else if (score >= 70) {
                scoreElement.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
            } else if (score >= 55) {
                scoreElement.style.background = 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)';
            } else {
                scoreElement.style.background = 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)';
            }
            scoreElement.style.webkitBackgroundClip = 'text';
            scoreElement.style.webkitTextFillColor = 'transparent';
            scoreElement.style.backgroundClip = 'text';
            
            results.classList.add('show');
            
            // Scroll vers les résultats
            results.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
        
        function showError(message) {
            errorDiv.textContent = '❌ ' + message;
            errorDiv.classList.add('show');
            errorDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        
        // Preview du nom de fichier
        document.getElementById('cv_enseignant').addEventListener('change', function(e) {
            if (e.target.files.length > 0) {
                const fileName = e.target.files[0].name;
                console.log('Fichier sélectionné:', fileName);
            }
        });
    </script>
</body>
</html>
'''


if __name__ == '__main__':
    print("=" * 60)
    print("🚀 API de Matching Enseignant - Plateforme de Répétition")
    print("=" * 60)
    print("📌 URL: http://localhost:5000")
    print("📌 Endpoints disponibles:")
    print("   - GET  /           : Interface de test HTML")
    print("   - POST /api/matching/enseignant : Matching 1 enseignant")
    print("   - POST /api/matching/batch      : Matching multiple enseignants")
    print("   - GET  /api/health              : Health check")
    print("=" * 60)
    print("🔧 Modèle: OrdalieTech/Solon-embeddings-large-0.1")
    print("📊 Formule: Score = Experience(60%) + Diplome(40%)")
    print("=" * 60)
    
    app.run(debug=True, host='0.0.0.0', port=5000)