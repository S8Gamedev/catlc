CREATE TABLE IF NOT EXISTS skill_content (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             skill_id BIGINT NOT NULL UNIQUE,
                                             title VARCHAR(255) NOT NULL,
    summary VARCHAR(2000),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_skill_content_skill
    FOREIGN KEY (skill_id) REFERENCES skill(id)
    );