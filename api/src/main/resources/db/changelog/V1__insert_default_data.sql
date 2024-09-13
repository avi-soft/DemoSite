-- Insert data for Himachal Pradesh
INSERT INTO custom_districts (district_name, state_code) VALUES
('Bilaspur', 'HP'),
('Chamba', 'HP'),
('Hamirpur', 'HP'),
('Kangra', 'HP'),
('Kinnaur', 'HP'),
('Kullu', 'HP'),
('Lahaul and Spiti', 'HP'),
('Mandi', 'HP'),
('Shimla', 'HP'),
('Sirmaur', 'HP'),
('Solan', 'HP'),
('Una', 'HP');

-- Insert data for Jammu & Kashmir
-- Jammu Division
INSERT INTO custom_districts (district_name, state_code) VALUES
('Jammu', 'JK'),
('Samba', 'JK'),
('Kathua', 'JK'),
('Udhampur', 'JK'),
('Reasi', 'JK'),
('Ramban', 'JK'),
('Doda', 'JK'),
('Poonch', 'JK'),
('Rajouri', 'JK'),
('Anantnag', 'JK'),
('Kishtwar', 'JK');

-- Kashmir Division
INSERT INTO custom_districts (district_name, state_code) VALUES
('Srinagar', 'JK'),
('Baramulla', 'JK'),
('Pulwama', 'JK'),
('Shopian', 'JK'),
('Anantnag', 'JK'),
('Bandipora', 'JK'),
('Ganderbal', 'JK'),
('Kulgam', 'JK');

-- Insert data for Punjab
INSERT INTO custom_districts (district_name, state_code) VALUES
('Amritsar', 'PB'),
('Barnala', 'PB'),
('Bathinda', 'PB'),
('Faridkot', 'PB'),
('Fatehgarh Sahib', 'PB'),
('Fazilka', 'PB'),
('Ferozepur', 'PB'),
('Gurdaspur', 'PB'),
('Hoshiarpur', 'PB'),
('Jalandhar', 'PB'),
('Kapurthala', 'PB'),
('Ludhiana', 'PB'),
('Mansa', 'PB'),
('Moga', 'PB'),
('Mohali', 'PB'),
('Pathankot', 'PB'),
('Patiala', 'PB'),
('Rupnagar', 'PB'),
('Sangrur', 'PB'),
('Tarn Taran', 'PB');

-- Insert data for Haryana
INSERT INTO custom_districts (district_name, state_code) VALUES
('Ambala', 'HR'),
('Bhiwani', 'HR'),
('Faridabad', 'HR'),
('Fatehabad', 'HR'),
('Gurgaon', 'HR'),
('Hisar', 'HR'),
('Jhajjar', 'HR'),
('Jind', 'HR'),
('Kaithal', 'HR'),
('Karnal', 'HR'),
('Mahendragarh', 'HR'),
('Mewat', 'HR'),
('Palwal', 'HR'),
('Panchkula', 'HR'),
('Panipat', 'HR'),
('Rewari', 'HR'),
('Sirsa', 'HR'),
('Sonipat', 'HR'),
('Yamunanagar', 'HR');


-- States and UTs of India

INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (1, 'Andhra Pradesh', 'AP');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (2, 'Arunachal Pradesh', 'AR');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (3, 'Assam', 'AS');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (4, 'Bihar', 'BR');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (5, 'Chhattisgarh', 'CG');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (6, 'Goa', 'GA');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (7, 'Gujarat', 'GJ');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (8, 'Haryana', 'HR');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (9, 'Himachal Pradesh', 'HP');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (10, 'Jharkhand', 'JH');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (11, 'Karnataka', 'KA');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (12, 'Kerala', 'KL');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (13, 'Madhya Pradesh', 'MP');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (14, 'Maharashtra', 'MH');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (15, 'Manipur', 'MN');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (16, 'Meghalaya', 'ML');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (17, 'Mizoram', 'MZ');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (18, 'Nagaland', 'NL');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (19, 'Odisha', 'OD');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (20, 'Punjab', 'PB');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (21, 'Rajasthan', 'RJ');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (22, 'Sikkim', 'SK');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (23, 'Tamil Nadu', 'TN');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (24, 'Telangana', 'TS');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (25, 'Tripura', 'TR');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (26, 'Uttar Pradesh', 'UP');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (27, 'Uttarakhand', 'UK');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (28, 'West Bengal', 'WB');

-- Union Territories
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (29, 'Andaman and Nicobar Islands', 'AN');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (30, 'Chandigarh', 'CH');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (31, 'Dadra and Nagar Haveli and Daman and Diu', 'DN');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (32, 'Lakshadweep', 'LD');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (33, 'Delhi', 'DL');
INSERT INTO custom_state_codes (state_id, state_name, state_code) VALUES (34, 'Puducherry', 'PY');

INSERT INTO custom_service_provider_address_ref (address_type_Id, address_name) VALUES (1, 'OFFICE_ADDRESS');
INSERT INTO custom_service_provider_address_ref (address_type_Id, address_name) VALUES (2, 'CURRENT_ADDRESS');
INSERT INTO custom_service_provider_address_ref (address_type_Id, address_name) VALUES (3, 'BILLING_ADDRESS');
INSERT INTO custom_service_provider_address_ref (address_type_Id, address_name) VALUES (4, 'MAILING_ADDRESS');

