INSERT INTO training_types (training_type_name) VALUES ('Yoga');
INSERT INTO training_types (training_type_name) VALUES ('Cardio');
INSERT INTO training_types (training_type_name) VALUES ('Pilates');
INSERT INTO training_types (training_type_name) VALUES ('Strength');
INSERT INTO training_types (training_type_name) VALUES ('Stretching');

INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Alice', 'Smith', 'alice.smith', 'password123', true);
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Bob', 'Johnson', 'bob.johnson', 'password123', true);
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Charlie', 'Brown', 'charlie.brown', 'password123', true);
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Diana', 'Prince', 'diana.prince', 'password123', true);
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Evan', 'Taylor', 'evan.taylor', 'password123', true);

INSERT INTO trainers (specialization_id, user_id) VALUES (1, 2);
INSERT INTO trainers (specialization_id, user_id) VALUES (2, 3);
INSERT INTO trainers (specialization_id, user_id) VALUES (4, 4);

INSERT INTO trainees (date_of_birth, address, user_id) VALUES ('1990-05-12', '123 Main St', 1);
INSERT INTO trainees (date_of_birth, address, user_id) VALUES ('1995-08-20', '456 Oak Ave', 5);

INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (1, 1);
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (1, 2);
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (2, 2);
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (2, 3);

INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES (1, 1, 'Morning Yoga Session', 1, '2026-04-02', 60);
INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES (1, 2, 'Cardio Blast', 2, '2026-04-03', 45);
INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES (2, 2, 'Intense Cardio', 2, '2026-04-02', 50);
INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES (2, 3, 'Strength Training Basics', 4, '2026-04-04', 70);