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


# ─────────────────────────────────────────────────────────────────────────────
# PARSING DU CV  (uniquement spécialité, expériences, diplôme)
# La bio de l'enseignant est reçue comme champ de formulaire,
# elle n'est donc PAS extraite depuis le CV.
# ─────────────────────────────────────────────────────────────────────────────
class CVParser:
    """Extraction des sections pertinentes du CV de l'enseignant."""

    @staticmethod
    def extraire_texte_pdf(file):
        """Extrait le texte brut d'un fichier PDF."""
        try:
            pdf_reader = PyPDF2.PdfReader(file)
            texte = ""
            for page in pdf_reader.pages:
                texte += page.extract_text() + "\n"
            return texte
        except Exception as e:
            raise Exception(f"Erreur lecture PDF : {str(e)}")

    @staticmethod
    def extraire_texte_docx(file):
        """Extrait le texte brut d'un fichier DOCX."""
        try:
            doc = docx.Document(file)
            texte = "\n".join([p.text for p in doc.paragraphs])
            return texte
        except Exception as e:
            raise Exception(f"Erreur lecture DOCX : {str(e)}")

    @staticmethod
    def extraire_texte(file):
        """Détecte le format et extrait le texte brut."""
        filename = secure_filename(file.filename).lower()
        if filename.endswith('.pdf'):
            return CVParser.extraire_texte_pdf(file)
        elif filename.endswith('.docx'):
            return CVParser.extraire_texte_docx(file)
        elif filename.endswith('.txt'):
            return file.read().decode('utf-8')
        else:
            raise Exception("Format non supporté. Utilisez PDF, DOCX ou TXT.")

    @staticmethod
    def extraire_section(texte, mots_cles):
        """Extrait une section du CV identifiée par ses mots-clés d'en-tête."""
        lignes = texte.split('\n')

        debut_idx = -1
        for i, ligne in enumerate(lignes):
            if any(mc in ligne.lower() for mc in mots_cles):
                debut_idx = i
                break

        if debut_idx == -1:
            return ""

        # Marqueurs de sections connues qui indiquent la fin de la section courante
        sections_fin = [
            'expérience', 'experience', 'compétence', 'competence',
            'formation', 'diplôme', 'diplome', 'éducation', 'education',
            'langues', 'centres d\'intérêt', 'loisirs', 'références',
            'méthodes', 'présentation', 'spécialité', 'specialite',
        ]

        fin_idx = len(lignes)
        for i in range(debut_idx + 1, len(lignes)):
            ligne_lower = lignes[i].lower().strip()
            if any(sf in ligne_lower for sf in sections_fin) and len(ligne_lower) < 50:
                fin_idx = i
                break

        return '\n'.join(lignes[debut_idx:fin_idx]).strip()

    @staticmethod
    def parser_cv_enseignant(texte_cv):
        """
        Parse le CV de l'enseignant.
        Extrait uniquement :
          - specialite  : spécialité / matière enseignée
          - experience  : expériences professionnelles détaillées
          - diplome     : diplômes / formation académique

        NOTE : La bio / méthodes pédagogiques est intentionnellement
               exclue de l'extraction CV — elle est reçue comme champ
               séparé dans la requête (bio_enseignant).
        """
        return {
            'specialite': CVParser.extraire_section(texte_cv, [
                'spécialité', 'specialite', 'matière', 'matiere',
                'domaine', 'discipline', 'enseignement',
            ]),
            'experience': CVParser.extraire_section(texte_cv, [
                'expérience professionnelle', 'experience professionnelle',
                'expériences', 'experiences', 'parcours professionnel',
                'expérience', 'experience',
            ]),
            'diplome': CVParser.extraire_section(texte_cv, [
                'formation', 'diplômes', 'diplomes', 'diplôme', 'diplome',
                'éducation', 'education', 'parcours académique',
                'études', 'etudes',
            ]),
        }


