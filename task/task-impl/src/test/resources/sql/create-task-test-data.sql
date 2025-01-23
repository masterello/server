insert into public.task (uuid, user_uuid, worker_uuid, name, description, category_code, status, created_date, updated_date)
values
    ('a45fb214-7c41-4a3d-a990-b499577d46c0', '0c018736-49e5-4611-8722-d2ecd0567fb1', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Repair the door', 'Remove old door and put new one', 1, 1, now(), now()),
    ('a45fb214-7c41-4a3d-a990-b499577d46c1', '0c018736-49e5-4611-8722-d2ecd0567fb1', null, 'Cleaning', 'Clean apartment', 2, 0, now(), now()),
    ('a45fb214-7c41-4a3d-a990-b499577d46c2', '0c018736-49e5-4611-8722-d2ecd0567fb1', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Baby Sitting', 'Watch the baby', 3, 4, now(), now()),
    ('a45fb214-7c41-4a3d-a990-b499577d46c3', '0c018736-49e5-4611-8722-d2ecd0567fb1', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Baby Sitting 2', 'Watch the baby 2', 3, 5, now(), now()),
    ('a45fb214-7c41-4a3d-a990-b499577d46c4', '0c018736-49e5-4611-8722-d2ecd0567fb1', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 'Baby Sitting 2', 'Watch the baby 2', 3, 3, now(), now()),
    ('a45fb214-7c41-4a3d-a990-b499577d46c5', '0c018736-49e5-4611-8722-d2ecd0567fb1', 'e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'Repair window', 'Remove window', 1, 2, now(), now()),
    ('a99fb214-7c41-4a3d-a990-b499577d46c5', '0c018736-49e5-4611-8722-d2ecd0567fb1', 'e4de38bf-168e-41fc-b7b1-b9d74a47529e', 'Repair window', 'Remove window', 1, 3, now(), now());

insert into public.task_review(uuid, task_uuid, reviewer_uuid, reviewer_type, review, created_date, updated_date)
values
    ('b45fb214-7c41-4a3d-a990-b499577d46c0', 'a45fb214-7c41-4a3d-a990-b499577d46c3', '0c018736-49e5-4611-8722-d2ecd0567fb1', 0, 'Good work', now(), now()),
    ('b45fb214-7c41-4a3d-a990-b499577d46c1', 'a45fb214-7c41-4a3d-a990-b499577d46c3', 'e5fcf8dd-b6be-4a36-a85a-e2d952cc6254', 1, 'Good task', now(), now()),
    ('b45fb214-7c41-4a3d-a990-b499577d46c2', 'a45fb214-7c41-4a3d-a990-b499577d46c3', 'e4de38bf-168e-41fc-b7b1-b9d74a47529e', 1, 'User reassigned task from me!', now(), now()),
    ('b45fb214-7c41-4a3d-a990-b499577d46c3', 'a99fb214-7c41-4a3d-a990-b499577d46c5', '0c018736-49e5-4611-8722-d2ecd0567fb1', 1, 'Normal', now(), now());

insert into public.worker_rating(uuid, task_uuid, rating, created_date, updated_date)
values
    ('c45fb214-7c41-4a3d-a990-b499577d46c0', 'a45fb214-7c41-4a3d-a990-b499577d46c3', 1, now(), now()),
    ('c45fb214-7c41-4a3d-a990-b499577d46c1', 'a45fb214-7c41-4a3d-a990-b499577d46c3', 5, now(), now());

 insert into public.user_rating(uuid, task_uuid, rating, created_date, updated_date)
 values
    ('d45fb214-7c41-4a3d-a990-b499577d46c0', 'a45fb214-7c41-4a3d-a990-b499577d46c3', 5, now(), now()),
    ('d45fb214-7c41-4a3d-a990-b499577d46c1', 'a99fb214-7c41-4a3d-a990-b499577d46c5', 4, now(), now());