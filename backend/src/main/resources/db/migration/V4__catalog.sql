-- V4: catalog P3 — khu vực, hãng rạp, rạp, phòng, ghế, phim, suất chiếu, bắp nước.
-- Quy ước: NVARCHAR + N'...' cho tiếng Việt; tiền VND lưu BIGINT nguyên; không GO/USE.

CREATE TABLE regions (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    name       NVARCHAR(100) NOT NULL,
    sort_order INT           NOT NULL CONSTRAINT df_regions_sort DEFAULT 0
);

CREATE TABLE cinema_chains (
    id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    name     NVARCHAR(120) NOT NULL,
    logo_url NVARCHAR(500) NULL
);

CREATE TABLE cinemas (
    id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    chain_id  BIGINT        NOT NULL,
    region_id BIGINT        NOT NULL,
    name      NVARCHAR(150) NOT NULL,
    address   NVARCHAR(300) NOT NULL,
    CONSTRAINT fk_cinemas_chain  FOREIGN KEY (chain_id)  REFERENCES cinema_chains(id),
    CONSTRAINT fk_cinemas_region FOREIGN KEY (region_id) REFERENCES regions(id)
);
CREATE INDEX ix_cinemas_region ON cinemas(region_id);

CREATE TABLE rooms (
    id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    cinema_id BIGINT       NOT NULL,
    name      NVARCHAR(50)  NOT NULL,
    room_type NVARCHAR(20)  NOT NULL CONSTRAINT df_rooms_type DEFAULT '2D',
    CONSTRAINT fk_rooms_cinema  FOREIGN KEY (cinema_id) REFERENCES cinemas(id),
    CONSTRAINT uq_rooms_cinema_name UNIQUE (cinema_id, name),
    CONSTRAINT ck_rooms_type CHECK (room_type IN ('2D', '3D', 'IMAX'))
);

CREATE TABLE seats (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    room_id    BIGINT       NOT NULL,
    row_label  NVARCHAR(2)  NOT NULL,
    col_number INT          NOT NULL,
    seat_type  NVARCHAR(20) NOT NULL CONSTRAINT df_seats_type DEFAULT 'STANDARD',
    CONSTRAINT fk_seats_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT uq_seats_room_pos UNIQUE (room_id, row_label, col_number),
    CONSTRAINT ck_seats_type CHECK (seat_type IN ('STANDARD', 'VIP', 'COUPLE'))
);

CREATE TABLE genres (
    id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE movies (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    title        NVARCHAR(200)  NOT NULL,
    description  NVARCHAR(MAX)  NULL,
    duration_min INT            NOT NULL,
    age_rating   NVARCHAR(5)    NOT NULL CONSTRAINT df_movies_age DEFAULT 'P',
    poster_url   NVARCHAR(500)  NULL,
    backdrop_url NVARCHAR(500)  NULL,
    trailer_url  NVARCHAR(500)  NULL,
    status       NVARCHAR(20)   NOT NULL CONSTRAINT df_movies_status DEFAULT 'COMING_SOON',
    release_date DATE           NULL,
    created_at   DATETIME2      NOT NULL CONSTRAINT df_movies_created DEFAULT SYSDATETIME(),
    CONSTRAINT ck_movies_age    CHECK (age_rating IN ('P', 'K', 'T13', 'T16', 'T18')),
    CONSTRAINT ck_movies_status CHECK (status IN ('NOW_SHOWING', 'COMING_SOON', 'ARCHIVED'))
);
CREATE INDEX ix_movies_status ON movies(status);

CREATE TABLE movie_genres (
    movie_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    CONSTRAINT pk_movie_genres PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT fk_movie_genres_movie FOREIGN KEY (movie_id) REFERENCES movies(id),
    CONSTRAINT fk_movie_genres_genre FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE showtimes (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    movie_id   BIGINT      NOT NULL,
    room_id    BIGINT      NOT NULL,
    starts_at  DATETIME2   NOT NULL,
    ends_at    DATETIME2   NOT NULL,
    base_price BIGINT      NOT NULL,
    status     NVARCHAR(20) NOT NULL CONSTRAINT df_showtimes_status DEFAULT 'OPEN',
    CONSTRAINT fk_showtimes_movie FOREIGN KEY (movie_id) REFERENCES movies(id),
    CONSTRAINT fk_showtimes_room  FOREIGN KEY (room_id)  REFERENCES rooms(id),
    CONSTRAINT ck_showtimes_status CHECK (status IN ('OPEN', 'CLOSED'))
);
CREATE INDEX ix_showtimes_room_start  ON showtimes(room_id, starts_at);
CREATE INDEX ix_showtimes_movie_start ON showtimes(movie_id, starts_at);

CREATE TABLE concessions (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(120) NOT NULL,
    description NVARCHAR(300) NULL,
    price       BIGINT        NOT NULL,
    image_url   NVARCHAR(500) NULL,
    category    NVARCHAR(20)  NOT NULL CONSTRAINT df_concessions_cat DEFAULT 'SINGLE',
    is_active   BIT           NOT NULL CONSTRAINT df_concessions_active DEFAULT 1,
    CONSTRAINT ck_concessions_cat CHECK (category IN ('COMBO', 'SINGLE'))
);
