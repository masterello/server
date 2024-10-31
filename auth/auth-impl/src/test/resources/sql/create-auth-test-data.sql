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