# ─────────────────────────────────────────────────────────────────────────────
# SERVICE DE MATCHING PROFIL  (mP uniquement — pas de localisation)
# ─────────────────────────────────────────────────────────────────────────────
class MatchingService:
    """
    Calcule le Matching de Profil (mP) selon les critères Educia.

    Critères et pondérations :
      Spécialité                  30%   (similarité spécialité CV ↔ matière offre)
      Années d'expérience         10%   (valeur numérique normalisée)
      Pertinence des expériences  20%   (similarité expériences CV ↔ contexte offre)
      Bio / méthodes pédagogiques 20%   (similarité bio_enseignant ↔ contexte offre)
      Diplôme principal           20%   (similarité diplôme CV ↔ matière offre)

    Score final retourné : mP  (0–10)
    La formule mF = 0.8 × mP + 0.2 × mL est hors périmètre de cette API.
    """

    POIDS = {
        'specialite':             0.30,
        'annees_experience':      0.10,
        'pertinence_experience':  0.20,
        'bio':                    0.20,
        'diplome':                0.20,
    }

    def __init__(self, model):
        self.model = model

    # ── Similarité sémantique ─────────────────────────────────────────────────
    def calculer_similarite(self, texte1, texte2):
        """
        Cosinus entre les embeddings de deux textes.
        Retourne un score entre 0 et 10.
        """
        if not texte1 or not texte1.strip() or not texte2 or not texte2.strip():
            return 0.0
        emb1 = self.model.encode(texte1, convert_to_tensor=True)
        emb2 = self.model.encode(texte2, convert_to_tensor=True)
        sim  = util.cos_sim(emb1, emb2).item()
        return round(max(0.0, min(10.0, sim * 10)), 2)

    # ── Barème années d'expérience ────────────────────────────────────────────
    def normaliser_annees_experience(self, annees):
        """
        Barème :
          0–1 an   → 2/10
          2–3 ans  → 4/10
          4–6 ans  → 6/10
          7–10 ans → 8/10
          > 10 ans → 10/10
        """
        try:
            annees = float(annees)
        except (ValueError, TypeError):
            return 0.0

        if annees <= 1:   return 2.0
        if annees <= 3:   return 4.0
        if annees <= 6:   return 6.0
        if annees <= 10:  return 8.0
        return 10.0

    # ── Calcul mP ─────────────────────────────────────────────────────────────
    def calculer_matching_profil(
        self,
        sections_cv,        # dict : specialite, experience, diplome  (extraits du CV)
        bio_enseignant,     # str  : bio / méthodes pédagogiques (champ de requête)
        offre,              # dict : matiere, niveau, bio (description de l'offre)
        annees_experience,  # int/float
    ):
        """
        Calcule le score de matching de profil (mP).

        Args:
            sections_cv       : {'specialite', 'experience', 'diplome'}
            bio_enseignant    : bio de l'enseignant reçue dans la requête
            offre             : {'matiere', 'niveau', 'bio'}
                                  - matiere : matière ciblée (depuis MatiereOffre)
                                  - niveau  : niveau de l'élève
                                  - bio     : description/besoins (depuis OffreRepetition.bio)
            annees_experience : années d'expérience déclarées

        Returns:
            dict avec mP, scores_details et ponderations_pct
        """
        matiere = offre.get('matiere', '')
        niveau  = offre.get('niveau',  '')
        bio_offre = offre.get('bio',   '')

        # Contexte global de l'offre pour les comparaisons sémantiques multi-critères
        contexte_offre = (
            f"Matière : {matiere}. "
            f"Niveau : {niveau}. "
            f"Description de l'offre : {bio_offre}."
        )

        # 1. Spécialité (30%)
        #    Similarité entre la spécialité déclarée dans le CV
        #    et la matière + niveau de l'offre
        score_specialite = self.calculer_similarite(
            sections_cv.get('specialite') or sections_cv.get('experience', ''),
            f"{matiere} {niveau}".strip()
        )

        # 2. Années d'expérience (10%)
        score_annees = self.normaliser_annees_experience(annees_experience)

        # 3. Pertinence des expériences (20%)
        #    Similarité entre les expériences détaillées du CV
        #    et le contexte complet de l'offre
        score_experience = self.calculer_similarite(
            sections_cv.get('experience', ''),
            contexte_offre
        )

        # 4. Bio / méthodes pédagogiques (20%)
        #    Similarité entre la bio reçue en paramètre (pas extraite du CV)
        #    et le contexte complet de l'offre
        score_bio = self.calculer_similarite(
            bio_enseignant,
            contexte_offre
        )

        # 5. Diplôme principal (20%)
        #    Similarité entre la section diplôme du CV
        #    et la matière + niveau de l'offre
        score_diplome = self.calculer_similarite(
            sections_cv.get('diplome', ''),
            f"{matiere} {niveau}".strip()
        )

        # Score mP pondéré (sur 10)
        mP = round(
            score_specialite   * self.POIDS['specialite']            +
            score_annees       * self.POIDS['annees_experience']     +
            score_experience   * self.POIDS['pertinence_experience'] +
            score_bio          * self.POIDS['bio']                   +
            score_diplome      * self.POIDS['diplome'],
            2
        )

        return {
            'mP': mP,
            'scores_details': {
                'specialite':            score_specialite,
                'annees_experience':     score_annees,
                'pertinence_experience': score_experience,
                'bio':                   score_bio,
                'diplome':               score_diplome,
            },
            'ponderations_pct': {
                'specialite':            30,
                'annees_experience':     10,
                'pertinence_experience': 20,
                'bio':                   20,
                'diplome':               20,
            },
        }


