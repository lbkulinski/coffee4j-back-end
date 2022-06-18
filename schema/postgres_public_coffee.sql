create table coffee (
    id      serial primary key,
    user_id integer     not null references "user" on update cascade on delete cascade,
    name    varchar(45) not null
);

alter table coffee
    owner to postgres;

