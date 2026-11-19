CREATE TABLE iam_users (
    id varchar(100) PRIMARY KEY,
    username varchar(200) NOT NULL UNIQUE,
    email varchar(200) NOT NULL UNIQUE,
    password_hash varchar(200) NOT NULL,
    first_name varchar(100),
    last_name varchar(100),
    enabled boolean NOT NULL DEFAULT true,
    platform_user_id varchar(100),
    authentication_version bigint NOT NULL DEFAULT 1,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_iam_users_email ON iam_users (email);
CREATE INDEX idx_iam_users_platform_user_id ON iam_users (platform_user_id);

CREATE TABLE iam_user_authorities (
    user_id varchar(100) NOT NULL REFERENCES iam_users(id) ON DELETE CASCADE,
    authority varchar(100) NOT NULL,
    PRIMARY KEY (user_id, authority)
);

CREATE TABLE iam_user_access_contexts (
    id varchar(100) PRIMARY KEY,
    user_id varchar(100) NOT NULL REFERENCES iam_users(id) ON DELETE CASCADE,
    assignment_id varchar(100) NOT NULL,
    scope_key varchar(100) NOT NULL,
    scope_id varchar(100) NOT NULL,
    role varchar(100) NOT NULL,
    display_name varchar(200),
    authorities text NOT NULL,
    synced_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_iam_user_contexts_user ON iam_user_access_contexts (user_id);