# Initialisation du service de matching
matching_service = MatchingService(model)


# ─────────────────────────────────────────────────────────────────────────────
# ENDPOINTS
# ─────────────────────────────────────────────────────────────────────────────

@app.route('/')
def index():
    return render_template_string(HTML_TEMPLATE)


@app.route('/api/health', methods=['GET'])
def health_check():
    return jsonify({
        'status':     'healthy',
        'plateforme': 'Educia',
        'model':      'OrdalieTech/Solon-embeddings-large-0.1',
        'version':    '3.0',
        'scope':      'matching_profil_uniquement',
    })


# ── /api/matching/enseignant  (matching individuel) ──────────────────────────
@app.route('/api/matching/enseignant', methods=['POST'])
def matching_enseignant():
    """
    Calcule le matching de profil (mP) entre une OffreRepetition et un Enseignant.

    Form-data attendu :
      Fichier :
        cv_enseignant        : CV de l'enseignant (PDF, DOCX ou TXT)

      Données enseignant :
        bio_enseignant       : Bio / méthodes pédagogiques de l'enseignant
        annees_experience    : Années d'expérience déclarées (int, défaut 0)
        statut_verifie       : "true" | "false"
        section_enseignant   : "francophone" | "anglophone"

      Données offre (OffreRepetition / MatiereOffre) :
        matiere              : Intitulé de la matière (MatiereOffre.matiere.intitule)
        niveau               : Niveau de l'élève (OffreRepetition.eleve.niveau.intitule)
        description          : Bio / description de l'offre (OffreRepetition.bio)
        section_demandee     : Section linguistique souhaitée (optionnel)

    Réponse JSON :
      {
        "success": true,
        "data": {
          "score_final": <mP sur 10>,
          "scores_details": { ... },
          "ponderations_pct": { ... },
          "interpretation": "...",
          "sections_cv_extraites": { ... }
        }
      }
    """
    try:
        # ── Filtre 1 : statut vérifié ─────────────────────────────────────────
        statut_verifie = request.form.get('statut_verifie', 'false').lower() == 'true'
        if not statut_verifie:
            return jsonify({
                'success': False,
                'error':   'Enseignant non vérifié : exclu du matching.',
            }), 400

        # ── Filtre 2 : compatibilité de section ──────────────────────────────
        section_enseignant = request.form.get('section_enseignant', '').lower().strip()
        section_demandee   = request.form.get('section_demandee',   '').lower().strip()
        if section_enseignant and section_demandee and section_enseignant != section_demandee:
            return jsonify({
                'success': False,
                'error':   (f"Incompatibilité de section : enseignant '{section_enseignant}' "
                            f"/ demande '{section_demandee}'."),
            }), 400

        # ── CV de l'enseignant ────────────────────────────────────────────────
        if 'cv_enseignant' not in request.files:
            return jsonify({'success': False, 'error': 'Fichier CV manquant (cv_enseignant).'}), 400
        cv_file = request.files['cv_enseignant']
        if not cv_file or cv_file.filename == '':
            return jsonify({'success': False, 'error': 'Aucun fichier CV sélectionné.'}), 400

        texte_cv    = CVParser.extraire_texte(cv_file)
        sections_cv = CVParser.parser_cv_enseignant(texte_cv)

        # ── Bio de l'enseignant (champ requis, pas extrait du CV) ─────────────
        bio_enseignant = request.form.get('bio_enseignant', '').strip()
        # On accepte aussi le champ générique "bio" pour rétrocompatibilité
        if not bio_enseignant:
            bio_enseignant = request.form.get('bio', '').strip()

        # ── Données de l'offre ────────────────────────────────────────────────
        matiere     = request.form.get('matiere',     '').strip()
        niveau      = request.form.get('niveau',      '').strip()
        description = request.form.get('description', '').strip()

        if not matiere:
            return jsonify({'success': False, 'error': 'Le champ "matiere" est obligatoire.'}), 400

        offre = {
            'matiere': matiere,
            'niveau':  niveau,
            'bio':     description,   # OffreRepetition.bio
        }

        # ── Années d'expérience ───────────────────────────────────────────────
        annees_experience = request.form.get('annees_experience', 0)

        # ── Calcul du matching de profil ─────────────────────────────────────
        resultat = matching_service.calculer_matching_profil(
            sections_cv=sections_cv,
            bio_enseignant=bio_enseignant,
            offre=offre,
            annees_experience=annees_experience,
        )

        mP = resultat['mP']

        # Interprétation
        if mP >= 8:
            interpretation = "Excellent match — Enseignant fortement recommandé"
        elif mP >= 6:
            interpretation = "Bon match — Enseignant qualifié"
        elif mP >= 4:
            interpretation = "Match moyen — À considérer avec réserve"
        else:
            interpretation = "Faible match — Enseignant peu adapté"

        # Aperçu des sections extraites du CV
        def apercu(t, n=200):
            return (t[:n] + '…') if len(t) > n else t

        data = {
            'score_final':         mP,          # champ lu par MatchingConfirmService.java
            'mP':                  mP,
            'scores_details':      resultat['scores_details'],
            'ponderations_pct':    resultat['ponderations_pct'],
            'interpretation':      interpretation,
            'sections_cv_extraites': {
                'specialite': apercu(sections_cv.get('specialite', '')),
                'experience': apercu(sections_cv.get('experience', '')),
                'diplome':    apercu(sections_cv.get('diplome',    '')),
            },
        }

        return jsonify({'success': True, 'data': data}), 200

    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500


