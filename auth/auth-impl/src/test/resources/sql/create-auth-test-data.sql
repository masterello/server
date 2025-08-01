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

INSERT INTO public.oauth2_authorization (
    id,
    registered_client_id,
    principal_name,
    authorization_grant_type,
    authorized_scopes,
    principal,
    authorization_code_value,
    authorization_code_issued_at,
    authorization_code_expires_at,
    authorization_code_metadata
) VALUES (
    '01f5ab10-5af0-4646-9aef-a2ebc349ee22',
    'd9c235bc-73c2-42b0-b7b9-2ad0672e486e',
    '49200ea0-3879-11ee-be56-0242ac120002',
    'google_auth_code',
    NULL,
    '49200ea0-3879-11ee-be56-0242ac120002',
    'XQ40Jy1ERz9QGWG2Lhl3CpjPfSLZ15FQyPCTafsL2dXxwZK3YVnVyZ-kW2Ov8t_riBjR_QUHX8QYmOHCo0Ica0bzUW68mf29c5yshIZCFrkGjwELHgE-HZarznEPBang',
    NOW(),
    NOW() + INTERVAL '5 MINUTES',
    '{"@class":"java.util.Collections$UnmodifiableMap","metadata.token.invalidated":false}'
),
(
    'abda1cb3-885e-40e7-8dfc-50d1a468ac6a',
    'd9c235bc-73c2-42b0-b7b9-2ad0672e486e',
    '49200ea0-3879-11ee-be56-0242ac120002',
    'google_auth_code',
    NULL,
    '49200ea0-3879-11ee-be56-0242ac120002',
    'QqqqQqqqz9QGWG2Lhl3CpjPfSLZ15FQyPCTafsL2dXxwZK3YVnVyZ-kW2Ov8t_riBjR_QUHX8QYmOHCo0Ica0bzUW68mf29c5yshIZCFrkGjwELHgE-HZarznEPBig',
    NOW(),
    NOW() + INTERVAL '5 MINUTES',
    '{"@class":"java.util.Collections$UnmodifiableMap","metadata.token.invalidated":true}'
);

--INSERT INTO public.oauth2_token_pair (
--    id,
--    authorization_id,
--    access_token_value,
--    access_token_type,
--    access_token_expires_at,
--    access_token_metadata,
--    access_token_scopes,
--    refresh_token_value,
--    refresh_token_expires_at,
--    refresh_token_metadata,
--    is_revoked,
--    issued_at
--) VALUES (
--    'eafd5c49-4f37-4d02-b883-5830c99c7850',
--    'abda1cb3-885e-40e7-8dfc-50d1a468ac6a',
--    'BcphAyte-kmt07oAAIuTSt73m1FX9oGBGH8KctiwSnZzgyFmHTJyBACJ97MZdJIZyJ5dvs7YYoQVYKskiUfmsKD8Gs0q7BN3ae7U9MxlyuffCp_ydNht7KsKxsjELUxV',
--    'Bearer',
--    NOW() + INTERVAL '5 MINUTES',
--    '{"@class":"java.util.Collections$UnmodifiableMap","metadata.token.claims":{"@class":"java.util.Collections$UnmodifiableMap","sub":"4ab7cd73-0186-4dd0-9918-947eee9a3773","aud":["java.util.Collections$SingletonList",["bff"]],"emailVerified":false,"nbf":["java.time.Instant",1749299814.792283],"userStatus":"ACTIVE","roles":["java.util.HashSet",["USER"]],"iss":["java.net.URL","http://127.0.0.1:8090"],"exp":["java.time.Instant",1749300114.792283],"iat":["java.time.Instant",1749299814.792283],"userId":"4ab7cd73-0186-4dd0-9918-947eee9a3773","jti":"3f5f6de7-28c2-444a-b625-e53866df9d18","username":"dmitry.borodin90@gmail.com"},"metadata.token.invalidated":false}',
--    NULL,
--    'ns1LjbwhMWwo8Q0wnUJebykMOP8EdM9K5aq-JOngYGOdjyP-4jyyE1IyA2LECaE5Bv07YF90d-ujqNqOszDLY6Zg7o05B98ON0OZBuzp7CAoWNNfpmlxegbMZb4lcmW7',
--    NOW() + INTERVAL '1 DAY',
--    '{"@class":"java.util.Collections$UnmodifiableMap","metadata.token.invalidated":false}',
--    false,
--    NOW() - INTERVAL '5 MINUTES'
--);
