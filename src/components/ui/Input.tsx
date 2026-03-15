// ============================================================
// FICHIER  : src/components/ui/Input.tsx
// RÔLE     : Champ de saisie réutilisable avec label, icône
//            gauche, élément droit (ex : toggle mot de passe)
//            et affichage des erreurs de validation.
//            Compatible react-hook-form grâce à forwardRef.
// ============================================================

import React, { InputHTMLAttributes, forwardRef } from 'react';

// ── Props du composant ──────────────────────────────────────
interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  /** Label affiché au-dessus du champ */
  label: string;
  /** Message d'erreur de validation (optionnel) */
  error?: string;
  /** Icône SVG affichée à gauche dans le champ */
  icon?: React.ReactNode;
  /** Élément affiché à droite (ex : bouton afficher/masquer mdp) */
  rightElement?: React.ReactNode;
}

/**
 * Champ de saisie universel du design system GBE-MINFI.
 *
 * Usage :
 * ```tsx
 * <Input
 *   name="email"
 *   label="Adresse email"
 *   type="email"
 *   placeholder="vous@exemple.cm"
 *   icon={<MailIcon />}
 *   error={errors.email}
 * />
 * ```
 */
const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, icon, rightElement, className = '', id, name, ...rest }, ref) => {

    // Utiliser `id` s'il est fourni, sinon `name` pour le lien label ↔ input
    const fieldId = id ?? name;

    return (
      <div className="inp-group">

        {/* ── Label ── */}
        <label className="inp-label" htmlFor={fieldId}>
          {label}
        </label>

        {/* ── Wrapper relatif pour positionner les icônes ── */}
        <div className="inp-wrapper">

          {/* Icône gauche (optionnelle) */}
          {icon && (
            <span className="inp-icon-left" aria-hidden="true">
              {icon}
            </span>
          )}

          {/* Champ de saisie */}
          <input
            ref={ref}
            id={fieldId}
            name={name}
            className={[
              'inp-field',
              icon         ? 'inp-field--with-icon'  : '',
              rightElement ? 'inp-field--with-right'  : '',
              error        ? 'inp-field--error'        : '',
              className,
            ].filter(Boolean).join(' ')}
            aria-describedby={error ? `${fieldId}-error` : undefined}
            aria-invalid={error ? true : undefined}
            {...rest}
          />

          {/* Élément droit (ex : toggle visibilité mot de passe) */}
          {rightElement && (
            <span className="inp-icon-right">
              {rightElement}
            </span>
          )}

        </div>

        {/* ── Message d'erreur ── */}
        {error && (
          <p
            id={`${fieldId}-error`}
            className="inp-error-msg"
            role="alert"
          >
            {/* Icône d'avertissement inline */}
            <svg
              width="12" height="12"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.5"
              aria-hidden="true"
            >
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8"  x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            {error}
          </p>
        )}

      </div>
    );
  }
);

Input.displayName = 'Input';

// ✅ Export par défaut — s'importe SANS accolades :
// import Input from '@/components/ui/Input'
export default Input;