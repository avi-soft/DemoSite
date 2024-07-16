ALTER TABLE blc_product
ADD CONSTRAINT chk_go_live_date CHECK (go_live_date >= created_date);