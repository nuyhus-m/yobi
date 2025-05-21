CREATE TABLE users (
                       user_id INTEGER PRIMARY KEY,
                       name VARCHAR(10) NOT NULL,
                       employee_number INTEGER NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       consent BOOLEAN NOT NULL,
                       image VARCHAR(255)
);

CREATE TABLE clients (
                         client_id INTEGER PRIMARY KEY,
                         user_id INTEGER NOT NULL,
                         name VARCHAR(10) NOT NULL,
                         birth DATE NOT NULL,
                         gender INTEGER NOT NULL,
                         height REAL NOT NULL,
                         weight REAL NOT NULL,
                         image VARCHAR(255),
                         address VARCHAR(100) NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES users(user_id)
);


CREATE TABLE body_composition (
                                  composition_id BIGINT PRIMARY KEY,
                                  bfp REAL ,
                                  bfm REAL ,
                                  smm REAL ,
                                  bmr REAL ,
                                  icw REAL ,
                                  ecw REAL ,
                                  ecf REAL ,
                                  protein REAL ,
                                  mineral REAL ,
                                  bodyage SMALLINT,
                                  created_at BIGINT
);

CREATE TABLE blood_pressure (
                                blood_id BIGINT PRIMARY KEY,
                                sbp FLOAT,
                                dbp FLOAT,
                                created_at BIGINT
);

CREATE TABLE heart_rate (
                            heart_id BIGINT PRIMARY KEY,
                            bpm SMALLINT,
                            oxygen SMALLINT,
                            created_at BIGINT
);

CREATE TABLE stress (
                        stress_id BIGINT PRIMARY KEY,
                        stress_value SMALLINT,
                        stress_level VARCHAR(10),
                        oxygen SMALLINT,
                        bpm SMALLINT,
                        created_at BIGINT
);

CREATE TABLE temperature (
                             temperature_id BIGINT PRIMARY KEY,
                             temperature REAL,
                             created_at BIGINT
);

CREATE TABLE measure (
                         measure_id BIGINT PRIMARY KEY,
                         date BIGINT NOT NULL,
                         user_id INTEGER NOT NULL,
                         client_id INTEGER NOT NULL,
                         composition_id BIGINT NOT NULL,
                         blood_id BIGINT NOT NULL,
                         heart_id BIGINT,
                         stress_id BIGINT,
                         temperature_id BIGINT,
                         FOREIGN KEY (user_id) REFERENCES users(user_id),
                         FOREIGN KEY (client_id) REFERENCES clients(client_id),
                         FOREIGN KEY (composition_id) REFERENCES body_composition(composition_id),
                         FOREIGN KEY (blood_id) REFERENCES blood_pressure(blood_id),
                         FOREIGN KEY (heart_id) REFERENCES heart_rate(heart_id),
                         FOREIGN KEY (stress_id) REFERENCES stress(stress_id),
                         FOREIGN KEY (temperature_id) REFERENCES temperature(temperature_id)
);


CREATE TABLE schedule (
                          schedule_id SERIAL PRIMARY KEY,
                          user_id INTEGER NOT NULL,
                          client_id INTEGER NOT NULL,
                          visited_date BIGINT NOT NULL,
                          start_at BIGINT NOT NULL,
                          end_at BIGINT NOT NULL,
                          log_content VARCHAR(150),
                          log_created_at TIMESTAMP,
                          log_updated_at TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(user_id),
                          FOREIGN KEY (client_id) REFERENCES clients(client_id)
);

CREATE TABLE weekly_report (
                               report_id BIGINT PRIMARY KEY,
                               client_id INTEGER NOT NULL,
                               report_content TEXT NOT NULL,
                               log_summary TEXT NOT NULL,
                               created_at BIGINT NOT NULL,
                               FOREIGN KEY (client_id) REFERENCES clients(client_id)
);


CREATE TABLE batch_logs (
                            id BIGSERIAL PRIMARY KEY,
                            executed_at BIGINT NOT NULL,
                            total_clients INTEGER NOT NULL,
                            success_count INTEGER NOT NULL,
                            error_count INTEGER NOT NULL,
                            duration NUMERIC(10, 2) NOT NULL,
                            success_rate NUMERIC(5, 2) NOT NULL,
                            failed_client_ids JSONB,
                            details JSONB
);


-- users 테이블
ALTER TABLE users
ALTER COLUMN user_id
ADD GENERATED BY DEFAULT AS IDENTITY;

-- clients 테이블
ALTER TABLE clients
ALTER COLUMN client_id
ADD GENERATED BY DEFAULT AS IDENTITY;

-- body_composition 테이블
ALTER TABLE body_composition
ALTER COLUMN composition_id
ADD GENERATED BY DEFAULT AS IDENTITY;

-- blood_pressure 테이블
ALTER TABLE blood_pressure
ALTER COLUMN blood_id
ADD GENERATED BY DEFAULT AS IDENTITY;

-- heart_rate 테이블
ALTER TABLE heart_rate
ALTER COLUMN heart_id
ADD GENERATED BY DEFAULT AS IDENTITY;

-- stress 테이블
ALTER TABLE stress
ALTER COLUMN stress_id
ADD GENERATED BY DEFAULT AS IDENTITY;

-- temperature 테이블
ALTER TABLE temperature
ALTER COLUMN temperature_id
ADD GENERATED BY DEFAULT AS IDENTITY;

-- measure 테이블
ALTER TABLE measure
ALTER COLUMN measure_id
ADD GENERATED BY DEFAULT AS IDENTITY;


