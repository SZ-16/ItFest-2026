CREATE TABLE site_scans (
    id SERIAL PRIMARY KEY,
    image_name VARCHAR(255) NOT NULL,
    has_anomaly BOOLEAN NOT NULL,
    ai_description TEXT,
    scan_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);