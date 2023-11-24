create database if not exists api_social_quarkus;

use api_social_quarkus;

create table users (
    id bigint auto_increment not null primary key,
    name varchar(100) not null,
    age date not null
);

create table posts (
    id bigint auto_increment not null primary key,
    post_text varchar(150) not null,
    date_time date not null,
    user_id integer not null,
    foreign key (user_id) references users(id)
);

create table followers (
    id bigint auto_increment not null primary key,
    user_id bigint not null,
    follower_id bigint not null,
    foreign key (user_id) references users(id),
    foreign key (follower_id) references users(id)
)