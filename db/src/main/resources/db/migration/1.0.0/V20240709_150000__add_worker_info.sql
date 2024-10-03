CREATE TABLE worker_info (
   worker_id UUID PRIMARY KEY,
   description TEXT,
   phone VARCHAR(50),
   telegram VARCHAR(50),
   whatsapp VARCHAR(50),
   viber VARCHAR(50)
);

CREATE TABLE worker_services (
   worker_id UUID,
   service_id integer,
   amount integer,
   PRIMARY KEY (worker_id, service_id),
   FOREIGN KEY (worker_id) REFERENCES worker_info(worker_id)
);