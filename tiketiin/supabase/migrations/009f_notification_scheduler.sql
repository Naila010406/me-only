-- TIKETIIN DATABASE
-- Tahap 09F: Notification Scheduler

select cron.schedule(
    'tiketiin-event-reminders',
    '0 * * * *',
    'select public.send_event_reminders();'
);
