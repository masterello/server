insert into public.users (uuid, name, lastname, email, phone, password, city, status, email_verified)
values
    ('e8b0639f-148c-4f74-b834-bbe04072a416', 'test', 'user', 'not_verified_link_valid@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, false),
    ('e8b0639f-148c-4f74-b834-bbe04072a999', 'aa', 'bb', 'not_verified_link_expired@gmail.com', '123456712312', 'vvvvvvv', 'berlin', 0, false),
    ('49200ea0-3879-11ee-be56-0242ac120002', 'test1', 'user1', 'verified@gmail.com', '123123123', '$2a$10$YtXmJtc04cZPCH32VEkQEObDJqy.X1.Gx4ecuDWiCGaqSTnAKoZMS', 'milan', 0, true);

insert into public.user_roles (user_id, role)
values
    ('e8b0639f-148c-4f74-b834-bbe04072a416', 'USER'),
    ('e8b0639f-148c-4f74-b834-bbe04072a999', 'USER'),
    ('49200ea0-3879-11ee-be56-0242ac120002', 'USER');

insert into public.user_languages (user_id, language)
values
     ('49200ea0-3879-11ee-be56-0242ac120002', 'RU'),
     ('49200ea0-3879-11ee-be56-0242ac120002', 'DE');

insert into public.confirmation_link (uuid, user_uuid, token, expires_at)
values
    ('7a321348-387a-11ee-be56-0242ac120002', '49200ea0-3879-11ee-be56-0242ac120002', '84e9798e-387a-11ee-be56-0242ac120002', NOW()),
    ('7a321348-387a-11ee-be56-0242ac120003', 'e8b0639f-148c-4f74-b834-bbe04072a416', '84e9798e-387a-11ee-be56-0242ac120011', NOW() + INTERVAL '1 DAY'),
    ('7a321348-387a-11ee-be56-0242ac120004', 'e8b0639f-148c-4f74-b834-bbe04072a999', '84e9798e-387a-11ee-be56-0242ac120099', NOW() - INTERVAL '1 DAY'),
    ('7a321348-387a-11ee-be56-0242ac120009', 'e8b0639f-148c-4f74-b834-aaaaaaaaa000', '84e9798e-387a-11ee-be56-000000000002', NOW() - INTERVAL '1 DAY');

insert into public.support (uuid, title, email, phone, message, processed, creation_date)
values
    ('2d3c2abe-af52-4008-b147-6c816dbaba04', 'Support 1', 'test@test.com', '91213', 'Login is not working', true, NOW() - INTERVAL '1 DAY'),
    ('2d3c2abe-af52-4008-b147-6c816dbaba05', 'Support 2', 'test@test.com', '91213', 'Login is not working!!', true, NOW() - INTERVAL '1 DAY'),
    ('2d3c2abe-af52-4008-b147-6c816dbaba06', 'Support 3', 'test@test.com', '91213', 'Login is still not working!!!', false, NOW());

