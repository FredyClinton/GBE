'use client';

// ============================================================
// FICHIER  : src/app/dashboard/predictions/page.tsx
// RÔLE     : Formulaire de prédiction AE → appel Spring →
//            affichage du résultat. Enveloppé par
//            dashboard/layout.tsx (sidebar + garde d'accès).
//
// CETTE VERSION :
//   - Grille du formulaire élargie (gap-6 au lieu de gap-5)
//   - Squelette (animate-pulse) sur Exercice/Chapitre/Nature
//     économique pendant le chargement du référentiel, au lieu
//     d'un <Select> vide et grisé
//   - Espacement vertical entre sections augmenté
// ============================================================

import React, {useEffect, useState} from 'react';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import {getExercices, getNaturesEconomiques, getSections} from '@/lib/referentielService';
import {predireProfilAE} from '@/lib/predictionService';
import {
    ExerciceOption,
    NatureEconomiqueOption,
    PredictionAEResult,
    PredictionFormErrors,
    SectionOption,
} from '@/types/prediction';

const MOIS = [
    'Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun',
    'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc',
];

const formatFCFA = (n: number) =>
    new Intl.NumberFormat('fr-FR', {maximumFractionDigits: 0}).format(n);

const IconAlert = () => (
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="2" className="alert__icon">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
    </svg>
);

function FieldSkeleton({label}: { label: string }) {
    return (
        <div className="flex flex-col gap-2">
      <span className="text-[13px] font-semibold text-[var(--clr-gray-600)] tracking-[.025em]">
        {label}
      </span>
            <div className="h-[46px] rounded-md bg-[var(--clr-gray-100)] animate-pulse"/>
        </div>
    );
}

