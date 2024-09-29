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
    '$2a$10$tLIpyfrweaz.H1Ta/oiuiOc.WxfK29t9bHPraGrWDyGH6SXb58akC',  --> Encrypted password pB%zH!Mx7vR5@vrR8rkp^P
    'api-gateway',
    'client_secret_basic',
    'google_oid,refresh_token,client_credentials,password',
    'WORKER,ADMIN,USER'
);