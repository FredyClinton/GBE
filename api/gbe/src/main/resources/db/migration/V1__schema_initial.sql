-- ============================================================
-- GBE — V1 : schéma initial
-- Généré à partir du schéma produit par Hibernate (pg_dump --schema-only).
-- Toute évolution ultérieure du modèle JPA doit faire l'objet d'une
-- nouvelle migration V<n>__description.sql (ddl-auto est en 'validate').
-- ============================================================

--
--

--
-- Name: actions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE actions (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    code character varying(255),
    code_chap character varying(255),
    code_exercice character varying(255),
    code_programme character varying(255),
    code_section character varying(255),
    libelle_en text,
    libelle text,
    numero character varying(255),
    numero_programme character varying(255),
    exercice_id character varying(255),
    programme_id character varying(255) NOT NULL,
    section_id character varying(255)
);

--
-- Name: agent; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE agent (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    actif boolean NOT NULL,
    cni_expiry_date date NOT NULL,
    cni_issue_date date NOT NULL,
    date_of_birth date,
    first_name character varying(50),
    last_name character varying(50),
    matricule character varying(255) NOT NULL,
    nui character varying(255) NOT NULL,
    numero_cni character varying(255) NOT NULL,
    phone_number character varying(255) NOT NULL
);

--
-- Name: chapitres; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE chapitres (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    code_chap character varying(3),
    code character varying(255) NOT NULL,
    code_exercice character varying(255),
    code_section character varying(255),
    libelle_en text,
    libelle text NOT NULL,
    exercice_id character varying(255),
    section_id character varying(255)
);

--
-- Name: classe_fonctionnelle; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE classe_fonctionnelle (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    code_classe character varying(1) NOT NULL,
    code_complet character varying(4) NOT NULL,
    libelle_en character varying(255) NOT NULL,
    libelle_fr character varying(255) NOT NULL
);

--
-- Name: credit_budgetaire; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE credit_budgetaire (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    code_imputation character varying(50) NOT NULL,
    montant_ae numeric(20,2) NOT NULL,
    montant_ae_consomme numeric(20,2) NOT NULL,
    montant_ae_disponible numeric(20,2) NOT NULL,
    montant_cp numeric(20,2) NOT NULL,
    montant_cp_consomme numeric(20,2) NOT NULL,
    montant_cp_disponible numeric(20,2) NOT NULL,
    statut character varying(255) NOT NULL,
    action_id character varying(255) NOT NULL,
    chapitre_id character varying(255) NOT NULL,
    credit_parent_id character varying(255),
    exercice_id character varying(255) NOT NULL,
    programme_id character varying(255) NOT NULL,
    section_id character varying(255) NOT NULL,
    CONSTRAINT credit_budgetaire_statut_check CHECK (((statut)::text = ANY ((ARRAY['CANTONNE'::character varying, 'DISPONIBLE'::character varying, 'ENGAGE'::character varying, 'SUSPENDU'::character varying, 'ANNULE'::character varying, 'SOLDE'::character varying])::text[])))
);

--
-- Name: exercice; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE exercice (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    actif boolean,
    annee integer NOT NULL,
    code_exercice integer NOT NULL,
    libelle_en character varying(255),
    libelle_fr character varying(255)
);

--
-- Name: mandat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE mandat (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    actif boolean NOT NULL,
    start_mandat date,
    end_mandat date,
    numero_decision character varying(100),
    role_systeme character varying(255) NOT NULL,
    programme_id character varying(255),
    section_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL,
    CONSTRAINT mandat_role_systeme_check CHECK (((role_systeme)::text = ANY ((ARRAY['ADMIN'::character varying, 'ORDONNATEUR'::character varying, 'MINISTRE'::character varying, 'CONTROLEUR_FINANCIER'::character varying, 'COMPTABLE'::character varying, 'GESTIONNAIRE'::character varying, 'AGENT'::character varying])::text[])))
);

--
-- Name: mandat_permissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE mandat_permissions (
    mandat_id character varying(255) NOT NULL,
    permission character varying(255),
    CONSTRAINT mandat_permissions_permission_check CHECK (((permission)::text = ANY ((ARRAY['ENGAGE_DEPENSE'::character varying, 'REVISER_AE'::character varying, 'REVISER_CP'::character varying, 'REJETER_DEPENSE'::character varying, 'VISA_CFI'::character varying, 'REJETER_CFI'::character varying, 'LIQUIDER_DEPENSE'::character varying, 'PAYER_DEPENSE'::character varying, 'INSCRIRE_CREDIT'::character varying, 'MANAGE_USERS'::character varying, 'MANAGE_AFFECTATIONS'::character varying, 'CONSULTER'::character varying])::text[])))
);

