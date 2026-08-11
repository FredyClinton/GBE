'use client';

// ============================================================
// FICHIER  : src/app/dashboard/apercu/page.tsx
// RÔLE     : Écran d'accueil. Cartes agrandies et plus "posées"
//            (padding, bordure visible, ombre légère) pour être
//            fidèles au mockup — la version précédente utilisait
//            un padding et une bordure trop discrets.
// ============================================================

import React, {useEffect, useState} from 'react';
import Link from 'next/link';
import {ArrowRight} from 'lucide-react';
import {getNaturesEconomiques, getSections} from '@/lib/referentielService';

function MetricCard({label, value, hint}: { label: string; value: number | null; hint: string }) {
    return (
        <div className="metric-card">
            <div className="metric-card__label">{label}</div>
            {value === null ? (
                <div className="h-8 w-16 rounded-md bg-[var(--clr-gray-100)] animate-pulse"/>
            ) : (
                <div className="metric-card__value">{value}</div>
            )}
            <div className="metric-card__hint">{hint}</div>
        </div>
    );
}

export default function ApercuPage() {
    const [nbSections, setNbSections] = useState<number | null>(null);
    const [nbNatures, setNbNatures] = useState<number | null>(null);

    useEffect(() => {
        getSections().then(list => setNbSections(list.length)).catch(() => setNbSections(0));
        getNaturesEconomiques().then(list => setNbNatures(list.length)).catch(() => setNbNatures(0));
    }, []);

    return (
        <div className="dash-container">
            <div className="page-header">
                <div className="page-eyebrow">MINFI - Direction Générale du Budget</div>
                <h1 className="page-title text-[28px] sm:text-[30px]">
                    Vue d&apos;ensemble
                </h1>
                <p className="page-subtitle text-sm mt-2">
                    Plateforme de gestion budgétaire
                </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 mb-6">
                <MetricCard label="Chapitres référencés" value={nbSections} hint="ministères et institutions"/>
                <MetricCard label="Natures économiques" value={nbNatures} hint="rubriques disponibles"/>
            </div>

            <Link href="/dashboard/predictions" className="promo-card">
                <div>
                    <div className="promo-card__title">Prédire un profil d&apos;engagement</div>
                    <div className="promo-card__subtitle">Chapitre, nature économique, dotation AE</div>
                </div>
                <div className="promo-card__arrow">
                    <ArrowRight size={19} aria-hidden="true"/>
                </div>
            </Link>
        </div>
    );
}