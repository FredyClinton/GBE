"""
Transforme data.sql et nature_economique_import.sql vers le schéma
des entités JPA actuelles du projet GBE.
Génère src/main/resources/data.sql
"""

import re
import sys
from pathlib import Path

BASE = Path(__file__).parent


def parse_values(values_str: str) -> list[str]:
    """Parse une chaîne de valeurs SQL en tenant compte des quotes."""
    values = []
    current = ""
    depth = 0
    in_quote = False
    i = 0
    while i < len(values_str):
        c = values_str[i]
        if c == "'" and not in_quote:
            in_quote = True
            current += c
        elif c == "'" and in_quote:
            if i + 1 < len(values_str) and values_str[i + 1] == "'":
                current += "''"
                i += 2
                continue
            else:
                in_quote = False
                current += c
        elif c == "," and not in_quote and depth == 0:
            values.append(current.strip())
            current = ""
            i += 1
            continue
        elif c == "(" and not in_quote:
            depth += 1
            current += c
        elif c == ")" and not in_quote:
            depth -= 1
            current += c
        else:
            current += c
        i += 1
    if current.strip():
        values.append(current.strip())
    return values


def parse_insert(line: str):
    """Retourne (table, cols_list, vals_list) ou None."""
    m = re.match(r"INSERT INTO (\w+)\s*\(([^)]+)\)\s*VALUES\s*\((.+)\);?\s*$", line, re.IGNORECASE | re.DOTALL)
    if not m:
        return None
    table = m.group(1).lower()
    cols = [c.strip() for c in m.group(2).split(",")]
    vals = parse_values(m.group(3))
    return table, cols, vals


def build_insert(table: str, col_map: dict, cols: list, vals: list) -> str | None:
    """
    col_map: {old_col: new_col} — colonnes à garder avec renommage éventuel.
             Si new_col est None, la colonne est ignorée.
             Si la valeur de new_col est un tuple (new_col, literal), on injecte literal.
    """
    col_idx = {c: i for i, c in enumerate(cols)}
    new_cols = []
    new_vals = []

    for old_col, new_col in col_map.items():
        if isinstance(new_col, tuple):
            # (new_col_name, literal_value)
            new_cols.append(new_col[0])
            new_vals.append(new_col[1])
        elif new_col is None:
            continue
        else:
            if old_col not in col_idx:
                continue
            idx = col_idx[old_col]
            if idx >= len(vals):
                continue
            new_cols.append(new_col)
            new_vals.append(vals[idx])

    return f"INSERT INTO {table} ({', '.join(new_cols)}) VALUES ({', '.join(new_vals)});"


# ── Mappings ────────────────────────────────────────────────────────────────
# Format: old_col → new_col  (None = ignoré, tuple = valeur littérale injectée)

EXERCICE_MAP = {
    "id":                 "id",
    "created_by":         "created_by",
    "created_date":       "created_date",
    "last_modified_date": "last_modified_date",
    "last_midified_by":   "last_midified_by",
    "actif":              "actif",
    "annee":              "annee",
    "code":               "code_exercice",
    # libelle_fr / libelle_en non présents dans data.sql → on injecte NULL
    "_libelle_fr":        ("libelle_fr", "NULL"),
    "_libelle_en":        ("libelle_en", "NULL"),
    # date_debut / date_fin ignorés
}

SECTION_MAP = {
    "id":                 "id",
    "created_date":       "created_date",
    "created_by":         "created_by",
    "last_modified_date": "last_modified_date",
    "last_midified_by":   "last_midified_by",
    "code":               "code",
    "code_exercice":      "code_exercice",
    "sigle":              "sigle",
    "libelle":            "libelle",
    "libelle_en":         "libelle_en",
    "exercice_id":        "exercice_id",
}