--
-- Name: natures_economiques; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE natures_economiques (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    code character varying(255),
    code_article character varying(255),
    code_exercice character varying(255),
    code_paragraphe character varying(255),
    code_rubrique character varying(255),
    code_titre character varying(255),
    libelle_en text,
    libelle text NOT NULL,
    exercice_id character varying(255)
);

--
-- Name: programmes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE programmes (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    code character varying(255),
    code_exercice character varying(255),
    code_section character varying(255),
    libelle_en text,
    libelle text,
    numero character varying(255),
    exercice_id character varying(255),
    section_id character varying(255)
);

--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE roles (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    name character varying(255) NOT NULL
);

--
-- Name: sections; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE sections (
    id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date date NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by character varying(255),
    code_exercice character varying(255),
    code character varying(255),
    libelle_en text,
    libelle text,
    sigle character varying(255),
    exercice_id character varying(255)
);

--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE users (
    id character varying(255) NOT NULL,
    cni_expiry_date date,
    cni_issue_date date,
    created_date date NOT NULL,
    credentials_expired boolean,
    date_of_birth date,
    email character varying(255) NOT NULL,
    is_email_verified boolean,
    is_enabled boolean,
    first_login boolean,
    first_name character varying(255) NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_name character varying(255) NOT NULL,
    is_account_locked boolean,
    matricule character varying(255),
    mfa_enabled boolean,
    nui character varying(255),
    numero_cni character varying(255),
    password character varying(255) NOT NULL,
    phone_number character varying(255) NOT NULL,
    phone_verified boolean,
    mfa_secret character varying(255),
    agent_id character varying(255),
    role_id character varying(255)
);

--
-- Name: actions actions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY actions
    ADD CONSTRAINT actions_pkey PRIMARY KEY (id);

--
-- Name: agent agent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY agent
    ADD CONSTRAINT agent_pkey PRIMARY KEY (id);

--
-- Name: chapitres chapitres_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY chapitres
    ADD CONSTRAINT chapitres_pkey PRIMARY KEY (id);

--
-- Name: classe_fonctionnelle classe_fonctionnelle_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY classe_fonctionnelle
    ADD CONSTRAINT classe_fonctionnelle_pkey PRIMARY KEY (id);

--
-- Name: credit_budgetaire credit_budgetaire_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY credit_budgetaire
    ADD CONSTRAINT credit_budgetaire_pkey PRIMARY KEY (id);

--
-- Name: exercice exercice_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY exercice
    ADD CONSTRAINT exercice_pkey PRIMARY KEY (id);

--
-- Name: mandat mandat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mandat
    ADD CONSTRAINT mandat_pkey PRIMARY KEY (id);

--
-- Name: natures_economiques natures_economiques_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY natures_economiques
    ADD CONSTRAINT natures_economiques_pkey PRIMARY KEY (id);

--
-- Name: programmes programmes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY programmes
    ADD CONSTRAINT programmes_pkey PRIMARY KEY (id);

--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);

--
-- Name: sections sections_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sections
    ADD CONSTRAINT sections_pkey PRIMARY KEY (id);

--
-- Name: classe_fonctionnelle uk4l4x1uepwi87xviyx7p3tg3bk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY classe_fonctionnelle
    ADD CONSTRAINT uk4l4x1uepwi87xviyx7p3tg3bk UNIQUE (code_complet);

--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

--
-- Name: users uk9q63snka3mdh91as4io72espi; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT uk9q63snka3mdh91as4io72espi UNIQUE (phone_number);

--
-- Name: agent uk_agent_cni; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY agent
    ADD CONSTRAINT uk_agent_cni UNIQUE (numero_cni);

--
-- Name: agent uk_agent_matricule; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY agent
    ADD CONSTRAINT uk_agent_matricule UNIQUE (matricule);

--
-- Name: agent uk_agent_nui; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY agent
    ADD CONSTRAINT uk_agent_nui UNIQUE (nui);

--
-- Name: agent uk_agent_phone; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY agent
    ADD CONSTRAINT uk_agent_phone UNIQUE (phone_number);

--
-- Name: mandat uk_mandat_user_programme; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mandat
    ADD CONSTRAINT uk_mandat_user_programme UNIQUE (user_id, programme_id);

--
-- Name: users ukbxbply1soavp0hufomkf2lye0; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT ukbxbply1soavp0hufomkf2lye0 UNIQUE (agent_id);

--
-- Name: exercice ukesqsm29lwke8ycpmwxrr696un; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY exercice
    ADD CONSTRAINT ukesqsm29lwke8ycpmwxrr696un UNIQUE (annee);

--
-- Name: credit_budgetaire ukn75yo9wu4i0cofrwtpriwjuoj; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY credit_budgetaire
    ADD CONSTRAINT ukn75yo9wu4i0cofrwtpriwjuoj UNIQUE (code_imputation);

--
-- Name: roles ukofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY roles
    ADD CONSTRAINT ukofx66keruapi6vyqpv6f2or37 UNIQUE (name);

