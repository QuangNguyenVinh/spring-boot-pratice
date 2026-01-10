-- Automatically update updated-_updated_at

CREATE OR REPLACE TRIGGER trg_app_user_set_updated_at
BEFORE
UPDATE ON app_user
    FOR EACH ROW
    EXECUTE FUNCTION app_set_updated_at();

CREATE OR REPLACE TRIGGER trg_role_set_updated_at
    BEFORE
UPDATE ON role
    FOR EACH ROW
    EXECUTE FUNCTION app_set_updated_at();

CREATE OR REPLACE TRIGGER trg_permission_set_updated_at
    BEFORE
UPDATE ON permission
    FOR EACH ROW
    EXECUTE FUNCTION app_set_updated_at();

-- Enable row-auditing on core auth tables
CREATE OR REPLACE TRIGGER aud_app_user
    AFTER INSERT OR
UPDATE OR
DELETE
ON app_user
    FOR EACH ROW EXECUTE FUNCTION app_audit_row();

CREATE OR REPLACE TRIGGER aud_role
    AFTER INSERT OR
UPDATE OR
DELETE
ON role
    FOR EACH ROW EXECUTE FUNCTION app_audit_row();

CREATE OR REPLACE TRIGGER aud_permission
    AFTER INSERT OR
UPDATE OR
DELETE
ON permission
    FOR EACH ROW EXECUTE FUNCTION app_audit_row();

CREATE OR REPLACE TRIGGER trg_auth_session_set_updated_at
    BEFORE
UPDATE ON auth_session
    FOR EACH ROW EXECUTE FUNCTION app_set_updated_at();

CREATE OR REPLACE TRIGGER aud_auth_session
    AFTER INSERT OR
UPDATE OR
DELETE
ON auth_session
    FOR EACH ROW EXECUTE FUNCTION app_audit_row();

CREATE OR REPLACE TRIGGER aud_refresh_token
    AFTER INSERT OR
UPDATE OR
DELETE
ON refresh_token
    FOR EACH ROW EXECUTE FUNCTION app_audit_row();

CREATE OR REPLACE TRIGGER aud_access_token_denylist
    AFTER INSERT OR
UPDATE OR
DELETE
ON access_token_denylist
    FOR EACH ROW EXECUTE FUNCTION app_audit_row();