INSERT INTO custom_document (document_type_id, document_type_name, description) VALUES (1, 'Aadhaar Card', 'A government-issued ID card in India');
INSERT INTO custom_document (document_type_id, document_type_name, description) VALUES (2, 'PAN Card', 'A permanent account number card for tax purposes in India');
INSERT INTO custom_document (document_type_id, document_type_name, description) VALUES (3, 'Passport Size Photo', 'A small photo typically used for official documents');
INSERT INTO custom_document (document_type_id, document_type_name, description) VALUES (4, 'Signature', 'A handwritten sign used to authenticate documents');

INSERT INTO custom_privileges (privilege_id, privilege_name, description) VALUES
(1, 'ADD_PRODUCT', 'Allows the ADMIN to add new products to the inventory. This includes entering product details such as name, price, category, and stock quantity.'),
(2, 'MANAGE_SERVICE_PROVIDER', 'Grants the ADMIN the ability to manage service providers, which includes adding, updating, or removing service provider details, and managing their service-related information.');


INSERT INTO custom_role_table (role_id, role_name, created_at, updated_at, created_by) VALUES
(1, 'SUPER_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
(2, 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
(3, 'ADMIN_SERVICE_PROVIDER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
(4, 'SERVICE_PROVIDER', CURRENT_TIMESTAMP,CURRENT_TIMESTAMP, 'SUPER_ADMIN'),
(5, 'CUSTOMER', CURRENT_TIMESTAMP,CURRENT_TIMESTAMP, 'SUPER_ADMIN');

INSERT INTO custom_service_provider_infra (infra_id, infra_name) VALUES (1, 'DESKTOP');
INSERT INTO custom_service_provider_infra (infra_id, infra_name) VALUES (2, 'SCANNER');
INSERT INTO custom_service_provider_infra (infra_id, infra_name) VALUES (3, 'LAPTOP');
INSERT INTO custom_service_provider_infra (infra_id, infra_name) VALUES (4, 'PRINTER');
INSERT INTO custom_service_provider_infra (infra_id, infra_name) VALUES (5, 'INTERNET_BROADBAND');

INSERT INTO custom_service_provider_language (language_id, language_name) VALUES (1, 'Hindi');
INSERT INTO custom_service_provider_language (language_id, language_name) VALUES (2, 'Bengali');
INSERT INTO custom_service_provider_language (language_id, language_name) VALUES (3, 'Telugu');
INSERT INTO custom_service_provider_language (language_id, language_name) VALUES (4, 'Marathi');
INSERT INTO custom_service_provider_language (language_id, language_name) VALUES (5, 'Tamil');
INSERT INTO custom_service_provider_language (language_id, language_name) VALUES (6, 'Gujarati');
INSERT INTO custom_service_provider_language (language_id, language_name) VALUES (7, 'Punjabi');

INSERT INTO custom_skill_set (skill_id, skill_name) VALUES (1, 'Form Filling Knowledge/Expertise');
INSERT INTO custom_skill_set (skill_id, skill_name) VALUES (2, 'Resizing & Uploading Image/Document');
INSERT INTO custom_skill_set (skill_id, skill_name) VALUES (3, 'Executing Online Payment/Transactions');
INSERT INTO custom_skill_set (skill_id, skill_name) VALUES (4, 'Apply To Various Government Schemes');

INSERT INTO ServiceProviderStatus (status_id, status_name, description, created_at, updated_at, created_by)
VALUES (1, 'DOCUMENTS_SUBMISSION_PENDING', 'Documents submission is pending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN');

INSERT INTO ServiceProviderStatus (status_id, status_name, description, created_at, updated_at, created_by)
VALUES (2, 'APPLIED', 'Application has been submitted', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN');

INSERT INTO ServiceProviderStatus (status_id, status_name, description, created_at, updated_at, created_by)
VALUES (3, 'APPROVAL_PENDING', 'Application is awaiting approval', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN');

INSERT INTO ServiceProviderStatus (status_id, status_name, description, created_at, updated_at, created_by)
VALUES (4, 'APPROVED', 'Application has been approved', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUPER_ADMIN');

INSERT INTO qualification(qualification_id, qualification_name, qualification_description)
VALUES(1, 'MATRICULATION', 'Completed secondary education or equivalent');

INSERT INTO qualification(qualification_id, qualification_name, qualification_description)
VALUES(2, 'INTERMEDIATE', 'Completed higher secondary education or equivalent');

INSERT INTO qualification(qualification_id, qualification_name, qualification_description)
VALUES(3, 'BACHELORS', 'Completed undergraduate degree program');

INSERT INTO qualification(qualification_id, qualification_name, qualification_description)
VALUES(4, 'MASTERS', 'Completed postgraduate degree program');

INSERT INTO qualification(qualification_id, qualification_name, qualification_description)
VALUES(5, 'DOCTORATE', 'Completed doctoral degree program');

