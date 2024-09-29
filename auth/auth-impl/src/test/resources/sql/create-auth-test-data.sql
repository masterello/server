--insert into public.users (uuid, name, lastname, email, phone, password, city, status, email_verified)
--values
--    ('49200ea0-3879-11ee-be56-0242ac120002', 'test1', 'user1', 'user1@gmail.com', '123123123', '$2a$10$YtXmJtc04cZPCH32VEkQEObDJqy.X1.Gx4ecuDWiCGaqSTnAKoZMS', 'milan', 0, true);
--    ('e8b0639f-148c-4f74-b834-bbe04072a416', 'test2', 'user2', 'user2@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, false);
--
--insert into public.user_roles (user_id, role)
--values
--    ('e8b0639f-148c-4f74-b834-bbe04072a416', 'USER'),
--    ('49200ea0-3879-11ee-be56-0242ac120002', 'USER');
--
--insert into public.user_languages (user_id, language)
--values
--     ('e8b0639f-148c-4f74-b834-bbe04072a416', 'RU'),
--     ('49200ea0-3879-11ee-be56-0242ac120002', 'DE');

INSERT INTO public.clients (
    id,
    client_id,
    client_id_issued_at,
    client_secret,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    scopes
) VALUES (
    'd9c235bc-73c2-42b0-b7b9-2ad0672e486e',
    'gw',
    current_timestamp,
    '$2a$10$mIIGRuS.nSPc//ctztiBx.OhC.PLixfP7nZ.KygDIFGoHKma8hw2q', --> Encrypted password
    'api-gateway',
    'client_secret_basic',
    'google_oid,refresh_token,client_credentials,password',
    'WORKER,ADMIN,USER'
);