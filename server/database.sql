DROP TABLE IF EXISTS site_scans;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE site_scans (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    image_name VARCHAR(255) NOT NULL,
    has_anomaly BOOLEAN NOT NULL,
    ai_description TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    hazard_type VARCHAR(100),
    affects_wheelchair BOOLEAN DEFAULT FALSE,
    affects_hearing BOOLEAN DEFAULT FALSE,
    affects_vision BOOLEAN DEFAULT FALSE,
    affects_autism BOOLEAN DEFAULT FALSE,
    affects_chronic_pain BOOLEAN DEFAULT FALSE,
    scan_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);