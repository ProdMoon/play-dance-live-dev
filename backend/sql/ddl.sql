drop table if exists member CASCADE;
create table member
(
    id   bigint generated by default as identity,
    name varchar(255),
    primary key (id)
);
drop table if exists waitlist CASCADE;
create table waitlist
(
    id   bigint generated by default as identity,
    name varchar(255),
    primary key (id)
);
drop table if exists broadcast CASCADE;
create table broadcast
(
    id   bigint generated by default as identity,
    name varchar(255),
    primary key (id)
);