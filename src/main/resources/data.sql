-- ============================================================
--  AndBank · Kata Exporter — Seed Data
--  H2 in-memory — se ejecuta al iniciar la app
-- ============================================================

-- ── Clientes ────────────────────────────────────────────────
INSERT INTO customers (id, first_name, last_name, email, cuit)
VALUES
    ('c001', 'Valentina', 'Morales',  'vmorales@mail.com',  '27-38291047-4'),
    ('c002', 'Rodrigo',   'Fernández','rfernandez@mail.com','20-29183746-2'),
    ('c003', 'Luciana',   'Gómez',    'lgomez@mail.com',    '27-40123456-1');

-- ── Cuentas ─────────────────────────────────────────────────
INSERT INTO accounts (id, alias, cbu, currency, balance, customer_id)
VALUES
    ('a001', 'morales.ahorro.pesos',  '0000003100074926130001', 'ARS', 485200.00, 'c001'),
    ('a002', 'morales.usd',           '0000003100074926130002', 'USD',   3200.50, 'c001'),
    ('a003', 'fernandez.cuenta',      '0000003100074926130003', 'ARS', 120000.00, 'c002'),
    ('a004', 'gomez.ahorro',          '0000003100074926130004', 'ARS', 930400.75, 'c003');

-- ── Movimientos 2024-01 (cuenta a001) ───────────────────────
INSERT INTO transactions (id, account_id, type, amount, currency, balance_after, description, reference, transaction_date)
VALUES
    ('t001','a001','CREDIT',  50000.00,'ARS', 550000.00,'Acreditación de sueldo',          'REF-SAL-2401','2024-01-05 09:00:00'),
    ('t002','a001','DEBIT',   12500.00,'ARS', 537500.00,'Pago servicio eléctrico',          'REF-SVC-2401','2024-01-08 11:30:00'),
    ('t003','a001','DEBIT',    3200.00,'ARS', 534300.00,'Supermercado Coto',                'REF-COM-2401','2024-01-12 18:45:00'),
    ('t004','a001','DEBIT',    8750.00,'ARS', 525550.00,'Expensas Edificio Belgrano',       'REF-EXP-2401','2024-01-15 10:00:00'),
    ('t005','a001','CREDIT',  10000.00,'ARS', 535550.00,'Transferencia recibida Fernández', 'REF-TRF-2401','2024-01-20 14:22:00'),
    ('t006','a001','DEBIT',    2300.00,'ARS', 533250.00,'Netflix subscription',             'REF-STR-2401','2024-01-22 00:00:00'),
    ('t007','a001','DEBIT',   45000.00,'ARS', 488250.00,'Alquiler Enero 2024',              'REF-ALQ-2401','2024-01-28 09:15:00');

-- ── Movimientos 2024-02 (cuenta a001) ───────────────────────
INSERT INTO transactions (id, account_id, type, amount, currency, balance_after, description, reference, transaction_date)
VALUES
    ('t008','a001','CREDIT',  50000.00,'ARS', 538250.00,'Acreditación de sueldo',          'REF-SAL-2402','2024-02-05 09:00:00'),
    ('t009','a001','DEBIT',    4500.00,'ARS', 533750.00,'Farmacity',                        'REF-FAR-2402','2024-02-09 16:00:00'),
    ('t010','a001','DEBIT',   15000.00,'ARS', 518750.00,'Pago tarjeta Visa Feb',            'REF-VIS-2402','2024-02-12 12:00:00'),
    ('t011','a001','DEBIT',    8750.00,'ARS', 510000.00,'Expensas Edificio Belgrano',       'REF-EXP-2402','2024-02-15 10:00:00'),
    ('t012','a001','CREDIT',  25000.00,'ARS', 535000.00,'Reintegro seguro médico',          'REF-SEG-2402','2024-02-19 11:30:00'),
    ('t013','a001','DEBIT',   45000.00,'ARS', 490000.00,'Alquiler Febrero 2024',            'REF-ALQ-2402','2024-02-28 09:15:00');

-- ── Movimientos 2024-03 (cuenta a001) ───────────────────────
INSERT INTO transactions (id, account_id, type, amount, currency, balance_after, description, reference, transaction_date)
VALUES
    ('t014','a001','CREDIT',  55000.00,'ARS', 545000.00,'Acreditación de sueldo + aumento','REF-SAL-2403','2024-03-05 09:00:00'),
    ('t015','a001','DEBIT',   20000.00,'ARS', 525000.00,'Pago tarjeta Visa Mar',            'REF-VIS-2403','2024-03-10 12:00:00'),
    ('t016','a001','DEBIT',    5600.00,'ARS', 519400.00,'Supermercado Carrefour',           'REF-COM-2403','2024-03-14 19:00:00'),
    ('t017','a001','DEBIT',    8750.00,'ARS', 510650.00,'Expensas Edificio Belgrano',       'REF-EXP-2403','2024-03-15 10:00:00'),
    ('t018','a001','DEBIT',    3500.00,'ARS', 507150.00,'Spotify + YouTube Premium',        'REF-STR-2403','2024-03-22 00:00:00'),
    ('t019','a001','DEBIT',   45000.00,'ARS', 462150.00,'Alquiler Marzo 2024',              'REF-ALQ-2403','2024-03-28 09:15:00'),
    ('t020','a001','CREDIT',  23050.00,'ARS', 485200.00,'Venta Mercado Libre',              'REF-ML-2403', '2024-03-30 16:45:00');

-- ── Movimientos cuenta USD (a002) ───────────────────────────
INSERT INTO transactions (id, account_id, type, amount, currency, balance_after, description, reference, transaction_date)
VALUES
    ('t021','a002','CREDIT',  2000.00,'USD', 2000.00,'Ingreso freelance USD',   'REF-FRL-2401','2024-01-10 10:00:00'),
    ('t022','a002','CREDIT',  1500.00,'USD', 3500.00,'Ingreso freelance USD',   'REF-FRL-2402','2024-02-10 10:00:00'),
    ('t023','a002','DEBIT',    299.50,'USD', 3200.50,'Renovación dominio + VPS','REF-WEB-2402','2024-02-20 12:00:00');

-- ── Movimientos cuenta Fernández (a003) ─────────────────────
INSERT INTO transactions (id, account_id, type, amount, currency, balance_after, description, reference, transaction_date)
VALUES
    ('t024','a003','CREDIT', 80000.00,'ARS', 80000.00,'Sueldo Febrero',          'REF-SAL-0001','2024-02-05 09:00:00'),
    ('t025','a003','DEBIT',  10000.00,'ARS', 70000.00,'Transferencia a Morales', 'REF-TRF-0001','2024-02-20 14:00:00'),
    ('t026','a003','CREDIT', 60000.00,'ARS',130000.00,'Sueldo Marzo',            'REF-SAL-0002','2024-03-05 09:00:00'),
    ('t027','a003','DEBIT',  10000.00,'ARS',120000.00,'Pago cuota préstamo',     'REF-PRE-0001','2024-03-18 11:00:00');
