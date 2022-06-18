create table "user" (
    id            serial primary key,
    username      varchar(15) not null unique,
    password_hash varchar(60) not null
);

alter table "user"
    owner to postgres;

