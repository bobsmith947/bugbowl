CREATE TABLE IF NOT EXISTS hackathon_users (
	id serial PRIMARY KEY,
	data jsonb NOT NULL
);

CREATE INDEX IF NOT EXISTS hackathon_user_name_idx ON hackathon_users USING GIN ((data -> 'name'));

CREATE TABLE IF NOT EXISTS hackathon_competitions (
	id serial PRIMARY KEY,
	data jsonb NOT NULL
);

CREATE TABLE IF NOT EXISTS hackathon_submissions (
	id serial PRIMARY KEY,
	user_id integer NOT NULL REFERENCES hackathon_users (id),
	comp_id integer NOT NULL REFERENCES hackathon_competitions (id),
	data jsonb NOT NULL
);