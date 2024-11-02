-- Populating oauth2_authorization table
INSERT INTO oauth2_authorization (id, registered_client_id, principal_name, authorization_grant_type, authorized_scopes, principal)
VALUES
  ('550e8400-e29b-41d4-a716-446655440000', 'd9c235bc-73c2-42b0-b7b9-2ad0672e486e', '9e1cec67-1286-47c8-8956-18183583b7fd', 'password', '{}', '9e1cec67-1286-47c8-8956-18183583b7fd'),
  ('550e8400-e29b-41d4-a716-446655440001', 'd9c235bc-73c2-42b0-b7b9-2ad0672e486e', '9e1cec67-1286-47c8-8956-18183583b7fd', 'authorization_code', '{}', '9e1cec67-1286-47c8-8956-18183583b7fd'),
  ('550e8400-e29b-41d4-a716-446655440002', 'd9c235bc-73c2-42b0-b7b9-2ad0672e486e', '9e1cec67-1286-47c8-8956-18183583b7fd', 'client_credentials', '{}', '9e1cec67-1286-47c8-8956-18183583b7fd'),
  ('550e8400-e29b-41d4-a716-446655440003', 'd9c235bc-73c2-42b0-b7b9-2ad0672e486e', '9e1cec67-1286-47c8-8956-18183583b7fd', 'refresh_token', '{}', '9e1cec67-1286-47c8-8956-18183583b7fd'),
  ('550e8400-e29b-41d4-a716-446655440004', 'd9c235bc-73c2-42b0-b7b9-2ad0672e486e', '9e1cec67-1286-47c8-8956-18183583b7fd', 'implicit', '{}', '9e1cec67-1286-47c8-8956-18183583b7fd'),
  ('550e8400-e29b-41d4-a716-446655440005', 'd9c235bc-73c2-42b0-b7b9-2ad0672e486e', '9e1cec67-1286-47c8-8956-18183583b7fd', 'device_code', '{}', '9e1cec67-1286-47c8-8956-18183583b7fd'),
  ('550e8400-e29b-41d4-a716-446655440006', 'd9c235bc-73c2-42b0-b7b9-2ad0672e486e', '9e1cec67-1286-47c8-8956-18183583b7fd', 'authorization_code', '{}', '9e1cec67-1286-47c8-8956-18183583b7fd'),
  ('550e8400-e29b-41d4-a716-446655440007', 'd9c235bc-73c2-42b0-b7b9-2ad0672e486e', '9e1cec67-1286-47c8-8956-18183583b7fd', 'password', '{}', '9e1cec67-1286-47c8-8956-18183583b7fd'),
  ('550e8400-e29b-41d4-a716-446655440008', 'd9c235bc-73c2-42b0-b7b9-2ad0672e486e', '9e1cec67-1286-47c8-8956-18183583b7fd', 'client_credentials', '{}', '9e1cec67-1286-47c8-8956-18183583b7fd'),
  ('550e8400-e29b-41d4-a716-446655440009', 'd9c235bc-73c2-42b0-b7b9-2ad0672e486e', '9e1cec67-1286-47c8-8956-18183583b7fd', 'refresh_token', '{}', '9e1cec67-1286-47c8-8956-18183583b7fd');