export default function PredictionsPage() {
    const [sections, setSections] = useState<SectionOption[]>([]);
    const [natures, setNatures] = useState<NatureEconomiqueOption[]>([]);
    const [exercices, setExercices] = useState<ExerciceOption[]>([]);
    const [loadingReferentiels, setLoadingReferentiels] = useState(true);
    const [loadError, setLoadError] = useState('');

    const [chapitreCode, setChapitreCode] = useState('');
    const [rubriqueCode, setRubriqueCode] = useState('');
    const [exerciceCode, setExerciceCode] = useState<number | ''>('');
    const [dotationAE, setDotationAE] = useState('');
    const [errors, setErrors] = useState<PredictionFormErrors>({});

    const [isPredicting, setIsPredicting] = useState(false);
    const [predictionError, setPredictionError] = useState('');
    const [result, setResult] = useState<PredictionAEResult | null>(null);

    useEffect(() => {
        async function loadReferentiels() {
            try {
                const [sectionsRes, naturesRes, exercicesRes] = await Promise.all([
                    getSections(),
                    getNaturesEconomiques(),
                    getExercices(),
                ]);
                setSections(sectionsRes);
                setNatures(naturesRes);
                setExercices(exercicesRes);
            } catch (err) {
                setLoadError(
                    err instanceof Error ? err.message : 'Impossible de charger les référentiels.'
                );
            } finally {
                setLoadingReferentiels(false);
            }
        }

        loadReferentiels();
    }, []);

    function validate(): PredictionFormErrors {
        const e: PredictionFormErrors = {};
        if (!exerciceCode) e.exercice = 'Sélectionnez un exercice.';
        if (!chapitreCode) e.chapitreCode = 'Sélectionnez un chapitre.';
        if (!rubriqueCode) e.rubriqueCode = 'Sélectionnez une nature économique.';
        const montant = Number(dotationAE);
        if (!dotationAE || Number.isNaN(montant) || montant <= 0) {
            e.dotationAE = 'Entrez une dotation AE strictement positive.';
        }
        return e;
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        const fieldErrors = validate();
        if (Object.keys(fieldErrors).length > 0) {
            setErrors(fieldErrors);
            return;
        }
        setErrors({});
        setPredictionError('');
        setResult(null);
        setIsPredicting(true);
        try {
            const res = await predireProfilAE({
                exercice: Number(exerciceCode),
                chapitreCode,
                rubriqueCode,
                dotationAE: Number(dotationAE),
            });
            setResult(res);
        } catch (err) {
            setPredictionError(
                err instanceof Error ? err.message : 'Erreur lors de la prédiction.'
            );
        } finally {
            setIsPredicting(false);
        }
    }

    const maxMontant = result?.montantsPredits ? Math.max(...result.montantsPredits, 1) : 1;

    return (
        <div className="dash-container">
            <div className="page-eyebrow">MINFI — Direction Générale du Budget</div>
            <h1 className="page-title text-[22px] mb-1.5">
                Prédiction du profil d&apos;engagement AE
            </h1>
            <p className="page-subtitle text-[13.5px] mb-8">
                Module GBE+ - estimation du profil mensuel d&apos;engagement
            </p>

            <div className="dash-card">
                <div className="dash-card__header">
                    <div className="dash-card__title">Paramètres de la prédiction</div>
                    <div className="dash-card__subtitle">
                        Sélectionnez l&apos;exercice, le chapitre et la nature économique concernés.
                    </div>
                </div>

                <form onSubmit={handleSubmit} noValidate className="dash-card__body w-full">
                    {loadError && (
                        <div className="alert alert--error mb-6" role="alert">
                            <IconAlert/> {loadError}
                        </div>
                    )}

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-6">
                        {loadingReferentiels ? (
                            <>
                                <FieldSkeleton label="Exercice"/>
                                <FieldSkeleton label="Section (ministère ou institution)"/>
                                <div className="sm:col-span-2">
                                    <FieldSkeleton label="Nature économique (rubrique)"/>
                                </div>
                            </>
                        ) : (
                            <>
                                <Select
                                    label="Exercice"
                                    value={exerciceCode}
                                    onChange={(e) => setExerciceCode(e.target.value ? Number(e.target.value) : '')}
                                    error={errors.exercice}
                                >
                                    <option value="">Sélectionner…</option>
                                    {exercices.map((ex) => (
                                        <option key={ex.id} value={ex.codeExercice}>
                                            {ex.libelleFr} ({ex.annee})
                                        </option>
                                    ))}
                                </Select>

                                <Select
                                    label="Section (ministère ou institution)"
                                    value={chapitreCode}
                                    onChange={(e) => setChapitreCode(e.target.value)}
                                    error={errors.chapitreCode}
                                >
                                    <option value="">Sélectionner…</option>
                                    {sections.map((s) => (
                                        <option key={s.id} value={s.codeSection}>
                                            {s.codeSection} — {s.libelleFr}
                                        </option>
                                    ))}
                                </Select>

                                <div className="sm:col-span-2">
                                    <Select
                                        label="Nature économique (rubrique)"
                                        value={rubriqueCode}
                                        onChange={(e) => setRubriqueCode(e.target.value)}
                                        error={errors.rubriqueCode}
                                    >
                                        <option value="">Sélectionner…</option>
                                        {natures.map((n) => (
                                            <option key={n.id} value={n.code}>
                                                {n.code} — {n.libelleFr}
                                            </option>
                                        ))}
                                    </Select>
                                </div>
                            </>
                        )}

                        <div className="sm:col-span-2">
                            <Input
                                label="Dotation AE (FCFA)"
                                type="number"
                                min="0"
                                step="1"
                                placeholder="ex : 50000000"
                                value={dotationAE}
                                onChange={(e) => setDotationAE(e.target.value)}
                                error={errors.dotationAE}
                            />
                        </div>
                    </div>

                    <div className="mt-9 pt-7 border-t border-[var(--clr-gray-100)]">
                        <Button type="submit" variant="primary" isLoading={isPredicting} fullWidth>
                            Prédire le profil d&apos;engagement
                        </Button>
                    </div>

                    {predictionError && (
                        <div className="alert alert--error mt-6" role="alert">
                            <IconAlert/> {predictionError}
                        </div>
                    )}
                </form>
            </div>

            {result && (
                <div className="dash-card mt-7">
                    <div className="dash-card__header flex items-center justify-between flex-wrap gap-2">
                        <div>
                            <div className="dash-card__title">Profil prédit</div>
                            <div className="dash-card__subtitle">
                                Exercice {result.exercice} — {result.chapitreCode} / {result.rubriqueCode}
                            </div>
                        </div>
                        {result.archetype && (
                            <span className="result-badge">{result.archetype}</span>
                        )}
                    </div>

                    <div className="dash-card__body">
                        {result.avertissement && (
                            <div className="alert alert--info mb-6" role="status">
                                {result.avertissement}
                            </div>
                        )}

                        {result.montantsPredits && (
                            <div className="flex items-end gap-1.5 sm:gap-2 h-40">
                                {result.montantsPredits.map((montant, i) => (
                                    <div key={i} className="flex-1 flex flex-col items-center gap-2 h-full justify-end">
                                        <div
                                            className="w-full rounded-t-md bg-[var(--clr-navy)] hover:bg-[var(--clr-navy-light)] transition-colors"
                                            style={{height: `${Math.max((montant / maxMontant) * 100, 3)}%`}}
                                            title={`${formatFCFA(montant)} FCFA`}
                                        />
                                        <span className="text-[10px] text-[var(--clr-gray-400)]">{MOIS[i]}</span>
                                    </div>
                                ))}
                            </div>
                        )}

                        {result.niveauHistorique && (
                            <p className="text-xs text-[var(--clr-gray-400)] mt-6 pt-5 border-t border-[var(--clr-gray-100)]">
                                Fiabilité de la prédiction : <strong
                                className="text-[var(--clr-gray-600)]">{result.niveauHistorique}</strong>
                            </p>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}