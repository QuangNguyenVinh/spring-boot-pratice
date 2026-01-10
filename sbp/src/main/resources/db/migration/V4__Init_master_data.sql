SET search_path TO auth;
INSERT INTO role (name, description)
VALUES ('ADMIN', 'System administrator'),
       ('USER', 'Standard user')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (name, description)
VALUES ('USER_READ', 'Read users'),
       ('USER_WRITE', 'Write users'),
       ('ROLE_READ', 'Read roles'),
       ('ROLE_WRITE', 'Write roles'),
       ('PERMISSION_READ', 'Read permissions'),
       ('PERMISSION_WRITE', 'Write permissions')
ON CONFLICT (name) DO NOTHING;

-- ADMIN gets everything
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r
         CROSS JOIN permission p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- USER gets basics
INSERT INTO role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM role r
         JOIN permission p ON p.name IN ('USER_READ')
WHERE r.name = 'USER'
ON CONFLICT DO NOTHING;
