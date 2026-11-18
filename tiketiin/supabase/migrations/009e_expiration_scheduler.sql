-- TIKETIIN DATABASE
-- Tahap 09E: Expiration Scheduler

create extension if not exists pg_cron
with schema pg_catalog;

select cron.schedule(
    'tiketiin-expire-orders',
    '* * * * *',
    'select public.expire_orders();'
);
