insert into public.chat(id, user_id, worker_id, chat_type, task_id, created_at)
values
    ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'bb2c6e16-2228-4ac1-8482-1f3548672b43', '57bc029c-d8e3-458f-b25a-7f73283cec98', 'TASK_SPECIFIC', 'd1c822c9-0ee4-462a-a88e-7c45e3bb0e54', NOW() - INTERVAL '1 DAY');

INSERT INTO public.chat_message (id, chat_id, message, created_by, created_at)
VALUES
    ('1a2b3c4d-1111-2222-3333-444444444444', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Hi, I saw your ad about dog walking services.', 'bb2c6e16-2228-4ac1-8482-1f3548672b43', '2025-01-12T10:15:00+00:00'),
    ('2b3c4d5e-5555-6666-7777-888888888888', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Hi! Yes, I offer dog-walking services. How can I help you?', '57bc029c-d8e3-458f-b25a-7f73283cec98', '2025-01-12T10:16:00+00:00'),
    ('3c4d5e6f-9999-aaaa-bbbb-cccccccccccc', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'I need someone to walk my Labrador twice a day while I’m at work.', 'bb2c6e16-2228-4ac1-8482-1f3548672b43', '2025-01-12T10:17:00+00:00'),
    ('4d5e6f7a-dddd-eeee-ffff-111111111111', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Sure! What times are you thinking for the walks?', '57bc029c-d8e3-458f-b25a-7f73283cec98', '2025-01-12T10:18:00+00:00'),
    ('5e6f7a8b-2222-3333-4444-555555555555', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Around 9 AM and 5 PM would be perfect.', 'bb2c6e16-2228-4ac1-8482-1f3548672b43', '2025-01-12T10:19:00+00:00'),
    ('6f7a8b9c-6666-7777-8888-999999999999', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Got it. How long would you like each walk to be?', '57bc029c-d8e3-458f-b25a-7f73283cec98', '2025-01-12T10:20:00+00:00'),
    ('7a8b9c0d-aaaa-bbbb-cccc-dddddddddddd', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'About 30 minutes per walk should be enough.', 'bb2c6e16-2228-4ac1-8482-1f3548672b43', '2025-01-12T10:21:00+00:00'),
    ('8b9c0d1e-eeee-ffff-1111-222222222222', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Great! I charge $15 per walk. Does that work for you?', '57bc029c-d8e3-458f-b25a-7f73283cec98', '2025-01-12T10:22:00+00:00'),
    ('9c0d1e2f-4444-5555-6666-777777777777', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Yes, that’s fine. Are you available to start tomorrow?', 'bb2c6e16-2228-4ac1-8482-1f3548672b43', '2025-01-12T10:23:00+00:00'),
    ('0d1e2f3a-8888-9999-aaaa-bbbbbbbbbbbb', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Yes, I can start tomorrow. What’s your address?', '57bc029c-d8e3-458f-b25a-7f73283cec98', '2025-01-12T10:24:00+00:00'),
    ('1e2f3a4b-cccc-dddd-eeee-ffffffffffff', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'It’s 123 Maple Street. Do you need anything else?', 'bb2c6e16-2228-4ac1-8482-1f3548672b43', '2025-01-12T10:25:00+00:00'),
    ('2f3a4b5c-1111-2222-3333-444444444444', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Nope, that’s all. I’ll be there at 9 AM. See you then!', '57bc029c-d8e3-458f-b25a-7f73283cec98', '2025-01-12T10:26:00+00:00');

-- Append per-message read status for 1:1 chat using message timestamps
-- Unread for worker: m1, m3
INSERT INTO public.message_reads (message_id, chat_id, recipient_id, read_at, created_at) VALUES
    ('1a2b3c4d-1111-2222-3333-444444444444', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', '57bc029c-d8e3-458f-b25a-7f73283cec98', NULL, '2025-01-12T10:15:00+00:00'),
    ('3c4d5e6f-9999-aaaa-bbbb-cccccccccccc', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', '57bc029c-d8e3-458f-b25a-7f73283cec98', NULL, '2025-01-12T10:17:00+00:00');
-- Unread for user: m2, m4
INSERT INTO public.message_reads (message_id, chat_id, recipient_id, read_at, created_at) VALUES
    ('2b3c4d5e-5555-6666-7777-888888888888', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'bb2c6e16-2228-4ac1-8482-1f3548672b43', NULL, '2025-01-12T10:16:00+00:00'),
    ('4d5e6f7a-dddd-eeee-ffff-111111111111', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'bb2c6e16-2228-4ac1-8482-1f3548672b43', NULL, '2025-01-12T10:18:00+00:00');
-- Already read examples for completeness
INSERT INTO public.message_reads (message_id, chat_id, recipient_id, read_at, created_at) VALUES
    ('5e6f7a8b-2222-3333-4444-555555555555', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', '57bc029c-d8e3-458f-b25a-7f73283cec98', '2025-01-12T10:30:00+00:00', '2025-01-12T10:19:00+00:00'),
    ('6f7a8b9c-6666-7777-8888-999999999999', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'bb2c6e16-2228-4ac1-8482-1f3548672b43', '2025-01-12T10:31:00+00:00', '2025-01-12T10:20:00+00:00');