# ── /api/matching/batch  (matching de plusieurs enseignants en une requête) ──
@app.route('/api/matching/batch', methods=['POST'])
def matching_batch():
    """
    Calcule le matching de profil pour plusieurs enseignants en une seule requête.

    Form-data attendu :
      Fichiers (clé répétée) :
        cv_files[]           : Un ou plusieurs CVs (PDF, DOCX ou TXT)

      Données enseignants :
        noms_enseignants     : Noms complets séparés par des virgules
                               (même ordre que les fichiers cv_files[])
        bios_enseignants     : Bios séparées par "|||"
                               (même ordre que les fichiers cv_files[])
        annees_experience    : Valeurs séparées par des virgules (même ordre)
        statuts_verifies     : "true,true,false,…" (même ordre)
        sections_enseignants : "francophone,anglophone,…" (même ordre)

      Données de l'offre :
        matiere              : Matière de l'offre
        niveau               : Niveau de l'élève
        description          : Bio / description de l'offre
        section_demandee     : Section linguistique souhaitée

    Réponse JSON :
      {
        "success": true,
        "resultats": [
          {
            "nom_enseignant": "...",
            "matiere": "...",
            "score": <mP>,
            "interpretation": "..."
          },
          …
        ]
      }
      Triés par score décroissant.
    """
    try:
        # ── Données de l'offre ────────────────────────────────────────────────
        matiere     = request.form.get('matiere',     '').strip()
        niveau      = request.form.get('niveau',      '').strip()
        description = request.form.get('description', '').strip()
        section_demandee = request.form.get('section_demandee', '').lower().strip()

        if not matiere:
            return jsonify({'success': False, 'error': 'Le champ "matiere" est obligatoire.'}), 400

        offre = {'matiere': matiere, 'niveau': niveau, 'bio': description}

        # ── Données enseignants ───────────────────────────────────────────────
        noms_raw    = request.form.get('noms_enseignants', '')
        bios_raw    = request.form.get('bios_enseignants', '')
        annees_raw  = request.form.get('annees_experience', '')
        statuts_raw = request.form.get('statuts_verifies', '')
        sections_raw = request.form.get('sections_enseignants', '')

        noms    = [n.strip() for n in noms_raw.split(',')    if n.strip()]
        bios    = [b.strip() for b in bios_raw.split('|||')] if bios_raw else []
        annees  = [a.strip() for a in annees_raw.split(',')]  if annees_raw else []
        statuts = [s.strip().lower() == 'true' for s in statuts_raw.split(',')] if statuts_raw else []
        sections = [s.strip().lower() for s in sections_raw.split(',')] if sections_raw else []

        cv_files = request.files.getlist('cv_files') or request.files.getlist('cv_files[]')

        if not cv_files:
            return jsonify({'success': False, 'error': 'Aucun fichier CV fourni (cv_files[]).'}), 400
        if not noms:
            return jsonify({'success': False, 'error': 'Le champ "noms_enseignants" est obligatoire.'}), 400
        if len(cv_files) != len(noms):
            return jsonify({
                'success': False,
                'error':   f'{len(cv_files)} CV(s) reçu(s) mais {len(noms)} nom(s) fourni(s).',
            }), 400

        resultats = []

        for i, (cv_file, nom) in enumerate(zip(cv_files, noms)):

            # Filtre : statut vérifié
            statut_ok = statuts[i] if i < len(statuts) else True
            if not statut_ok:
                print(f"⚠️  {nom} : non vérifié, ignoré.")
                continue

            # Filtre : section
            section_ens = sections[i] if i < len(sections) else ''
            if section_ens and section_demandee and section_ens != section_demandee:
                print(f"⚠️  {nom} : section incompatible ({section_ens} ≠ {section_demandee}), ignoré.")
                continue

            try:
                texte_cv    = CVParser.extraire_texte(cv_file)
                sections_cv = CVParser.parser_cv_enseignant(texte_cv)
            except Exception as parse_err:
                print(f"⚠️  {nom} : erreur lecture CV — {parse_err}")
                continue

            bio_enseignant    = bios[i]    if i < len(bios)   else ''
            annees_experience = annees[i]  if i < len(annees) else 0

            resultat = matching_service.calculer_matching_profil(
                sections_cv=sections_cv,
                bio_enseignant=bio_enseignant,
                offre=offre,
                annees_experience=annees_experience,
            )

            mP = resultat['mP']

            if mP >= 8:
                interpretation = "Excellent match — Enseignant fortement recommandé"
            elif mP >= 6:
                interpretation = "Bon match — Enseignant qualifié"
            elif mP >= 4:
                interpretation = "Match moyen — À considérer avec réserve"
            else:
                interpretation = "Faible match — Enseignant peu adapté"

            resultats.append({
                'nom_enseignant': nom,
                'matiere':        matiere,
                'score':          mP,
                'interpretation': interpretation,
                'scores_details': resultat['scores_details'],
            })

        # Tri décroissant par score
        resultats.sort(key=lambda x: x['score'], reverse=True)

        print(f"✅ Matching batch terminé : {len(resultats)} enseignant(s) analysé(s)")
        return jsonify({'success': True, 'resultats': resultats}), 200

    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500


