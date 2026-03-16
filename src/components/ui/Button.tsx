// ============================================================
// FICHIER  : src/components/ui/Button.tsx
// RÔLE     : Bouton réutilisable avec 3 variantes visuelles,
//            état de chargement (spinner) et support disabled.
// ============================================================

import React, { ButtonHTMLAttributes } from 'react';

// ── Props du composant ──
interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  /**
   * Variante visuelle :
   * - primary   → bleu marine (action principale)
   * - secondary → vert MINFI (action secondaire / inscription)
   * - ghost     → transparent avec bordure (action tertiaire)
   */
  variant?: 'primary' | 'secondary' | 'ghost';
  /** Affiche un spinner et désactive le bouton pendant un chargement */
  isLoading?: boolean;
  /** Prend toute la largeur du conteneur parent */
  fullWidth?: boolean;
}

/**
 * Bouton du design system GBE-MINFI.
 *
 * Usage :
 * ```tsx
 * <Button variant="primary" isLoading={isSubmitting} fullWidth>
 *   Se connecter
 * </Button>
 * ```
 */
const Button: React.FC<ButtonProps> = ({
  children,
  variant   = 'primary',
  isLoading = false,
  fullWidth = false,
  disabled,
  className = '',
  ...rest
}) => {
  const isDisabled = disabled || isLoading;

  return (
    <button
      className={[
        'btn',
        `btn--${variant}`,
        fullWidth ? 'btn--full' : '',
        className,
      ].join(' ')}
      disabled={isDisabled}
      aria-busy={isLoading}
      {...rest}
    >
      {isLoading ? (
        /* ── État chargement : spinner + texte ── */
        <span className="btn__loader">
          <span className="btn__spinner" aria-hidden="true" />
          <span>Chargement...</span>
        </span>
      ) : (
        /* ── État normal ── */
        children
      )}
    </button>
  );
};

export default Button;