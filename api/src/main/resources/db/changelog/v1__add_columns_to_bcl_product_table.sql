ALTER TABLE blc_product
ADD COLUMN created_date DATE DEFAULT CURRENT_DATE,
ADD COLUMN expiration_date DATE,
ADD COLUMN go_live_date DATE;
