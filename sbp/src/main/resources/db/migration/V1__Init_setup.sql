SET search_path TO auth;
-- USERS
CREATE TABLE IF NOT EXISTS app_user
(
    id            uuid PRIMARY KEY     DEFAULT uuidv7(),
    username      text        NOT NULL UNIQUE,
    email         text        NOT NULL UNIQUE,
    password_hash text        NOT NULL, -- Store password hash only (BCrypt/Argon2 produced by Spring Security)
    enabled       boolean     NOT NULL DEFAULT true,
    locked        boolean     NOT NULL DEFAULT false,
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    last_login_at timestamptz NULL
);

-- ROLES
CREATE TABLE IF NOT EXISTS role
(
    id          uuid PRIMARY KEY     DEFAULT uuidv7(),
    name        text        NOT NULL UNIQUE, -- e.g. ADMIN, USER
    description text        NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);

-- PERMISSIONS
CREATE TABLE IF NOT EXISTS permission
(
    id          uuid PRIMARY KEY     DEFAULT uuidv7(),
    name        text        NOT NULL UNIQUE, -- e.g. USER_READ, USER_WRITE
    description text        NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);

-- USER <-> ROLE
CREATE TABLE IF NOT EXISTS user_role
(
    user_id    uuid        NOT NULL,
    role_id    uuid        NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_role_role_id ON user_role (role_id);

-- ROLE <-> PERMISSION
CREATE TABLE IF NOT EXISTS role_permission
(
    role_id       uuid        NOT NULL,
    permission_id uuid        NOT NULL,
    created_at    timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permission (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_role_permission_permission_id ON role_permission (permission_id);