CHAPITRE_MAP = {
    "id":                 "id",
    "created_date":       "created_date",
    "created_by":         "created_by",
    "last_modified_date": "last_modified_date",
    "last_midified_by":   "last_midified_by",
    "code":               "code",
    "code_chap":          "code_chap",
    "code_exercice":      "code_exercice",
    "code_section":       "code_section",
    "libelle":            "libelle",
    "libelle_en":         "libelle_en",
    "section_id":         "section_id",
    "exercice_id":        "exercice_id",
}

PROGRAMME_MAP = {
    "id":                 "id",
    "created_date":       "created_date",
    "created_by":         "created_by",
    "last_modified_date": "last_modified_date",
    "last_midified_by":   "last_midified_by",
    "code":               "code",
    "code_exercice":      "code_exercice",
    "code_section":       "code_section",
    "libelle":            "libelle",
    "libelle_en":         "libelle_en",
    "numero":             "numero",
    "section_id":         "section_id",
    "exercice_id":        "exercice_id",
}

ACTION_MAP = {
    "id":                 "id",
    "created_date":       "created_date",
    "created_by":         "created_by",
    "last_modified_date": "last_modified_date",
    "last_midified_by":   "last_midified_by",
    "code":               "code",
    "code_chap":          "code_chap",
    "code_exercice":      "code_exercice",
    "code_programme":     "code_programme",
    "code_section":       "code_section",
    "libelle":            "libelle",
    "libelle_en":         "libelle_en",
    "numero":             "numero",
    "numero_programme":   "numero_programme",
    "programme_id":       "programme_id",
    "section_id":         "section_id",
    "exercice_id":        "exercice_id",
}

NATURE_MAP = {
    "id":                 "id",
    "created_by":         "created_by",
    "created_date":       "created_date",
    "last_modified_date": "last_modified_date",
    "last_midified_by":   "last_midified_by",
    "code":               "code",
    "code_titre":         "code_titre",
    "code_article":       "code_article",
    "code_paragraphe":    "code_paragraphe",
    "code_rubrique":      "code_rubrique",
    "libelle":            "libelle",
    "libelle_en":         "libelle_en",
    "_code_exercice":     ("code_exercice", "NULL"),
    "exercice_id":        "exercice_id",
    # old_id ignoré
}

TABLE_MAPS = {
    "exercices":           ("exercice",            EXERCICE_MAP),
    "sections":            ("sections",            SECTION_MAP),
    "chapitres":           ("chapitres",           CHAPITRE_MAP),
    "programmes":          ("programmes",          PROGRAMME_MAP),
    "actions":             ("actions",             ACTION_MAP),
    "natures_economiques": ("natures_economiques", NATURE_MAP),
}

REFERENTIAL_TABLES = set(TABLE_MAPS.keys())


def transform_file(path: Path, out_lines: list):
    with open(path, encoding="utf-8") as f:
        for raw_line in f:
            line = raw_line.rstrip()
            if not line.upper().startswith("INSERT INTO"):
                continue
            parsed = parse_insert(line)
            if not parsed:
                continue
            table, cols, vals = parsed
            if table not in TABLE_MAPS:
                continue
            new_table, col_map = TABLE_MAPS[table]
            stmt = build_insert(new_table, col_map, cols, vals)
            if stmt:
                out_lines.append(stmt)


def main():
    out = [
        "-- ============================================================",
        "-- GBE - Données référentielles (généré par transform_data.py)",
        "-- ============================================================",
        "",
        "-- Désactive les contraintes FK temporairement",
        "SET session_replication_role = replica;",
        "",
    ]

    print("Transformation de data.sql …")
    transform_file(BASE / "data.sql", out)

    out.append("")
    out.append("-- natures_economiques")
    print("Transformation de nature_economique_import.sql …")
    transform_file(BASE / "nature_economique_import.sql", out)

    out.append("")
    out.append("-- Réactive les contraintes")
    out.append("SET session_replication_role = DEFAULT;")

    dest = BASE.parent / "src" / "main" / "resources" / "data.sql"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text("\n".join(out) + "\n", encoding="utf-8")
    print(f"Fichier généré : {dest}  ({dest.stat().st_size // 1024} Ko)")


if __name__ == "__main__":
    main()
