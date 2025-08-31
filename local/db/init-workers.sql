-- Local setup script to seed ~15 worker accounts with required data
-- Tables referenced: public.users, public.user_roles, public.worker_info, public.worker_languages, public.worker_location_cities, public.worker_services
-- Password for all users is the provided bcrypt for "Password!1"
-- Adjust emails/phones if they collide with existing local data.

BEGIN;

-- Shared password hash ("Password!1")
-- $2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa

-- Users
INSERT INTO public.users (uuid, email, password, title, name, lastname, phone, country, city, status, email_verified)
VALUES
    ('a1111111-1111-4111-8111-111111111111', 'worker01@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Mr.',  'Alex',   'Muster',  '+49-151-00000001', 'DE', 'BE', 0, true),
    ('a2222222-2222-4222-8222-222222222222', 'worker02@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Ms.',  'Bianca', 'Beispiel','+49-151-00000002', 'DE', 'HH', 0, true),
    ('a3333333-3333-4333-8333-333333333333', 'worker03@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', NULL,   'Chris',  'Schmidt', '+49-151-00000003', 'DE', 'M',  0, true),
    ('a4444444-4444-4444-8444-444444444444', 'worker04@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Mr.',  'Daria',  'Klein',   '+49-151-00000004', 'DE', 'F',  0, true),
    ('a5555555-5555-4555-8555-555555555555', 'worker05@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Ms.',  'Egon',   'Huber',   '+49-151-00000005', 'DE', 'D',  0, true),
    ('a6666666-6666-4666-8666-666666666666', 'worker06@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', NULL,   'Fiona',  'Keller',  '+49-151-00000006', 'DE', 'S',  0, true),
    ('a7777777-7777-4777-8777-777777777777', 'worker07@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Mr.',  'Gerd',   'Meier',   '+49-151-00000007', 'DE', 'L',  0, true),
    ('a8888888-8888-4888-8888-888888888888', 'worker08@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Mr.',  'Hana',   'Weiss',   '+49-151-00000008', 'DE', 'DO', 0, true),
    ('a9999999-9999-4999-8999-999999999999', 'worker09@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Ms.',  'Ivan',   'Fischer', '+49-151-00000009', 'DE', 'HB', 0, true),
    ('b1111111-1111-4111-8111-111111111111', 'worker10@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', NULL,   'Julia',  'Krause',  '+49-151-00000010', 'DE', 'E',  0, true),
    ('b2222222-2222-4222-8222-222222222222', 'worker11@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Mr.',  'Karl',   'König',   '+49-151-00000011', 'DE', 'DD', 0, true),
    ('b3333333-3333-4333-8333-333333333333', 'worker12@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Ms.',  'Lena',   'Wolf',    '+49-151-00000012', 'DE', 'N',  0, true),
    ('b4444444-4444-4444-8444-444444444444', 'worker13@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', NULL,   'Max',    'Neumann', '+49-151-00000013', 'DE', 'H',  0, true),
    ('b5555555-5555-4555-8555-555555555555', 'worker14@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Mr.',  'Nina',   'Schulz',  '+49-151-00000014', 'DE', 'DU', 0, true),
    ('b6666666-6666-4666-8666-666666666666', 'worker15@local.test', '$2a$10$QkaP94CmAsmzIUnzYys52OmN8xfZnjbjOnfNPwoMZvgDNKmbgqxwa', 'Ms.',  'Omar',   'Hart',    '+49-151-00000015', 'DE', 'W',  0, true)
ON CONFLICT (uuid) DO NOTHING;