# ─────────────────────────────────────────────────────────────────────────────
# TEMPLATE HTML (page de test)
# ─────────────────────────────────────────────────────────────────────────────
HTML_TEMPLATE = '''
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Educia – Matching Profil Enseignant</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #1a6e3c 0%, #27ae60 100%);
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 960px;
            margin: 0 auto;
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            overflow: hidden;
        }

        .header {
            background: linear-gradient(135deg, #1a6e3c 0%, #27ae60 100%);
            color: white;
            padding: 40px;
            text-align: center;
        }

        .header h1 { font-size: 2.1em; margin-bottom: 8px; }
        .header p  { opacity: 0.9; font-size: 1em; }
        .badge {
            display: inline-block;
            background: rgba(255,255,255,0.25);
            border-radius: 20px;
            padding: 4px 14px;
            font-size: 0.82em;
            margin-top: 10px;
        }

        .content { padding: 40px; }

        .section { margin-bottom: 30px; }

        .section h2 {
            color: #1a6e3c;
            margin-bottom: 15px;
            font-size: 1.3em;
            border-bottom: 2px solid #27ae60;
            padding-bottom: 8px;
        }

        .row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

        .form-group { margin-bottom: 16px; }

        label {
            display: block;
            font-weight: 600;
            margin-bottom: 6px;
            color: #333;
            font-size: 0.93em;
        }

        input[type="file"] {
            width: 100%;
            padding: 12px;
            border: 2px dashed #27ae60;
            border-radius: 8px;
            background: #f4fbf7;
            cursor: pointer;
        }

        input[type="text"], input[type="number"], textarea, select {
            width: 100%;
            padding: 10px 12px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-family: inherit;
            font-size: 14px;
            resize: vertical;
            transition: border-color 0.3s;
        }

        input:focus, textarea:focus, select:focus {
            outline: none;
            border-color: #27ae60;
        }

        button {
            background: linear-gradient(135deg, #1a6e3c 0%, #27ae60 100%);
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
            box-shadow: 0 10px 20px rgba(39,174,96,0.3);
        }

        .loader { display: none; text-align: center; padding: 30px; }
        .loader.active { display: block; }
        .spinner {
            width: 50px; height: 50px;
            border: 4px solid #f3f3f3;
            border-top: 4px solid #27ae60;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 15px;
        }
        @keyframes spin { to { transform: rotate(360deg); } }

        .error {
            display: none;
            background: #ffebee;
            border: 1px solid #f44336;
            border-radius: 8px;
            padding: 15px;
            color: #c62828;
            margin-top: 20px;
        }
        .error.show { display: block; }

        .results { display: none; margin-top: 30px; }
        .results.show { display: block; }

        .score-final {
            background: linear-gradient(135deg, #1a6e3c, #27ae60);
            color: white;
            border-radius: 16px;
            padding: 30px;
            text-align: center;
            margin-bottom: 25px;
        }

        .mP-label    { font-size: 0.95em; opacity: 0.85; margin-bottom: 5px; }
        .score-number { font-size: 4em; font-weight: 700; }
        .interpretation { font-size: 1.1em; margin-top: 10px; }

        .scores-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 15px;
            margin-bottom: 25px;
        }

        .score-card {
            background: #f4fbf7;
            border: 1px solid #c8e6c9;
            border-radius: 12px;
            padding: 18px;
            text-align: center;
        }

        .score-card h3 { color: #1a6e3c; font-size: 0.88em; margin-bottom: 8px; }
        .score-card .val  { font-size: 2em; font-weight: 700; color: #27ae60; }
        .score-card .wgt  { font-size: 0.78em; color: #888; margin-top: 4px; }

        .sections-extraites { margin-top: 20px; }

        .section-preview {
            background: white;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }

        .section-preview h4 { color: #1a6e3c; margin-bottom: 6px; }
        .section-preview p  { color: #666; font-size: 0.88em; line-height: 1.5; }

        .note {
            background: #fffde7;
            border-left: 4px solid #f9a825;
            border-radius: 6px;
            padding: 10px 14px;
            font-size: 0.85em;
            color: #5d4037;
            margin-bottom: 16px;
        }

        @media (max-width: 640px) {
            .row { grid-template-columns: 1fr; }
            .header h1 { font-size: 1.6em; }
            .content { padding: 20px; }
        }
    </style>
</head>
<body>
<div class="container">

    <div class="header">
        <h1>🎓 Educia – Matching Profil Enseignant</h1>
        <p>Analyse de la correspondance entre le profil d'un enseignant et une offre de répétition</p>
        <span class="badge">Score retourné : mP (matching profil) · Localisation traitée séparément</span>
    </div>

    <div class="content">
        <form id="matchingForm" enctype="multipart/form-data">

            <!-- Filtres préalables -->
            <div class="section">
                <h2>🔍 Filtres Préalables</h2>
                <div class="row">
                    <div class="form-group">
                        <label>Statut de l'enseignant</label>
                        <select name="statut_verifie">
                            <option value="true">✅ Vérifié</option>
                            <option value="false">❌ Non vérifié</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Section enseignant</label>
                        <select name="section_enseignant">
                            <option value="francophone">Francophone</option>
                            <option value="anglophone">Anglophone</option>
                        </select>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group">
                        <label>Section demandée (offre)</label>
                        <select name="section_demandee">
                            <option value="francophone">Francophone</option>
                            <option value="anglophone">Anglophone</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Années d'expérience déclarées</label>
                        <input type="number" name="annees_experience" min="0" max="50"
                               placeholder="Ex : 8" value="0">
                    </div>
                </div>
            </div>

            <!-- CV Enseignant -->
            <div class="section">
                <h2>📄 CV de l'Enseignant</h2>
                <div class="form-group">
                    <label>Télécharger le CV (PDF, DOCX, TXT)</label>
                    <input type="file" name="cv_enseignant" accept=".pdf,.docx,.txt" required>
                </div>
            </div>

            <!-- Bio enseignant -->
            <div class="section">
                <h2>✍️ Bio de l'Enseignant</h2>
                <div class="note">
                    ℹ️ La biographie est transmise directement en paramètre — elle n'est pas
                    extraite du CV. Elle correspond au champ <code>bio</code> du profil
                    enseignant en base de données.
                </div>
                <div class="form-group">
                    <label>Bio / Méthodes pédagogiques (bio_enseignant)</label>
                    <textarea name="bio_enseignant" rows="4"
                              placeholder="Ex : J'utilise une pédagogie active centrée sur la résolution de problèmes concrets…"></textarea>
                </div>
            </div>

            <!-- Données de l'offre -->
            <div class="section">
                <h2>📋 Offre de Répétition (OffreRepetition)</h2>
                <div class="row">
                    <div class="form-group">
                        <label>Matière (MatiereOffre.matiere.intitule)</label>
                        <input type="text" name="matiere"
                               placeholder="Ex : Mathématiques, Physique…" required>
                    </div>
                    <div class="form-group">
                        <label>Niveau de l'élève</label>
                        <input type="text" name="niveau"
                               placeholder="Ex : Terminale, 3ème…">
                    </div>
                </div>
                <div class="form-group">
                    <label>Description / Bio de l'offre (OffreRepetition.bio)</label>
                    <textarea name="description" rows="4"
                              placeholder="Ex : Mon enfant a des difficultés en algèbre et en géométrie. Nous cherchons un enseignant capable de préparer efficacement le baccalauréat…"></textarea>
                </div>
            </div>

            <button type="submit" id="submitBtn">🚀 Calculer le Matching de Profil (mP)</button>
        </form>

        <div class="loader" id="loader">
            <div class="spinner"></div>
            <p>Analyse sémantique en cours…</p>
        </div>

        <div class="error" id="error"></div>

        <div class="results" id="results">
            <div class="score-final">
                <div class="mP-label">Score de matching profil (mP)</div>
                <div class="score-number" id="scoreFinal">--</div>
                <div style="font-size:0.82em;opacity:0.8;margin-top:6px;">sur 10 · Localisation (mL) traitée séparément</div>
                <div class="interpretation" id="interpretation"></div>
            </div>

            <h3 style="color:#1a6e3c;margin-bottom:15px;">Détail des 5 Critères de mP</h3>
            <div class="scores-grid">
                <div class="score-card">
                    <h3>Spécialité</h3>
                    <div class="val" id="scoreSpecialite">--</div>
                    <div class="wgt">30%</div>
                </div>
                <div class="score-card">
                    <h3>Années d'expérience</h3>
                    <div class="val" id="scoreAnnees">--</div>
                    <div class="wgt">10%</div>
                </div>
                <div class="score-card">
                    <h3>Pertinence expériences</h3>
                    <div class="val" id="scoreExperience">--</div>
                    <div class="wgt">20%</div>
                </div>
                <div class="score-card">
                    <h3>Bio / Méthodes</h3>
                    <div class="val" id="scoreBio">--</div>
                    <div class="wgt">20%</div>
                </div>
                <div class="score-card">
                    <h3>Diplôme</h3>
                    <div class="val" id="scoreDiplome">--</div>
                    <div class="wgt">20%</div>
                </div>
            </div>

            <div class="sections-extraites">
                <h3 style="color:#1a6e3c;margin-bottom:15px;">Sections Extraites du CV</h3>
                <div class="section-preview">
                    <h4>🎯 Spécialité</h4>
                    <p id="extractSpecialite">…</p>
                </div>
                <div class="section-preview">
                    <h4>📊 Expériences</h4>
                    <p id="extractExperience">…</p>
                </div>
                <div class="section-preview">
                    <h4>🎓 Diplôme</h4>
                    <p id="extractDiplome">…</p>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    const form      = document.getElementById('matchingForm');
    const loader    = document.getElementById('loader');
    const results   = document.getElementById('results');
    const errorDiv  = document.getElementById('error');
    const submitBtn = document.getElementById('submitBtn');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        results.classList.remove('show');
        errorDiv.classList.remove('show');
        loader.classList.add('active');
        submitBtn.disabled = true;

        try {
            const resp = await fetch('/api/matching/enseignant', {
                method: 'POST',
                body: new FormData(form),
            });
            const data = await resp.json();

            if (resp.ok && data.success) {
                afficherResultats(data.data);
            } else {
                afficherErreur(data.error || 'Erreur inconnue');
            }
        } catch (err) {
            afficherErreur('Erreur de connexion : ' + err.message);
        } finally {
            loader.classList.remove('active');
            submitBtn.disabled = false;
        }
    });

    function afficherResultats(d) {
        document.getElementById('scoreFinal').textContent  = d.mP + '/10';
        document.getElementById('interpretation').textContent = d.interpretation;

        const det = d.scores_details;
        document.getElementById('scoreSpecialite').textContent = det.specialite;
        document.getElementById('scoreAnnees').textContent     = det.annees_experience;
        document.getElementById('scoreExperience').textContent = det.pertinence_experience;
        document.getElementById('scoreBio').textContent        = det.bio;
        document.getElementById('scoreDiplome').textContent    = det.diplome;

        const ext = d.sections_cv_extraites;
        document.getElementById('extractSpecialite').textContent = ext.specialite || 'Aucune section trouvée';
        document.getElementById('extractExperience').textContent = ext.experience || 'Aucune section trouvée';
        document.getElementById('extractDiplome').textContent    = ext.diplome    || 'Aucune section trouvée';

        results.classList.add('show');
    }

    function afficherErreur(msg) {
        errorDiv.textContent = '❌ ' + msg;
        errorDiv.classList.add('show');
    }
</script>
</body>
</html>
'''

if __name__ == '__main__':
    print("\n" + "=" * 62)
    print("🚀 Educia – API Matching Profil Enseignant  v3.0")
    print("=" * 62)
    print("📍 UI   : http://localhost:5000")
    print("📍 API  : POST http://localhost:5000/api/matching/enseignant")
    print("📍 Batch: POST http://localhost:5000/api/matching/batch")
    print("📍 Health: GET http://localhost:5000/api/health")
    print("=" * 62 + "\n")
    app.run(debug=True, host='0.0.0.0', port=5000)