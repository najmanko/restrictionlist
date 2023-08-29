CREATE TYPE restriction_type AS ENUM ('US1', 'US2', 'US3', 'US4', 'RU', 'BY');

CREATE TABLE restricted_person
(
    cluid   VARCHAR(50) PRIMARY KEY,
    restriction_type restriction_type NOT NULL,
    signed boolean,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_to TIMESTAMP WITH TIME ZONE
)