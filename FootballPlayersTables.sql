CREATE TABLE football_players (
    player_id INT,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    name VARCHAR(255),
    last_season YEAR,
    current_club_id INT,
    player_code VARCHAR(255),
    country_of_birth VARCHAR(255),
    city_of_birth VARCHAR(255),
    country_of_citizenship VARCHAR(255),
    date_of_birth DATE,
    sub_position VARCHAR(255),
    position VARCHAR(255),
    foot VARCHAR(50),
    height_in_cm INT,
    contract_expiration_date DATE,
    agent_name VARCHAR(255),
    image_url VARCHAR(255),
    url VARCHAR(255),
    current_club_domestic_competition_id INT,
    current_club_name VARCHAR(255),
    market_value_in_eur DECIMAL(15,2),
    highest_market_value_in_eur DECIMAL(15,2)
);


LOAD DATA LOCAL INFILE 'Desktop/Spring 24/CSE 385/Project/players.csv'
INTO TABLE football_players
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 lines;

CREATE TABLE Players(
    player_id INT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    sub_position VARCHAR(255)
);
INSERT INTO Players (player_id, first_name, last_name, sub_position)
SELECT player_id, first_name, last_name, sub_position
FROM football_players
WHERE player_id is not NULL;

CREATE TABLE Clubs (
    club_id INT PRIMARY KEY,
    club_name VARCHAR(255)
);

INSERT INTO Clubs (club_id, club_name)
SELECT DISTINCT current_club_id, current_club_name
FROM football_players
WHERE current_club_id is not NULL;


CREATE TABLE PlayerClubContracts (
    contract_id INT PRIMARY KEY,
    player_id INT,
    club_id INT,
    market_value_in_eur DECIMAL(15,2),
    FOREIGN KEY (player_id) REFERENCES Players(player_id),
    FOREIGN KEY (club_id) REFERENCES Clubs(club_id)
);
INSERT INTO PlayerClubContracts (contract_id, player_id, club_id, market_value_in_eur)
SELECT player_id, player_id, current_club_id, market_value_in_eur
FROM football_players;

select * from football_players;
select * from players;
select * from clubs;
select * from playerclubcontracts;

