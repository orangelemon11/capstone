CREATE TYPE user_role AS ENUM (
    'CUSTOMER',
    'AGENT',
    'ADMIN'
);

CREATE TYPE ticket_status AS ENUM (
    'OPEN',
    'IN_PROGRESS',
    'WAITING_FOR_CUSTOMER',
    'RESOLVED',
    'CLOSED'
);

CREATE TYPE ticket_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'URGENT'
);

CREATE TYPE notification_type AS ENUM (
    'TICKET_CREATED',
    'TICKET_ASSIGNED',
    'STATUS_CHANGED',
    'NEW_MESSAGE',
    'TICKET_RESOLVED'
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,

    role user_role NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    company_name VARCHAR(255),
    phone VARCHAR(50),

    CONSTRAINT fk_customer_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);


CREATE TABLE agents (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    department VARCHAR(100),

    is_available BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_agent_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);


CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,

    customer_id BIGINT NOT NULL,

    assigned_agent_id BIGINT,

    title VARCHAR(255) NOT NULL,

    description TEXT NOT NULL,

    status ticket_status NOT NULL DEFAULT 'OPEN',

    priority ticket_priority NOT NULL DEFAULT 'MEDIUM',

    category VARCHAR(100),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    closed_at TIMESTAMPTZ,

    CONSTRAINT fk_ticket_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_ticket_agent
        FOREIGN KEY (assigned_agent_id)
        REFERENCES agents(id)
        ON DELETE SET NULL
);

CREATE TABLE ticket_status_history (
    id BIGSERIAL PRIMARY KEY,

    ticket_id BIGINT NOT NULL,

    old_status ticket_status,

    new_status ticket_status NOT NULL,

    changed_by_user_id BIGINT NOT NULL,

    comment TEXT,

    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_status_history_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_status_history_user
        FOREIGN KEY (changed_by_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,

    ticket_id BIGINT NOT NULL,

    sender_user_id BIGINT NOT NULL,

    message TEXT NOT NULL,

    sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    is_read BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_chat_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_chat_sender
        FOREIGN KEY (sender_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    ticket_id BIGINT,

    type notification_type NOT NULL,

    title VARCHAR(255) NOT NULL,

    message TEXT NOT NULL,

    is_read BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_notification_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE CASCADE
);

CREATE TABLE service_history (
    id BIGSERIAL PRIMARY KEY,

    customer_id BIGINT NOT NULL,

    ticket_id BIGINT NOT NULL,

    agent_id BIGINT,

    service_type VARCHAR(100),

    description TEXT,

    completed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_service_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_service_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_service_agent
        FOREIGN KEY (agent_id)
        REFERENCES agents(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_users_email
    ON users(email);

CREATE INDEX idx_tickets_customer_id
    ON tickets(customer_id);

CREATE INDEX idx_tickets_assigned_agent_id
    ON tickets(assigned_agent_id);

CREATE INDEX idx_tickets_status
    ON tickets(status);

CREATE INDEX idx_tickets_priority
    ON tickets(priority);

CREATE INDEX idx_tickets_created_at
    ON tickets(created_at);

CREATE INDEX idx_ticket_status_history_ticket_id
    ON ticket_status_history(ticket_id);

CREATE INDEX idx_chat_messages_ticket_id
    ON chat_messages(ticket_id);

CREATE INDEX idx_chat_messages_sent_at
    ON chat_messages(sent_at);

CREATE INDEX idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX idx_notifications_is_read
    ON notifications(is_read);

CREATE INDEX idx_service_history_customer_id
    ON service_history(customer_id);


CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS 21687
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
21687 LANGUAGE plpgsql;


CREATE TRIGGER trigger_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


CREATE TRIGGER trigger_tickets_updated_at
BEFORE UPDATE ON tickets
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
