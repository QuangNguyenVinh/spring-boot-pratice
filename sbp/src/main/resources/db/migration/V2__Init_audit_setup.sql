-- 1) Extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto; -- for digest() and crypt(), gen_random_bytes(), etc.

-- 2) Shared helper: set updated_at automatically
CREATE OR REPLACE FUNCTION app_set_updated_at()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

-- 3) Shared helper: fetch "who did it" from session settings (if present)
CREATE OR REPLACE FUNCTION app_setting_text(key text)
    RETURNS text
    LANGUAGE sql
AS
$$
SELECT current_setting(key, true);
$$;

CREATE OR REPLACE FUNCTION app_setting_uuid(key text)
    RETURNS uuid
    LANGUAGE sql
AS
$$
SELECT NULLIF(current_setting(key, true), '')::uuid;
$$;

-- 4) Audit tables (event + row snapshots)
CREATE TABLE IF NOT EXISTS audit_event
(
    id             uuid PRIMARY KEY     DEFAULT uuidv7(),
    occurred_at    timestamptz NOT NULL DEFAULT now(),

    -- Who
    actor_user_id  uuid        NULL,
    actor_username text        NULL,

    -- Correlation / context
    request_id     uuid        NULL,
    ip             inet        NULL,
    user_agent     text        NULL,

    -- What
    action         text        NOT NULL, -- e.g. LOGIN, LOGOUT, REFRESH, ADMIN_CHANGE
    target_type    text        NULL,     -- e.g. app_user, role
    target_id      uuid        NULL,
    details        jsonb       NULL      -- flexible payload
);

CREATE INDEX IF NOT EXISTS idx_audit_event_occurred_at ON audit_event (occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_event_actor_user_id ON audit_event (actor_user_id);
CREATE INDEX IF NOT EXISTS idx_audit_event_request_id ON audit_event (request_id);

CREATE TABLE IF NOT EXISTS audit_row
(
    id             uuid PRIMARY KEY     DEFAULT uuidv7(),
    occurred_at    timestamptz NOT NULL DEFAULT now(),

    -- Who / correlation (copied from session)
    actor_user_id  uuid        NULL,
    actor_username text        NULL,
    request_id     uuid        NULL,
    ip             inet        NULL,
    user_agent     text        NULL,

    -- What row changed
    table_name     text        NOT NULL,
    operation      text        NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    row_pk         uuid        NULL, -- assumes UUID PK; ok for your auth tables
    old_data       jsonb       NULL,
    new_data       jsonb       NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_row_occurred_at ON audit_row (occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_row_table_name ON audit_row (table_name);
CREATE INDEX IF NOT EXISTS idx_audit_row_row_pk ON audit_row (row_pk);
CREATE INDEX IF NOT EXISTS idx_audit_row_request_id ON audit_row (request_id);

-- 5) Generic row-audit trigger function
--    Notes:
--    - expects PK column named "id" (uuid) on audited tables
--    - stores old/new snapshots as jsonb
CREATE OR REPLACE FUNCTION app_audit_row()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_actor_user_id  uuid;
    v_actor_username text;
    v_request_id     uuid;
    v_ip             inet;
    v_user_agent     text;
    v_row_pk         uuid;
BEGIN
    v_actor_user_id := app_setting_uuid('app.user_id');
    v_actor_username := app_setting_text('app.username');
    v_request_id := app_setting_uuid('app.request_id');
    v_ip := NULLIF(app_setting_text('app.ip'), '')::inet;
    v_user_agent := app_setting_text('app.user_agent');

    IF (TG_OP = 'INSERT') THEN
        v_row_pk := NEW.id;
        INSERT INTO audit_row(actor_user_id, actor_username, request_id, ip, user_agent,
                              table_name, operation, row_pk, old_data, new_data)
        VALUES (v_actor_user_id, v_actor_username, v_request_id, v_ip, v_user_agent,
                TG_TABLE_NAME, TG_OP, v_row_pk, NULL, to_jsonb(NEW));
        RETURN NEW;

    ELSIF (TG_OP = 'UPDATE') THEN
        v_row_pk := NEW.id;
        INSERT INTO audit_row(actor_user_id, actor_username, request_id, ip, user_agent,
                              table_name, operation, row_pk, old_data, new_data)
        VALUES (v_actor_user_id, v_actor_username, v_request_id, v_ip, v_user_agent,
                TG_TABLE_NAME, TG_OP, v_row_pk, to_jsonb(OLD), to_jsonb(NEW));
        RETURN NEW;

    ELSIF (TG_OP = 'DELETE') THEN
        v_row_pk := OLD.id;
        INSERT INTO audit_row(actor_user_id, actor_username, request_id, ip, user_agent,
                              table_name, operation, row_pk, old_data, new_data)
        VALUES (v_actor_user_id, v_actor_username, v_request_id, v_ip, v_user_agent,
                TG_TABLE_NAME, TG_OP, v_row_pk, to_jsonb(OLD), NULL);
        RETURN OLD;
    END IF;

    RETURN NULL;
END;
$$;

CREATE TABLE IF NOT EXISTS auth_session
(
    id            uuid PRIMARY KEY     DEFAULT uuidv7(),
    user_id       uuid        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE, -- Optional: stable ID per device to group sessions (mobile)
    device_id     text        NULL,                                                -- Server-side session tracking
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    last_seen_at  timestamptz NOT NULL DEFAULT now(),
    ip            inet        NULL,
    user_agent    text        NULL,
    revoked       boolean     NOT NULL DEFAULT false,
    revoked_at    timestamptz NULL,
    revoke_reason text        NULL
);

CREATE INDEX IF NOT EXISTS idx_auth_session_user_id ON auth_session (user_id);
CREATE INDEX IF NOT EXISTS idx_auth_session_last_seen_at ON auth_session (last_seen_at DESC);

-- Refresh tokens (rotating)
CREATE TABLE IF NOT EXISTS refresh_token
(
    id                   uuid PRIMARY KEY     DEFAULT uuidv7(),

    session_id           uuid        NOT NULL REFERENCES auth_session (id) ON DELETE CASCADE,
    user_id              uuid        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,

    -- Hash only. Use SHA-256 over the raw token (or HMAC).
    token_hash           bytea       NOT NULL UNIQUE,

    issued_at            timestamptz NOT NULL DEFAULT now(),
    expires_at           timestamptz NOT NULL,

    -- rotation chain
    replaced_by_token_id uuid        NULL REFERENCES refresh_token (id) ON DELETE SET NULL,

    revoked              boolean     NOT NULL DEFAULT false,
    revoked_at           timestamptz NULL,
    revoke_reason        text        NULL
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON refresh_token (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_session_id ON refresh_token (session_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expires_at ON refresh_token (expires_at);

-- Optional: store access token "jti" for denylist (if you do logout for JWT)
CREATE TABLE IF NOT EXISTS access_token_denylist
(
    id         uuid PRIMARY KEY     DEFAULT uuidv7(),
    user_id    uuid        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    jti        text        NOT NULL UNIQUE,
    expires_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_access_token_denylist_expires_at ON access_token_denylist (expires_at);
