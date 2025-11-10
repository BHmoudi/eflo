-- Migration V14: Insert Complete VN Workflow with All Task Types and Approvals
-- This migration creates the full VN workflow matching the reference system

-- First, update the task_type constraint to allow new types
ALTER TABLE workflow_state_tasks DROP CONSTRAINT IF EXISTS check_task_type;
ALTER TABLE workflow_state_tasks ADD CONSTRAINT check_task_type CHECK (task_type IN (
    'DATA_INPUT', 'DOCUMENT_UPLOAD', 'APPROVAL', 'VALIDATION',
    'NOTIFICATION', 'INTEGRATION', 'MANUAL', 'AUTOMATED',
    'DOCUMENT', 'ACTION', 'CONTROL'
));

-- Rename old VN_STANDARD to VN_STANDARD_OLD (preserve existing data)
UPDATE workflow_processes SET process_code = 'VN_STANDARD_OLD', is_active = false
WHERE process_code = 'VN_STANDARD';

-- ============================================
-- CREATE NEW VN WORKFLOW PROCESS
-- ============================================

INSERT INTO workflow_processes (process_code, process_name, description, order_type, max_duration_days, auto_progress_enabled, configuration)
VALUES
    ('VN_STANDARD', 'Workflow Standard Véhicule Neuf', 'Workflow complet pour les commandes de véhicules neufs basé sur le système de référence', 'VN', 120, true,
    '{"notifications": true, "auto_escalation": true, "document_validation": true, "sla_tracking": true}'::jsonb);

-- ============================================
-- CREATE WORKFLOW STATES (PHASES)
-- ============================================

INSERT INTO workflow_states (process_id, state_code, state_name, description, state_order, state_type, expected_duration_hours, is_final_state, requires_approval)
SELECT
    p.id,
    s.state_code,
    s.state_name,
    s.description,
    s.state_order,
    s.state_type,
    s.expected_duration_hours,
    s.is_final_state,
    s.requires_approval
FROM workflow_processes p
CROSS JOIN (VALUES
    -- Phase 1: Commande
    ('COMMANDE', 'Commande', 'Phase de création et validation de la commande', 1, 'START', 72, false, false),

    -- Phase 2: Validation
    ('VALIDATION', 'Validation', 'Phase de validation hiérarchique', 2, 'NORMAL', 96, false, true),

    -- Phase 3: Traitement Appro
    ('TRAITEMENT_APPRO', 'Traitement Appro', 'Phase de traitement approvisionnement', 3, 'NORMAL', 120, false, false),

    -- Phase 4: Traitement de la commande
    ('TRAITEMENT_COMMANDE', 'Traitement de la commande', 'Phase de traitement de la commande', 4, 'NORMAL', 168, false, false),

    -- Phase 5: Saisie DCS
    ('SAISIE_DCS', 'Saisie DCS', 'Phase de saisie DCS', 5, 'NORMAL', 48, false, false),

    -- Phase 6: Préparation
    ('PREPARATION', 'Préparation', 'Phase de préparation du véhicule', 6, 'NORMAL', 72, false, false),

    -- Phase 7: Livraison
    ('LIVRAISON', 'Livraison', 'Phase de livraison au client', 7, 'NORMAL', 48, false, false),

    -- Phase 8: Archivée
    ('ARCHIVEE', 'Archivée', 'Dossier archivé', 8, 'FINAL', 0, true, false),

    -- Special state
    ('CANCELLED', 'Annulé', 'Commande annulée', 9, 'FINAL', 0, true, false)
) AS s(state_code, state_name, description, state_order, state_type, expected_duration_hours, is_final_state, requires_approval)
WHERE p.process_code = 'VN_STANDARD';

-- ============================================
-- CREATE WORKFLOW STATE TASKS
-- ============================================

-- Get IDs for reuse
DO $$
DECLARE
    v_process_id BIGINT;
    v_commercial_role_id BIGINT;
    v_cdv_role_id BIGINT;
    v_cdr_role_id BIGINT;
    v_appro_role_id BIGINT;
    v_admin_role_id BIGINT;
    v_preparation_role_id BIGINT;
    v_livraison_role_id BIGINT;
    v_externe_role_id BIGINT;
    v_doc_type_id BIGINT;
    v_action_type_id BIGINT;
    v_control_type_id BIGINT;
    v_cdv_chain_id BIGINT;
    v_cdv_cdr_chain_id BIGINT;