--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

--
-- Name: credit_budgetaire fk34nybbqiu98dyef3ifstr1k0h; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY credit_budgetaire
    ADD CONSTRAINT fk34nybbqiu98dyef3ifstr1k0h FOREIGN KEY (section_id) REFERENCES sections(id);

--
-- Name: mandat_permissions fk3k76mvnx0wueple9i721dmsdu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mandat_permissions
    ADD CONSTRAINT fk3k76mvnx0wueple9i721dmsdu FOREIGN KEY (mandat_id) REFERENCES mandat(id);

--
-- Name: users fk5hm8s13rfhcqh5aioatkapyfx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fk5hm8s13rfhcqh5aioatkapyfx FOREIGN KEY (agent_id) REFERENCES agent(id);

--
-- Name: mandat fk7myl9ueacb4i9ck8p88t84d36; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mandat
    ADD CONSTRAINT fk7myl9ueacb4i9ck8p88t84d36 FOREIGN KEY (user_id) REFERENCES users(id);

--
-- Name: actions fk_action_exercice; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY actions
    ADD CONSTRAINT fk_action_exercice FOREIGN KEY (exercice_id) REFERENCES exercice(id);

--
-- Name: actions fk_action_programme; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY actions
    ADD CONSTRAINT fk_action_programme FOREIGN KEY (programme_id) REFERENCES programmes(id);

--
-- Name: actions fk_action_section; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY actions
    ADD CONSTRAINT fk_action_section FOREIGN KEY (section_id) REFERENCES sections(id);

--
-- Name: chapitres fk_chapitre_exercice; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY chapitres
    ADD CONSTRAINT fk_chapitre_exercice FOREIGN KEY (exercice_id) REFERENCES exercice(id);

--
-- Name: chapitres fk_chapitre_section; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY chapitres
    ADD CONSTRAINT fk_chapitre_section FOREIGN KEY (section_id) REFERENCES sections(id);

--
-- Name: natures_economiques fk_nature_eco_exercice; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY natures_economiques
    ADD CONSTRAINT fk_nature_eco_exercice FOREIGN KEY (exercice_id) REFERENCES exercice(id);

--
-- Name: programmes fk_programme_exercice; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY programmes
    ADD CONSTRAINT fk_programme_exercice FOREIGN KEY (exercice_id) REFERENCES exercice(id);

--
-- Name: programmes fk_programme_section; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY programmes
    ADD CONSTRAINT fk_programme_section FOREIGN KEY (section_id) REFERENCES sections(id);

--
-- Name: sections fk_section_exercice; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sections
    ADD CONSTRAINT fk_section_exercice FOREIGN KEY (exercice_id) REFERENCES exercice(id);

--
-- Name: credit_budgetaire fkco2b3vp5slawngi7bawonv2ag; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY credit_budgetaire
    ADD CONSTRAINT fkco2b3vp5slawngi7bawonv2ag FOREIGN KEY (action_id) REFERENCES actions(id);

--
-- Name: credit_budgetaire fkghg05pr6hqsmvcukf0ta4s4yh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY credit_budgetaire
    ADD CONSTRAINT fkghg05pr6hqsmvcukf0ta4s4yh FOREIGN KEY (exercice_id) REFERENCES exercice(id);

--
-- Name: mandat fkik2x2129u2cferi6g34nniddv; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mandat
    ADD CONSTRAINT fkik2x2129u2cferi6g34nniddv FOREIGN KEY (programme_id) REFERENCES programmes(id);

--
-- Name: credit_budgetaire fkmyww0vlciuxiw8v9r6q3peho7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY credit_budgetaire
    ADD CONSTRAINT fkmyww0vlciuxiw8v9r6q3peho7 FOREIGN KEY (chapitre_id) REFERENCES chapitres(id);

--
-- Name: users fkp56c1712k691lhsyewcssf40f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fkp56c1712k691lhsyewcssf40f FOREIGN KEY (role_id) REFERENCES roles(id);

--
-- Name: credit_budgetaire fkp9g2hhgpdw3y05qhg5xo46ynl; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY credit_budgetaire
    ADD CONSTRAINT fkp9g2hhgpdw3y05qhg5xo46ynl FOREIGN KEY (programme_id) REFERENCES programmes(id);

--
-- Name: mandat fkrajnxi749333omsivmf0wua7i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY mandat
    ADD CONSTRAINT fkrajnxi749333omsivmf0wua7i FOREIGN KEY (section_id) REFERENCES sections(id);

--
-- Name: credit_budgetaire fkrwr1ww5vrma1efou5oioaevad; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY credit_budgetaire
    ADD CONSTRAINT fkrwr1ww5vrma1efou5oioaevad FOREIGN KEY (credit_parent_id) REFERENCES credit_budgetaire(id);

--
--

