CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_projects_created_at ON projects (created_at);