BEGIN
    -- Get process ID
    SELECT id INTO v_process_id FROM workflow_processes WHERE process_code = 'VN_STANDARD';

    -- Get role IDs
    SELECT id INTO v_commercial_role_id FROM workflow_roles WHERE role_code = 'COMMERCIAL';
    SELECT id INTO v_cdv_role_id FROM workflow_roles WHERE role_code = 'CDV';
    SELECT id INTO v_cdr_role_id FROM workflow_roles WHERE role_code = 'CDR';
    SELECT id INTO v_appro_role_id FROM workflow_roles WHERE role_code = 'APPRO';
    SELECT id INTO v_admin_role_id FROM workflow_roles WHERE role_code = 'ADMIN';
    SELECT id INTO v_preparation_role_id FROM workflow_roles WHERE role_code = 'PREPARATION';
    SELECT id INTO v_livraison_role_id FROM workflow_roles WHERE role_code = 'LIVRAISON';
    SELECT id INTO v_externe_role_id FROM workflow_roles WHERE role_code = 'EXTERNE';

    -- Get task type IDs
    SELECT id INTO v_doc_type_id FROM workflow_task_types WHERE type_code = 'DOCUMENT';
    SELECT id INTO v_action_type_id FROM workflow_task_types WHERE type_code = 'ACTION';
    SELECT id INTO v_control_type_id FROM workflow_task_types WHERE type_code = 'CONTROL';

    -- Get approval chain IDs
    SELECT id INTO v_cdv_chain_id FROM workflow_approval_chains WHERE chain_code = 'CDV_APPROVAL';
    SELECT id INTO v_cdv_cdr_chain_id FROM workflow_approval_chains WHERE chain_code = 'CDV_CDR_CHAIN';

    -- ============================================
    -- PHASE 1: COMMANDE - Tasks
    -- ============================================

    -- Documents
    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, escalation_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'DOC_SIGNATURE_ELECTRONIQUE',
        'Signature électronique',
        'DOCUMENT',
        'Document de signature électronique du client',
        true,
        1,
        false,
        v_commercial_role_id,
        v_doc_type_id,
        24,
        12,
        '["PDF", "DOCX", "IMAGE"]'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'DOC_ER_CALCULE',
        'ER calculé',
        'DOCUMENT',
        'Document ER calculé',
        true,
        2,
        false,
        v_commercial_role_id,
        v_doc_type_id,
        24,
        '["PDF", "EXCEL"]'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'DOC_ER_RETENU',
        'ER retenu',
        'DOCUMENT',
        'Document ER retenu',
        true,
        3,
        false,
        v_commercial_role_id,
        v_doc_type_id,
        24,
        '["PDF", "EXCEL"]'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'DOC_FEUILLE_GESTION',
        'Feuille Gestion',
        'DOCUMENT',
        'Feuille de gestion',
        true,
        4,
        false,
        v_externe_role_id,
        v_doc_type_id,
        48,
        '["PDF", "EXCEL"]'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'DOC_BON_COMMANDE',
        'Bon commande',
        'DOCUMENT',
        'Bon de commande',
        true,
        5,
        false,
        v_commercial_role_id,
        v_doc_type_id,
        24,
        '["PDF"]'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'DOC_KBIS_3_MOIS',
        'KBIS_3_mois',
        'DOCUMENT',
        'KBIS de moins de 3 mois (pour les sociétés)',
        false,
        6,
        false,
        v_commercial_role_id,
        v_doc_type_id,
        48,
        '["PDF"]'::jsonb;

    -- Actions
    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, input_fields, validation_rules)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'ACTION_SAISIE_LIEU_LIVRAISON',
        'Saisie lieu de livraison',
        'ACTION',
        'Saisir le lieu de livraison du véhicule',
        true,
        7,
        false,
        v_commercial_role_id,
        v_action_type_id,
        12,
        '[
            {"name": "adresse", "type": "text", "label": "Adresse", "required": true, "maxLength": 255},
            {"name": "codePostal", "type": "text", "label": "Code Postal", "required": true, "pattern": "^[0-9]{5}$"},
            {"name": "ville", "type": "text", "label": "Ville", "required": true},
            {"name": "pays", "type": "select", "label": "Pays", "required": true, "options": ["France", "Belgique", "Luxembourg", "Suisse"]}
        ]'::jsonb,
        '{
            "codePostal": {"pattern": "^[0-9]{5}$", "message": "Code postal invalide"}
        }'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, input_fields, validation_rules)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'ACTION_SAISIE_MONTANT_ER_CALCULE',
        'Saisie montant ER calculé',
        'ACTION',
        'Saisir le montant ER calculé',
        true,
        8,
        false,
        v_commercial_role_id,
        v_action_type_id,
        12,
        '[
            {"name": "montantER", "type": "number", "label": "Montant ER (€)", "required": true, "min": 0, "step": 0.01}
        ]'::jsonb,
        '{
            "montantER": {"min": 0, "message": "Le montant doit être positif"}
        }'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, input_fields, validation_rules)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'ACTION_SAISIE_MONTANT_ER_RETENU',
        'Saisie montant ER retenu',
        'ACTION',
        'Saisir le montant ER retenu',
        true,
        9,
        false,
        v_commercial_role_id,
        v_action_type_id,
        12,
        '[
            {"name": "montantER", "type": "number", "label": "Montant ER retenu (€)", "required": true, "min": 0, "step": 0.01}
        ]'::jsonb,
        '{
            "montantER": {"min": 0, "message": "Le montant doit être positif"}
        }'::jsonb;

    -- Controls
    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'CONTROL_IMPORT_DOCUMENTS',
        'Import des documents obligatoire',
        'CONTROL',
        'Vérifier que tous les documents obligatoires sont présents',
        true,
        10,
        false,
        v_cdv_role_id,
        v_control_type_id,
        24;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'CONTROL_SIGNATURE_ELECTRONIQUE',
        'Signature électronique',
        'CONTROL',
        'Contrôle de la signature électronique',
        true,
        11,
        false,
        v_cdv_role_id,
        v_control_type_id,
        24;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'CONTROL_REMETTRE_CONTROLE',
        'Remettre au contrôle/Anticiper la remise au contrôle',
        'CONTROL',
        'Contrôle et anticipation de la remise',
        true,
        12,
        false,
        v_cdv_role_id,
        v_control_type_id,
        24;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE'),
        'CONTROL_VALIDATION_DOSSIER',
        'Validation du dossier pour envoie au traitement',
        'CONTROL',
        'Validation finale du dossier avant envoi au traitement',
        true,
        13,
        true,
        v_cdv_role_id,
        v_control_type_id,
        48;

    -- Link approval chain to validation control
    INSERT INTO workflow_task_approval_chains (task_id, chain_id, is_mandatory)
    SELECT
        (SELECT id FROM workflow_state_tasks WHERE task_code = 'CONTROL_VALIDATION_DOSSIER' AND state_id = (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'COMMANDE')),
        v_cdv_chain_id,
        true;

    -- ============================================
    -- PHASE 2: VALIDATION - Tasks
    -- ============================================

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'VALIDATION'),
        'APPROVAL_CDV_CDR',
        'Validation CDV puis CDR',
        'CONTROL',
        'Validation hiérarchique: CDV puis CDR',
        true,
        1,
        true,
        v_cdv_role_id,
        v_control_type_id,
        96;

    -- Link CDV-CDR approval chain
    INSERT INTO workflow_task_approval_chains (task_id, chain_id, is_mandatory)
    SELECT
        (SELECT id FROM workflow_state_tasks WHERE task_code = 'APPROVAL_CDV_CDR' AND state_id = (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'VALIDATION')),
        v_cdv_cdr_chain_id,
        true;

    -- ============================================
    -- PHASE 3: TRAITEMENT APPRO - Tasks
    -- ============================================

    -- Documents
    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'TRAITEMENT_APPRO'),
        'DOC_COPIE_ECRAN_RAPPROCHANT',
        'Copie Ecran rapprochant',
        'DOCUMENT',
        'Copie écran du rapprochement',
        true,
        1,
        false,
        v_appro_role_id,
        v_doc_type_id,
        48,
        '["PDF", "IMAGE"]'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'TRAITEMENT_APPRO'),
        'DOC_COPIE_ECRAN_CAR',
        'Copie Ecran Creation de CAR',
        'DOCUMENT',
        'Copie écran de création du CAR',
        true,
        2,
        false,
        v_appro_role_id,
        v_doc_type_id,
        48,
        '["PDF", "IMAGE"]'::jsonb;

    -- Controls
    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'TRAITEMENT_APPRO'),
        'CONTROL_CONFIRMATION_CAR',
        'Confirmation de traitement de la commande par l''ajout du CAR',
        'CONTROL',
        'Confirmer que le CAR a été ajouté',
        true,
        3,
        false,
        v_admin_role_id,
        v_control_type_id,
        24;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'TRAITEMENT_APPRO'),
        'CONTROL_COMPLETUDE_ELEMENTS',
        'Complétude des éléments pour les dossier incomplets',
        'CONTROL',
        'Vérifier et compléter les éléments manquants',
        true,
        4,
        false,
        v_admin_role_id,
        v_control_type_id,
        48;

    -- Actions
    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, input_fields)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'TRAITEMENT_APPRO'),
        'ACTION_SAISIE_DELAI_APPRO',
        'Saisie de l''info délai par appro',
        'ACTION',
        'Saisir les informations de délai d''approvisionnement',
        true,
        5,
        false,
        v_appro_role_id,
        v_action_type_id,
        24,
        '[
            {"name": "delaiLivraison", "type": "number", "label": "Délai de livraison (jours)", "required": true, "min": 0},
            {"name": "dateEstimee", "type": "date", "label": "Date estimée de livraison", "required": true},
            {"name": "commentaire", "type": "textarea", "label": "Commentaire", "required": false}
        ]'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, input_fields)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'TRAITEMENT_APPRO'),
        'ACTION_SAISIE_TYPE_RESSOURCE',
        'Saisie du type de ressource',
        'ACTION',
        'Saisir le type de ressource',
        true,
        6,
        false,
        v_appro_role_id,
        v_action_type_id,
        24,
        '[
            {"name": "typeRessource", "type": "select", "label": "Type de ressource", "required": true, "options": ["Stock", "Commande directe", "Transfert"]},
            {"name": "fournisseur", "type": "text", "label": "Fournisseur", "required": false}
        ]'::jsonb;

    -- ============================================
    -- PHASE 4: TRAITEMENT COMMANDE - Tasks
    -- ============================================

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'TRAITEMENT_COMMANDE'),
        'DOC_PVMINE',
        'Pvmine',
        'DOCUMENT',
        'Document Pvmine',
        false,
        1,
        false,
        v_admin_role_id,
        v_doc_type_id,
        72,
        '["PDF"]'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'TRAITEMENT_COMMANDE'),
        'DOC_FACTURE_ACHAT_VN',
        'Facture achat VN',
        'DOCUMENT',
        'Facture d''achat du véhicule neuf',
        true,
        2,
        false,
        v_admin_role_id,
        v_doc_type_id,
        48,
        '["PDF"]'::jsonb;

    -- ============================================
    -- PHASE 5: SAISIE DCS - Tasks
    -- ============================================

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'SAISIE_DCS'),
        'CONTROL_SAISIE_DCS',
        'Saisie DCS : Contremarque DCSnet "oui" quand c''est réalisé',
        'CONTROL',
        'Contrôler la saisie DCS dans DCSnet',
        true,
        1,
        false,
        v_admin_role_id,
        v_control_type_id,
        48;

    -- ============================================
    -- PHASE 6: PREPARATION - Tasks
    -- ============================================

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'PREPARATION'),
        'ACTION_PREPARATION_VEHICULE',
        'Préparation du véhicule',
        'ACTION',
        'Effectuer la préparation complète du véhicule',
        true,
        1,
        false,
        v_preparation_role_id,
        v_action_type_id,
        48;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'PREPARATION'),
        'CONTROL_QUALITE_VEHICULE',
        'Contrôle qualité',
        'CONTROL',
        'Vérification qualité du véhicule après préparation',
        true,
        2,
        false,
        v_preparation_role_id,
        v_control_type_id,
        24;

    -- ============================================
    -- PHASE 7: LIVRAISON - Tasks
    -- ============================================

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, input_fields)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'LIVRAISON'),
        'ACTION_PLANIFIER_LIVRAISON',
        'Planifier rendez-vous livraison',
        'ACTION',
        'Fixer un rendez-vous de livraison avec le client',
        true,
        1,
        false,
        v_livraison_role_id,
        v_action_type_id,
        24,
        '[
            {"name": "dateLivraison", "type": "datetime", "label": "Date et heure de livraison", "required": true},
            {"name": "lieuLivraison", "type": "text", "label": "Lieu de livraison", "required": true},
            {"name": "contactClient", "type": "text", "label": "Contact client", "required": true}
        ]'::jsonb;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'LIVRAISON'),
        'ACTION_PREPARER_DOCUMENTS_LIVRAISON',
        'Préparer documents de livraison',
        'ACTION',
        'Préparer tous les documents nécessaires pour la livraison',
        true,
        2,
        false,
        v_livraison_role_id,
        v_action_type_id,
        24;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'LIVRAISON'),
        'ACTION_REMISE_CLIENT',
        'Remise client',
        'ACTION',
        'Remise des clés et documents au client',
        true,
        3,
        false,
        v_livraison_role_id,
        v_action_type_id,
        2;

    INSERT INTO workflow_state_tasks (state_id, task_code, task_name, task_type, description, is_mandatory, task_order, requires_approval, assigned_role_id, task_type_id, sla_hours, required_documents)
    SELECT
        (SELECT id FROM workflow_states WHERE process_id = v_process_id AND state_code = 'LIVRAISON'),
        'CONTROL_CONFIRMATION_LIVRAISON',
        'Confirmation de livraison',
        'CONTROL',
        'Obtenir la confirmation de livraison signée du client',
        true,
        4,
        false,
        v_livraison_role_id,
        v_control_type_id,
        2,
        '["PDF", "IMAGE"]'::jsonb;

