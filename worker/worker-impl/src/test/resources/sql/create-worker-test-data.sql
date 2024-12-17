insert into public.worker_info (worker_id, description, phone, whatsapp, telegram, viber, country, city, registered_at)
values
    ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'best plumber', '+49111111111', 'plumber-w', 'plumber-t', 'plumber-v', 'DE', 'HH', '2024-12-14T15:00:00.111111Z'),
    ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'best electrician', '+49222222222', 'electrician-w', 'electrician-t', 'electrician-v', 'DE', 'M', '2024-12-13T15:00:00.111111Z'),
    ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 'best vocal-coach', '+49444444444', 'vocal-coach-w', 'vocal-coach-t', 'vocal-coach-v', 'DE', 'BE', '2024-12-12T15:00:00.111111Z'),
    ('57bc029c-d8e3-458f-b25a-7f73283cec98', 'not so good electrician', '+49555555555', 'meh-electrician-w', 'meh-electrician-t', 'meh-electrician-v', 'DE', 'BE', '2024-12-11T15:00:00.111111Z'),
    ('b007b62c-43cf-4ac3-b1e5-36fb9f1c0f52', 'not so good plumber', '+49666666666', 'meh-plumber-w', 'meh-plumber-t', 'meh-plumber-v', 'DE', 'BE', '2024-12-10T15:00:00.111111Z'),
    ('f2e91db9-4ceb-4231-bd4e-c898b441247d', 'fix toilets', '+49777777', 'plumber-w', 'plumber-t', 'plumber-v', 'DE', 'BE', '2024-12-09T15:00:00.111111Z'),
    ('dda832b4-b8e3-43df-a457-d77043b01751', 'fix sinks', '+498888888881', 'plumber-w', 'plumber-t', 'plumber-v', 'DE', 'BE', '2024-12-08T15:00:00.111111Z');

insert into public.worker_services (worker_id, service_id, amount)
values
    ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 10, 100),
    ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 10, 150),
    ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 20, 200),
    ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 10, 300),
    ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 30, 300),
    ('57bc029c-d8e3-458f-b25a-7f73283cec98', 40, 400),
    ('b007b62c-43cf-4ac3-b1e5-36fb9f1c0f52', 15, 500),
    ('f2e91db9-4ceb-4231-bd4e-c898b441247d', 11, 600),
    ('dda832b4-b8e3-43df-a457-d77043b01751', 12, 700);


insert into public.worker_languages (worker_id, language)
values
     ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'RU'),
     ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'DE'),
     ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'EN'),
     ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'UA'),
     ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 'DE'),
     ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 'EN'),
     ('57bc029c-d8e3-458f-b25a-7f73283cec98', 'DE'),
     ('b007b62c-43cf-4ac3-b1e5-36fb9f1c0f52', 'TR'),
     ('b007b62c-43cf-4ac3-b1e5-36fb9f1c0f52', 'DE'),
     ('f2e91db9-4ceb-4231-bd4e-c898b441247d', 'IT'),
     ('dda832b4-b8e3-43df-a457-d77043b01751', 'IT');


