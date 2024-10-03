insert into public.users (uuid, name, lastname, title, email, phone, password, city, status, email_verified)
values
    ('bb2c6e16-2228-4ac1-8482-1f3548672b43', 'user', 'userson', 'Mr.', 'user@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, true),
    ('455aee9c-9629-466f-bfc5-8d956da74769', 'admin', 'adminson', 'Mr.', 'admin@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, true),
    ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'worker1', 'workerson', 'Mr.', 'worker1@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, true),
    ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'worker2', 'workerson', 'Herr', 'worker2@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, true),
    ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 'worker3', 'workerson', 'Ms.', 'worker3@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, true),
    ('57bc029c-d8e3-458f-b25a-7f73283cec98', 'worker4', 'workerson', 'Frau', 'worker4@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, true),
    ('b007b62c-43cf-4ac3-b1e5-36fb9f1c0f52', 'worker5', 'workerson', null, 'worker5@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, true),
    ('8824a15c-98f5-49d9-bd97-43d1cba3f62c', 'worker6', 'workerson', null, 'worker6@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, true),
    ('f2e91db9-4ceb-4231-bd4e-c898b441247d', 'worker7', 'workerson', 'Mr.', 'worker7@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, true),
    ('dda832b4-b8e3-43df-a457-d77043b01751', 'worker8', 'workerson', 'Mr.', 'worker8@gmail.com', '1234567', 'aaabbb123', 'berlin', 0, true);


insert into public.user_roles (user_id, role)
values
    ('bb2c6e16-2228-4ac1-8482-1f3548672b43', 'USER'),
    ('455aee9c-9629-466f-bfc5-8d956da74769', 'USER'),
    ('455aee9c-9629-466f-bfc5-8d956da74769', 'ADMIN'),
    ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'USER'),
    ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'WORKER'),
    ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'USER'),
    ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'WORKER'),
    ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 'USER'),
    ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 'WORKER'),
    ('57bc029c-d8e3-458f-b25a-7f73283cec98', 'USER'),
    ('57bc029c-d8e3-458f-b25a-7f73283cec98', 'WORKER'),
    ('b007b62c-43cf-4ac3-b1e5-36fb9f1c0f52', 'USER'),
    ('b007b62c-43cf-4ac3-b1e5-36fb9f1c0f52', 'WORKER'),
    ('8824a15c-98f5-49d9-bd97-43d1cba3f62c', 'USER'),
    ('8824a15c-98f5-49d9-bd97-43d1cba3f62c', 'WORKER'),
    ('f2e91db9-4ceb-4231-bd4e-c898b441247d', 'USER'),
    ('f2e91db9-4ceb-4231-bd4e-c898b441247d', 'WORKER'),
    ('dda832b4-b8e3-43df-a457-d77043b01751', 'USER'),
    ('dda832b4-b8e3-43df-a457-d77043b01751', 'WORKER');


insert into public.worker_info (worker_id, description, phone, whatsapp, telegram, viber)
values
    ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'best plumber', '+49111111111', 'plumber-w', 'plumber-t', 'plumber-v'),
    ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'best electrician', '+49222222222', 'electrician-w', 'electrician-t', 'electrician-v'),
    ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 'best vocal-coach', '+49444444444', 'vocal-coach-w', 'vocal-coach-t', 'vocal-coach-v'),
    ('57bc029c-d8e3-458f-b25a-7f73283cec98', 'not so good electrician', '+49555555555', 'meh-electrician-w', 'meh-electrician-t', 'meh-electrician-v'),
    ('b007b62c-43cf-4ac3-b1e5-36fb9f1c0f52', 'not so good plumber', '+49666666666', 'meh-plumber-w', 'meh-plumber-t', 'meh-plumber-v'),
    ('f2e91db9-4ceb-4231-bd4e-c898b441247d', 'fix toilets', '+49777777', 'plumber-w', 'plumber-t', 'plumber-v'),
    ('dda832b4-b8e3-43df-a457-d77043b01751', 'fix sinks', '+498888888881', 'plumber-w', 'plumber-t', 'plumber-v');

insert into public.worker_services (worker_id, service_id, amount)
values
    ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 10, 100),
    ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 10, 150),
    ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 20, 200),
    ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 10, 300),
    ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 30, 300),
    ('57bc029c-d8e3-458f-b25a-7f73283cec98', 40, 400),
    ('b007b62c-43cf-4ac3-b1e5-36fb9f1c0f52', 10, 500),
    ('f2e91db9-4ceb-4231-bd4e-c898b441247d', 11, 600),
    ('dda832b4-b8e3-43df-a457-d77043b01751', 12, 700);


insert into public.user_languages (user_id, language)
values
     ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'RU'),
     ('e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'DE'),
     ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'EN'),
     ('e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'UA'),
     ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 'DE'),
     ('d1c822c9-0ee4-462a-a88e-7c45e3bb0e54', 'EN'),
     ('57bc029c-d8e3-458f-b25a-7f73283cec98', 'DE'),
     ('bb2c6e16-2228-4ac1-8482-1f3548672b43', 'DE'),
     ('8824a15c-98f5-49d9-bd97-43d1cba3f62c', 'EN'),
     ('f2e91db9-4ceb-4231-bd4e-c898b441247d', 'IT'),
     ('dda832b4-b8e3-43df-a457-d77043b01751', 'IT');


