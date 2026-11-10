# Analyse fonctionnelle préliminaire — Application de suivi des dépenses budgétaires

Ce dépôt contient les livrables de la première étape d'analyse fonctionnelle et de modélisation UML de l'application de suivi des dépenses budgétaires, élaborés à partir des notes de la réunion de cadrage avec le responsable du projet.

## 1. Structure du dossier

```text
analyse-fonctionnelle/
│
├── rapport/
│   ├── main.tex                              # Fichier maître LaTeX (A4, XeLaTeX / LuaLaTeX)
│   ├── main.pdf                              # Rapport compilé (PDF)
│   └── sections/                             # Chapitres modulaires du rapport
│       ├── 01_page_de_garde.tex              # 1. Page de garde
│       ├── 02_introduction.tex               # 2. Introduction
│       ├── 03_contexte_objectifs.tex         # 3. Contexte et objectifs du projet
│       ├── 04_processus_metier.tex           # 4. Présentation du processus métier
│       ├── 05_identification_acteurs.tex     # 5. Identification des acteurs
│       ├── 06_description_responsabilites.tex# 6. Description des responsabilités
│       ├── 07_cas_utilisation.tex            # 7. Présentation des principaux cas d'utilisation
│       ├── 08_diagramme_uml.tex              # 8. Diagramme général des cas d'utilisation UML
│       ├── 09_points_a_clarifier.tex         # 9. Points à clarifier avec le responsable métier
│       └── 10_conclusion.tex                 # 10. Conclusion
│
├── diagrammes/
│   ├── cas-utilisation-general.drawio        # Diagramme UML natif Draw.io (modifiable, format XML)
│   ├── cas-utilisation-general.pdf           # Export vectoriel inclus dans le rapport LaTeX
│   └── cas-utilisation-general.png           # Aperçu matriciel haute résolution
│
└── README.md                                 # Guide de compilation et d'utilisation
```

---

## 2. Ouverture et modification du diagramme UML dans Draw.io (diagrams.net)

Le diagramme général des cas d'utilisation est fourni au format XML natif non compressé dans le fichier :

```text
diagrammes/cas-utilisation-general.drawio
```

### Avec l'application de bureau Draw.io
1. Ouvrez l'application **Draw.io** (`diagrams.net`).
2. Cliquez sur **Fichier > Ouvrir depuis > Appareil...** (ou exécutez directement `drawio diagrammes/cas-utilisation-general.drawio` en ligne de commande).
3. Sélectionnez le fichier `diagrammes/cas-utilisation-general.drawio`.

### Avec la version web (navigateur)
1. Rendez-vous sur [https://app.diagrams.net/](https://app.diagrams.net/).
2. Choisissez **Ouvrir un diagramme existant** et sélectionnez `diagrammes/cas-utilisation-general.drawio`.

### Régénération de l'export PDF pour le rapport (après modification du diagramme)
Si vous modifiez le fichier `.drawio`, vous pouvez mettre à jour l'export PDF inclus dans le rapport LaTeX via l'interface (**Fichier > Exporter en tant que > PDF**, option *Recadrer / Crop*) ou en ligne de commande :

```bash
drawio --export --format pdf --crop \
  -o diagrammes/cas-utilisation-general.pdf \
  diagrammes/cas-utilisation-general.drawio
```

---

## 3. Compilation du rapport LaTeX

Le document est conçu pour être compilé avec **XeLaTeX** ou **LuaLaTeX** (encodage UTF-8 natif, gestion des polices via `fontspec` et typographie française via `babel`).

### Prérequis
Une distribution TeX Live (ou MiKTeX) disposant de `xelatex` ou `lualatex` et des paquets standards : `fontspec`, `babel` (français), `geometry`, `xcolor`, `graphicx`, `pdflscape`, `booktabs`, `tabularx`, `longtable`, `enumitem`, `titlesec`, `fancyhdr`, `lastpage`, `tcolorbox`, `hyperref`.

### Compilation avec XeLaTeX (recommandé)
Depuis le dossier `rapport/`, exécutez deux passes afin de générer correctement la table des matières et la numérotation des pages (`Page X sur Y`) :

```bash
cd rapport
xelatex -interaction=nonstopmode main.tex
xelatex -interaction=nonstopmode main.tex
```

### Compilation avec LuaLaTeX
```bash
cd rapport
lualatex -interaction=nonstopmode main.tex
lualatex -interaction=nonstopmode main.tex
```

### Compilation automatisée avec `latexmk`
```bash
cd rapport
latexmk -xelatex main.tex
```

Le fichier résultant est généré sous le nom `rapport/main.pdf`.