-- Populating oauth2_token_pair table
INSERT INTO oauth2_token_pair (id, authorization_id, access_token_value, access_token_type, access_token_expires_at, access_token_metadata, refresh_token_value, refresh_token_expires_at, refresh_token_metadata, is_revoked, issued_at)
VALUES
  -- Token pairs for authorization 1
  ('550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440000', 'access-token-1', 'Bearer', '2024-10-31 19:00:00', '{}', 'refresh-token-1', NOW() - INTERVAL '1 DAY' , '{}', false, '2024-10-31 18:58:00'),
  ('550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440000', 'access-token-2', 'Bearer', '2024-10-31 19:00:00', '{}', 'refresh-token-2', NOW() - INTERVAL '2 DAY' , '{}', true, '2024-10-30 18:58:00'),

  -- Token pairs for authorization 2
  ('550e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440001', 'access-token-3', 'Bearer', '2024-10-31 19:00:00', '{}', 'refresh-token-3', NOW() + INTERVAL '1 DAY', '{}', false, '2024-10-31 18:58:00'),

  -- Token pairs for authorization 3
  ('550e8400-e29b-41d4-a716-446655440104', '550e8400-e29b-41d4-a716-446655440002', 'access-token-4', 'Bearer', '2024-10-30 19:00:00', '{}', 'refresh-token-4', NOW() - INTERVAL '1 DAY', '{}', false, '2024-10-31 18:58:00'),
  ('550e8400-e29b-41d4-a716-446655440105', '550e8400-e29b-41d4-a716-446655440002', 'access-token-5', 'Bearer', '2024-10-30 19:00:00', '{}', 'refresh-token-5', NOW() + INTERVAL '2 DAY', '{}', true, '2024-10-30 18:58:00'),

  -- Token pairs for authorization 4
  ('550e8400-e29b-41d4-a716-446655440106', '550e8400-e29b-41d4-a716-446655440003', 'access-token-6', 'Bearer', '2024-10-30 19:00:00', '{}', 'refresh-token-6', NOW() + INTERVAL '1 DAY', '{}', false, '2024-10-31 18:58:00'),
  ('550e8400-e29b-41d4-a716-446655440107', '550e8400-e29b-41d4-a716-446655440003', 'access-token-7', 'Bearer', '2024-10-30 19:00:00', '{}', 'refresh-token-7', NOW() + INTERVAL '1 DAY', '{}', true, '2024-10-30 18:58:00'),

  -- Token pairs for authorization 5
  ('550e8400-e29b-41d4-a716-446655440108', '550e8400-e29b-41d4-a716-446655440004', 'access-token-8', 'Bearer', '2024-10-31 19:00:00', '{}', 'refresh-token-8', NOW() - INTERVAL '1 DAY', '{}', false, '2024-10-31 18:58:00'),

  -- Token pairs for authorization 6
  ('550e8400-e29b-41d4-a716-446655440109', '550e8400-e29b-41d4-a716-446655440005', 'access-token-9', 'Bearer', '2024-10-31 19:00:00', '{}', 'refresh-token-9', NOW() + INTERVAL '1 DAY', '{}', false, '2024-10-31 18:58:00'),

  -- Token pairs for authorization 7
  ('550e8400-e29b-41d4-a716-446655440110', '550e8400-e29b-41d4-a716-446655440006', 'access-token-10', 'Bearer', '2024-10-31 19:00:00', '{}', 'refresh-token-10', NOW() - INTERVAL '1 DAY', '{}', false, '2024-10-31 18:58:00'),

  -- Token pairs for authorization 8
  ('550e8400-e29b-41d4-a716-446655440111', '550e8400-e29b-41d4-a716-446655440007', 'access-token-11', 'Bearer', '2024-10-31 19:00:00', '{}', 'refresh-token-11', NOW() + INTERVAL '1 DAY', '{}', false, '2024-10-31 18:58:00'),
  ('550e8400-e29b-41d4-a716-446655440112', '550e8400-e29b-41d4-a716-446655440007', 'access-token-12', 'Bearer', '2024-10-31 19:00:00', '{}', 'refresh-token-12', NOW() + INTERVAL '1 DAY', '{}', true, '2024-10-30 18:58:00'),

  -- Token pairs for authorization 9
  ('550e8400-e29b-41d4-a716-446655440113', '550e8400-e29b-41d4-a716-446655440008', 'access-token-13', 'Bearer', '2024-10-30 19:00:00', '{}', 'refresh-token-13', NOW() - INTERVAL '1 DAY', '{}', false, '2024-10-31 18:58:00'),
  ('550e8400-e29b-41d4-a716-446655440114', '550e8400-e29b-41d4-a716-446655440008', 'access-token-13', 'Bearer', '2024-10-30 19:00:00', '{}', 'refresh-token-13', NOW() - INTERVAL '1 DAY', '{}', true, '2024-10-30 18:58:00'),
  ('550e8400-e29b-41d4-a716-446655440115', '550e8400-e29b-41d4-a716-446655440008', 'access-token-13', 'Bearer', '2024-10-30 19:00:00', '{}', 'refresh-token-13', NOW() - INTERVAL '1 DAY', '{}', true, '2024-10-30 18:58:00'),


  -- Token pairs for authorization 10
  ('550e8400-e29b-41d4-a716-446655440116', '550e8400-e29b-41d4-a716-446655440009', 'access-token-14', 'Bearer', '2024-10-30 19:00:00', '{}', 'refresh-token-14', NOW() + INTERVAL '1 DAY', '{}', true, '2024-10-30 18:58:00');