-- Roles
INSERT INTO public.user_roles (user_id, role)
SELECT uuid, 'WORKER' FROM public.users u
WHERE u.uuid IN (
    'a1111111-1111-4111-8111-111111111111','a2222222-2222-4222-8222-222222222222','a3333333-3333-4333-8333-333333333333',
    'a4444444-4444-4444-8444-444444444444','a5555555-5555-4555-8555-555555555555','a6666666-6666-4666-8666-666666666666',
    'a7777777-7777-4777-8777-777777777777','a8888888-8888-4888-8888-888888888888','a9999999-9999-4999-8999-999999999999',
    'b1111111-1111-4111-8111-111111111111','b2222222-2222-4222-8222-222222222222','b3333333-3333-4333-8333-333333333333',
    'b4444444-4444-4444-8444-444444444444','b5555555-5555-4555-8555-555555555555','b6666666-6666-4666-8666-666666666666'
)
ON CONFLICT DO NOTHING;

-- Worker info (basic contact + location + flags)
INSERT INTO public.worker_info (worker_id, description, phone, telegram, whatsapp, viber, country, city, registered_at, active, online)
VALUES
    ('a1111111-1111-4111-8111-111111111111','Plumber - Berlin','+49-151-00000001','alex-plumber','alex-whatsapp','alex-viber','DE','BE', NOW() - INTERVAL '15 days', true, false),
    ('a2222222-2222-4222-8222-222222222222','Electrician - Hamburg','+49-151-00000002','bianca-electric','bianca-whatsapp','bianca-viber','DE','HH', NOW() - INTERVAL '14 days', true, true),
    ('a3333333-3333-4333-8333-333333333333','Tutor - Munich','+49-151-00000003','chris-tutor','chris-whatsapp','chris-viber','DE','M', NOW() - INTERVAL '13 days', true, false),
    ('a4444444-4444-4444-8444-444444444444','Hairdresser - Frankfurt','+49-151-00000004','daria-hair','daria-whatsapp','daria-viber','DE','F', NOW() - INTERVAL '12 days', true, false),
    ('a5555555-5555-4555-8555-555555555555','Carpenter - Düsseldorf','+49-151-00000005','egon-carp','egon-whatsapp','egon-viber','DE','D', NOW() - INTERVAL '11 days', true, false),
    ('a6666666-6666-4666-8666-666666666666','Cleaner - Stuttgart','+49-151-00000006','fiona-clean','fiona-whatsapp','fiona-viber','DE','S', NOW() - INTERVAL '10 days', true, false),
    ('a7777777-7777-4777-8777-777777777777','Photographer - Leipzig','+49-151-00000007','gerd-photo','gerd-whatsapp','gerd-viber','DE','L', NOW() - INTERVAL '9 days', true, false),
    ('a8888888-8888-4888-8888-888888888888','Coach - Dortmund','+49-151-00000008','hana-coach','hana-whatsapp','hana-viber','DE','DO', NOW() - INTERVAL '8 days', true, true),
    ('a9999999-9999-4999-8999-999999999999','Designer - Bremen','+49-151-00000009','ivan-design','ivan-whatsapp','ivan-viber','DE','HB', NOW() - INTERVAL '7 days', true, false),
    ('b1111111-1111-4111-8111-111111111111','Painter - Essen','+49-151-00000010','julia-paint','julia-whatsapp','julia-viber','DE','E', NOW() - INTERVAL '6 days', true, false),
    ('b2222222-2222-4222-8222-222222222222','Mechanic - Dresden','+49-151-00000011','karl-mech','karl-whatsapp','karl-viber','DE','DD', NOW() - INTERVAL '5 days', true, false),
    ('b3333333-3333-4333-8333-333333333333','Gardener - Nürnberg','+49-151-00000012','lena-garden','lena-whatsapp','lena-viber','DE','N', NOW() - INTERVAL '4 days', true, false),
    ('b4444444-4444-4444-8444-444444444444','Handyman - Hannover','+49-151-00000013','max-hand','max-whatsapp','max-viber','DE','H', NOW() - INTERVAL '3 days', true, false),
    ('b5555555-5555-4555-8555-555555555555','Mover - Duisburg','+49-151-00000014','nina-move','nina-whatsapp','nina-viber','DE','DU', NOW() - INTERVAL '2 days', true, false),
    ('b6666666-6666-4666-8666-666666666666','Tutor - Wuppertal','+49-151-00000015','omar-tutor','omar-whatsapp','omar-viber','DE','W', NOW() - INTERVAL '1 days', true, true)
