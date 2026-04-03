-- Initial Patient Profiles [cite: 38-43]
INSERT INTO patient_profiles (patient_id, name, date_of_birth, contact_details)
VALUES (1000, 'John Doe', '1985-05-15', '9876543210');

INSERT INTO patient_profiles (patient_id, name, date_of_birth, contact_details)
VALUES (1001, 'Jane Smith', '1992-11-22', '8765432109');

INSERT INTO patient_profiles (patient_id, name, date_of_birth, contact_details)
VALUES (1002, 'Robert Wilson', '1978-03-10', '7654321098');

-- Ensure the sequence is updated to avoid primary key conflicts
ALTER TABLE patient_profiles AUTO_INCREMENT = 1003;