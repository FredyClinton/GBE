-- ============================================================
-- GBE — V24 : boîte de notifications dans l'application (lot 2d, Q20)
--
-- Une notification est écrite dans la même transaction que l'événement métier qui la provoque
-- (acte notifié, acte annulé, décision du CF). Pas d'e-mail ni de SMS pour l'instant ; un canal temps réel
-- (SSE) pourra se brancher plus tard sur les mêmes événements. Absente des diagrammes : écart volontaire.
-- La contrainte CHECK liste TypeNotification : ajouter un type demande une migration.
-- ============================================================

CREATE TABLE IF NOT EXISTS notifications (
    id                 character varying(255) NOT NULL,
    created_by         character varying(255) NOT NULL,
    created_date       date                   NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by   character varying(255),
    destinataire_id    character varying(255) NOT NULL,
    type               character varying(40)  NOT NULL,
    titre              character varying(200) NOT NULL,
    message            text                   NOT NULL,
    objet_type         character varying(40),
    objet_id           character varying(255),
    date_creation      timestamp(0) without time zone NOT NULL,
    date_lecture       timestamp(0) without time zone,
    CONSTRAINT notifications_pkey PRIMARY KEY (id),
    CONSTRAINT notifications_type_check CHECK (type IN ('ACTE_NOTIFIE', 'ACTE_ANNULE', 'DECISION_CF')),
    CONSTRAINT fk_notification_destinataire FOREIGN KEY (destinataire_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_notification_destinataire_date ON notifications (destinataire_id, date_creation DESC);
CREATE INDEX IF NOT EXISTS idx_notification_non_lues ON notifications (destinataire_id) WHERE date_lecture IS NULL;