ON CONFLICT (worker_id) DO NOTHING;

-- Optional: location cities (keeps existing city as one of multi-cities)
INSERT INTO public.worker_location_cities (worker_id, city)
SELECT worker_id, city FROM public.worker_info wi
WHERE wi.worker_id IN (
    'a1111111-1111-4111-8111-111111111111','a2222222-2222-4222-8222-222222222222','a3333333-3333-4333-8333-333333333333',
    'a4444444-4444-4444-8444-444444444444','a5555555-5555-4555-8555-555555555555','a6666666-6666-4666-8666-666666666666',
    'a7777777-7777-4777-8777-777777777777','a8888888-8888-4888-8888-888888888888','a9999999-9999-4999-8999-999999999999',
    'b1111111-1111-4111-8111-111111111111','b2222222-2222-4222-8222-222222222222','b3333333-3333-4333-8333-333333333333',
    'b4444444-4444-4444-8444-444444444444','b5555555-5555-4555-8555-555555555555','b6666666-6666-4666-8666-666666666666'
)
ON CONFLICT DO NOTHING;

-- Languages (at least one per worker)
INSERT INTO public.worker_languages (worker_id, language)
VALUES
    ('a1111111-1111-4111-8111-111111111111','DE'),
    ('a2222222-2222-4222-8222-222222222222','EN'),
    ('a3333333-3333-4333-8333-333333333333','DE'),
    ('a4444444-4444-4444-8444-444444444444','EN'),
    ('a5555555-5555-4555-8555-555555555555','DE'),
    ('a6666666-6666-4666-8666-666666666666','EN'),
    ('a7777777-7777-4777-8777-777777777777','DE'),
    ('a8888888-8888-4888-8888-888888888888','EN'),
    ('a9999999-9999-4999-8999-999999999999','DE'),
    ('b1111111-1111-4111-8111-111111111111','EN'),
    ('b2222222-2222-4222-8222-222222222222','DE'),
    ('b3333333-3333-4333-8333-333333333333','EN'),
    ('b4444444-4444-4444-8444-444444444444','DE'),
    ('b5555555-5555-4555-8555-555555555555','EN'),
    ('b6666666-6666-4666-8666-666666666666','DE')
ON CONFLICT DO NOTHING;

-- Services: ensure each worker has at least one service
-- Note: Adjust service_id values to valid ones in your local DB if necessary
INSERT INTO public.worker_services (worker_id, service_id, amount, details)
VALUES
    ('a1111111-1111-4111-8111-111111111111', 10, 100, 'Base rate'),
    ('a2222222-2222-4222-8222-222222222222', 11, 110, 'Standard'),
    ('a3333333-3333-4333-8333-333333333333', 12, 120, 'Standard'),
    ('a4444444-4444-4444-8444-444444444444', 13, 130, 'Standard'),
    ('a5555555-5555-4555-8555-555555555555', 14, 140, 'Standard'),
    ('a6666666-6666-4666-8666-666666666666', 15, 150, 'Standard'),
    ('a7777777-7777-4777-8777-777777777777', 16, 160, 'Standard'),
    ('a8888888-8888-4888-8888-888888888888', 17, 170, 'Standard'),
    ('a9999999-9999-4999-8999-999999999999', 18, 180, 'Standard'),
    ('b1111111-1111-4111-8111-111111111111', 19, 190, 'Standard'),
    ('b2222222-2222-4222-8222-222222222222', 20, 200, 'Standard'),
    ('b3333333-3333-4333-8333-333333333333', 21, 210, 'Standard'),
    ('b4444444-4444-4444-8444-444444444444', 22, 220, 'Standard'),
    ('b5555555-5555-4555-8555-555555555555', 23, 230, 'Standard'),
    ('b6666666-6666-4666-8666-666666666666', 24, 240, 'Standard')
ON CONFLICT DO NOTHING;

COMMIT;

