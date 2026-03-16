#!/bin/bash

# --- Configuration ---

# Chemin par défaut ou premier argument
PROJECT_PATH="${1:-.}"
OUTPUT_FILENAME="project_context_frontend.txt"

# Résolution du chemin absolu
PROJECT_PATH=$(realpath "$PROJECT_PATH" 2>/dev/null)

if [ ! -d "$PROJECT_PATH" ]; then
    echo "Erreur: Répertoire invalide ($PROJECT_PATH)." >&2
    exit 1
fi

OUTPUT_FILE="$PROJECT_PATH/$OUTPUT_FILENAME"

# Dossiers à ignorer totalement (prune)
EXCLUDE_DIRS=(
    ".git" ".vscode" ".idea" "node_modules" ".next" 
    "out" "build" "coverage" "public" "dist" ".vercel"
)

# Fichiers à ignorer (patterns)
EXCLUDE_FILES=(
    "*.log" "package-lock.json" "yarn.lock" "pnpm-lock.yaml" "bun.lockb"
    ".env*" "*.ico" "*.png" "*.jpg" "*.jpeg" "*.svg" "*.webp" 
    "*.pdf" "*.map" "*.ttf" "*.woff" "*.woff2" "*.eot" "*.mp4"
    "$OUTPUT_FILENAME"
)

# --- Préparation de la commande find ---

# Construction des arguments d'exclusion de dossiers
PRUNE_ARGS=()
for dir in "${EXCLUDE_DIRS[@]}"; do
    if [ ${#PRUNE_ARGS[@]} -gt 0 ]; then PRUNE_ARGS+=("-o"); fi
    PRUNE_ARGS+=("-name" "$dir")
done

# Construction des arguments d'exclusion de fichiers
FILE_ARGS=()
for file in "${EXCLUDE_FILES[@]}"; do
    FILE_ARGS+=("!" "-name" "$file")
done

# --- Exécution ---

echo "Recherche des fichiers dans : $PROJECT_PATH..."

# Entête du fichier
{
    echo "Next.js Project Context"
    echo "Generated On: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "Root: $PROJECT_PATH"
    echo "==============================================="
} > "$OUTPUT_FILE"

# Recherche et traitement des fichiers
# 1. On ignore les dossiers listés
# 2. On prend uniquement les fichiers (-type f)
# 3. On applique les exclusions de noms de fichiers
find "$PROJECT_PATH" \( "${PRUNE_ARGS[@]}" \) -prune -o -type f "${FILE_ARGS[@]}" -print | sort | while read -r FILE; do
    
    # Calcul du chemin relatif
    RELATIVE_PATH="${FILE#"$PROJECT_PATH"/}"
    
    echo "Traitement de : $RELATIVE_PATH"

    {
        echo -e "\n// FILE: $RELATIVE_PATH"
        echo "-----------------------------------------------"
    } >> "$OUTPUT_FILE"

    # Vérification si le fichier est binaire
    # Sur Linux, 'grep -I' est le moyen le plus rapide de détecter un binaire
    if grep -qI . "$FILE"; then
        cat "$FILE" >> "$OUTPUT_FILE"
    else
        echo "[Fichier binaire ou asset omis]" >> "$OUTPUT_FILE"
    fi

    echo -e "\n// END OF FILE: $RELATIVE_PATH" >> "$OUTPUT_FILE"
done

echo "-----------------------------------------------"
echo "Succès ! Le contexte est prêt : $OUTPUT_FILE"
