-- Add fields to store authorization code in oauth2_authorization table
ALTER TABLE public.oauth2_authorization
ADD COLUMN authorization_code_value TEXT,
ADD COLUMN authorization_code_issued_at TIMESTAMP,
ADD COLUMN authorization_code_expires_at TIMESTAMP,
ADD COLUMN authorization_code_metadata TEXT;