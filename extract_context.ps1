<#
.SYNOPSIS
Extrait le code source d'un projet Next.js pour l'utiliser comme contexte IA.
#>

param (
    [string]$ProjectPath = "."
)

$OutputFilename = "project_context_frontend.txt"

# Résolution du chemin absolu
try {
    $ResolvedPath = (Resolve-Path $ProjectPath -ErrorAction Stop).Path
} catch {
    Write-Error "Erreur: Répertoire invalide ($ProjectPath)."
    exit 1
}

$OutputFile = Join-Path $ResolvedPath $OutputFilename

# Suppression de l'ancien fichier s'il existe
if (Test-Path $OutputFile) {
    Remove-Item $OutputFile -Force
}

# Dossiers à ignorer (optimisation : on n'y entre même pas)
$ExcludeDirs = @(
    ".git", ".vscode", ".idea", "node_modules", ".next", 
    "out", "build", "coverage", "public", "dist", ".vercel"
)

# Fichiers à ignorer (accepte les wildcards *)
$ExcludeFiles = @(
    "*.log", "package-lock.json", "yarn.lock", "pnpm-lock.yaml", "bun.lockb",
    ".env*", "*.ico", "*.png", "*.jpg", "*.jpeg", "*.svg", "*.webp", 
    "*.pdf", "*.map", "*.ttf", "*.woff", "*.woff2", "*.eot", "*.mp4",
    $OutputFilename
)

# Fonction pour vérifier si un fichier est binaire (contient des octets nuls)
function Test-IsBinary {
    param ([string]$FilePath)
    try {
        $bytes = [System.IO.File]::ReadAllBytes($FilePath)
        $checkLength = [math]::Min(1024, $bytes.Length)
        for ($i = 0; $i -lt $checkLength; $i++) {
            if ($bytes[$i] -eq 0) { return $true }
        }
        return $false
    } catch {
        return $true # En cas d'erreur de lecture, on ignore par sécurité
    }
}

$foundFiles = [System.Collections.Generic.List[string]]::new()

# Fonction récursive pour lister les fichiers valides
function Get-ValidFiles {
    param ([string]$CurrentDir)
    
    $items = Get-ChildItem -Path $CurrentDir -Force -ErrorAction SilentlyContinue
    
    foreach ($item in $items) {
        if ($item.PSIsContainer) {
            # Si c'est un dossier et qu'il n'est pas dans la liste d'exclusion
            if ($ExcludeDirs -notcontains $item.Name) {
                Get-ValidFiles -CurrentDir $item.FullName
            }
        } else {
            # Si c'est un fichier, on vérifie son extension/nom
            $skip = $false
            foreach ($pattern in $ExcludeFiles) {
                if ($item.Name -like $pattern) {
                    $skip = $true
                    break
                }
            }
            if (-not $skip) {
                $foundFiles.Add($item.FullName)
            }
        }
    }
}

Write-Host "Recherche des fichiers en cours..." -ForegroundColor Cyan
Get-ValidFiles -CurrentDir $ResolvedPath

# Écriture de l'en-tête
$dateStr = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$header = @"
Next.js Project Context
Generated On: $dateStr
Root: $ResolvedPath
===============================================
"@

Set-Content -Path $OutputFile -Value $header -Encoding UTF8

Write-Host "Génération du fichier de contexte en cours..." -ForegroundColor Cyan

foreach ($file in $foundFiles) {
    # Calcul du chemin relatif
    $relativePath = $file.Substring($ResolvedPath.Length).TrimStart('\')
    $relativePath = $relativePath -replace '\\', '/' # Formatage type UNIX pour l'IA
    
    $fileHeader = "`n// FILE: $relativePath`n-----------------------------------------------"
    Add-Content -Path $OutputFile -Value $fileHeader -Encoding UTF8
    
    if (Test-IsBinary -FilePath $file) {
        Add-Content -Path $OutputFile -Value "[Fichier binaire ou asset omis]" -Encoding UTF8
    } else {
        try {
            # Lecture du contenu brut
            $content = Get-Content -Path $file -Raw -Encoding UTF8 -ErrorAction Stop
            if ($null -ne $content) {
                Add-Content -Path $OutputFile -Value $content -Encoding UTF8
            }
        } catch {
            Add-Content -Path $OutputFile -Value "[Erreur lors de la lecture du fichier]" -Encoding UTF8
        }
    }
    
    Add-Content -Path $OutputFile -Value "`n// END OF FILE: $relativePath`n" -Encoding UTF8
}

Write-Host "Succès ! Ton contexte Next.js est prêt dans : $OutputFile" -ForegroundColor Green