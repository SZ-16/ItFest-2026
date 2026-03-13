DROP TABLE IF EXISTS site_scans;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE site_scans (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id), -
    image_name VARCHAR(255) NOT NULL,
    has_anomaly BOOLEAN NOT NULL,
    ai_description TEXT,
    scan_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);