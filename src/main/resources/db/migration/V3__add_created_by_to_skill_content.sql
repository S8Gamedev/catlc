ALTER TABLE skill_content
    ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT;

UPDATE skill_content
SET created_by_user_id = 1
WHERE created_by_user_id IS NULL;

ALTER TABLE skill_content
    ALTER COLUMN created_by_user_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_skill_content_created_by_user'
    ) THEN
ALTER TABLE skill_content
    ADD CONSTRAINT fk_skill_content_created_by_user
        FOREIGN KEY (created_by_user_id) REFERENCES users(id);
END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'uq_skill_content_skill_user'
    ) THEN
ALTER TABLE skill_content
    ADD CONSTRAINT uq_skill_content_skill_user
        UNIQUE (skill_id, created_by_user_id);
END IF;
END $$;