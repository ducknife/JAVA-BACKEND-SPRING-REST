alter table users 
add column provider varchar(20) not null default 'LOCAL',
add column provider_id varchar(100) null,
modify column password varchar(100) null;