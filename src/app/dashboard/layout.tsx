'use client';

// ============================================================
// FICHIER  : src/app/dashboard/layout.tsx
// RÔLE     : Sidebar de navigation + garde d'accès centralisée.
//
// CETTE VERSION : boutons de nav agrandis (w-full explicite,
// padding et police augmentés) pour un vrai effet "bouton"
// plutôt qu'un lien texte discret.
// ============================================================

import React, {useEffect} from 'react';
import Link from 'next/link';
import {usePathname, useRouter} from 'next/navigation';
import {BarChart3, Home, LogOut} from 'lucide-react';
import {clearSession, getAccessToken} from '@/lib/session';
import {APP_ROUTES} from '@/constants/auth';

const NAV_ITEMS = [
    {href: '/dashboard/apercu', label: 'Vue d\u2019ensemble', Icon: Home},
    {href: '/dashboard/predictions', label: 'Prédiction', Icon: BarChart3},
];

export default function DashboardLayout({
                                            children,
                                        }: {
    children: React.ReactNode;
}) {
    const pathname = usePathname();
    const router = useRouter();

    useEffect(() => {
        if (!getAccessToken()) {
            router.push(APP_ROUTES.LOGIN);
        }
    }, [router]);

    function handleLogout() {
        clearSession();
        router.push(APP_ROUTES.LOGIN);
    }

    return (
        <div className="grid grid-cols-[230px_1fr] min-h-screen">

            <aside className="bg-[#0D2B55] px-4 py-6 flex flex-col">
                <div className="mb-8 px-1.5 py-4">
                    <div className="font-[var(--font-display)] font-bold text-[21px] text-white leading-none">
                        GBE
                    </div>
                    <div className="text-[11px] text-[#A9BAD4] tracking-wide mt-1.5">
                        MINFI Cameroun
                    </div>
                </div>

                <nav className="flex flex-col gap-2 flex-1">
                    {NAV_ITEMS.map(({href, label, Icon}) => {
                        const isActive = pathname.startsWith(href);
                        return (
                            <Link
                                key={href}
                                href={href}
                                className="w-full flex items-center gap-3 px-4 py-5 rounded-lg text-[14.5px] no-underline transition-colors"
                                style={{
                                    background: isActive ? '#C9A227' : 'transparent',
                                    color: isActive ? '#0D2B55' : '#E4EAF3',
                                    fontWeight: isActive ? 600 : 500,
                                }}
                            >
                                <Icon size={19} aria-hidden="true"/>
                                {label}
                            </Link>
                        );
                    })}
                </nav>

                <div className="border-t border-white/15 pt-4 flex items-center gap-2.5">
                    <div
                        className="w-8 h-8 rounded-full bg-[#2451A0] flex items-center justify-center text-xs font-semibold text-white shrink-0">
                        FF
                    </div>
                    <button
                        type="button"
                        onClick={handleLogout}
                        className="flex items-center gap-1.5 text-[11px] text-[#A9BAD4] bg-transparent border-0 cursor-pointer p-0 hover:text-white transition-colors"
                    >
                        <LogOut size={12} aria-hidden="true"/>
                        Se déconnecter
                    </button>
                </div>
            </aside>

            <main className="bg-[var(--clr-off-white)] p-9 md:p-14">
                {children}
            </main>
        </div>
    );
}