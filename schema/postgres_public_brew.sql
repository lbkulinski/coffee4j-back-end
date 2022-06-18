create table brew (
    id          serial primary key,
    user_id     integer        not null references "user" on update cascade on delete cascade,
    timestamp   timestamp      not null,
    coffee_id   integer        not null references coffee on update cascade on delete cascade,
    water_id    integer        not null references water on update cascade on delete cascade,
    brewer_id   integer        not null references brewer on update cascade on delete cascade,
    filter_id   integer        not null references filter on update cascade on delete cascade,
    vessel_id   integer        not null references vessel on update cascade on delete cascade,
    coffee_mass numeric(10, 4) not null,
    water_mass  numeric(10, 4) not null
);

alter table brew
    owner to postgres;