END $$;

-- ============================================
-- CREATE WORKFLOW TRANSITIONS
-- ============================================

INSERT INTO workflow_transitions (process_id, from_state_id, to_state_id, transition_name, transition_type, requires_approval, auto_transition)
SELECT
    p.id,
    s1.id,
    s2.id,
    t.transition_name,
    t.transition_type,
    t.requires_approval,
    t.auto_transition
FROM workflow_processes p
JOIN workflow_states s1 ON s1.process_id = p.id
JOIN workflow_states s2 ON s2.process_id = p.id
CROSS JOIN (VALUES
    -- Normal flow
    ('COMMANDE', 'VALIDATION', 'Soumettre pour validation', 'NORMAL', false, false),
    ('VALIDATION', 'TRAITEMENT_APPRO', 'Approuver et envoyer en traitement', 'NORMAL', true, false),
    ('TRAITEMENT_APPRO', 'TRAITEMENT_COMMANDE', 'Continuer traitement commande', 'NORMAL', false, true),
    ('TRAITEMENT_COMMANDE', 'SAISIE_DCS', 'Passer à saisie DCS', 'NORMAL', false, false),
    ('SAISIE_DCS', 'PREPARATION', 'Passer en préparation', 'NORMAL', false, true),
    ('PREPARATION', 'LIVRAISON', 'Planifier livraison', 'NORMAL', false, false),
    ('LIVRAISON', 'ARCHIVEE', 'Archiver le dossier', 'NORMAL', false, true),

    -- Rollbacks
    ('VALIDATION', 'COMMANDE', 'Rejeter - retour à commande', 'ROLLBACK', false, false),
    ('TRAITEMENT_APPRO', 'VALIDATION', 'Retour à validation', 'ROLLBACK', false, false),

    -- Cancellations
    ('COMMANDE', 'CANCELLED', 'Annuler commande', 'CANCEL', false, false),
    ('VALIDATION', 'CANCELLED', 'Annuler commande', 'CANCEL', true, false),
    ('TRAITEMENT_APPRO', 'CANCELLED', 'Annuler commande', 'CANCEL', true, false),
    ('TRAITEMENT_COMMANDE', 'CANCELLED', 'Annuler commande', 'CANCEL', true, false)
) AS t(from_state_code, to_state_code, transition_name, transition_type, requires_approval, auto_transition)
WHERE p.process_code = 'VN_STANDARD'
  AND s1.state_code = t.from_state_code
  AND s2.state_code = t.to_state_code;

-- Comments
COMMENT ON TABLE workflow_processes IS 'Complete VN workflow with all task types, roles, and approvals configured';
