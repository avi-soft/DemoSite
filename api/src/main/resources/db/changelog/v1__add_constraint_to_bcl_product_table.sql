ALTER TABLE blc_product
ADD CONSTRAINT chk_expiration_date CHECK (expiration_date >= created_date);