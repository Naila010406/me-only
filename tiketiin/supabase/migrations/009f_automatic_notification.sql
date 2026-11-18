-- TIKETIIN DATABASE
-- Tahap 09F: Automatic Notification

begin;

-- 1. Penanda pengingat yang sudah dikirim
alter table public.wishlists
add column if not exists
event_reminder_sent_at timestamptz;


-- 2. Notifikasi ketika harga tiket turun
create or replace function public.notify_price_drop()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin

    if new.price < old.price
       and new.status = 'AKTIF'
       and new.quantity > 0
       and exists (
           select 1
           from public.events e
           where e.id = new.event_id
             and e.date >= current_date
       ) then

        insert into public.notifications (
            user_id,
            title,
            message,
            type,
            event_id
        )
        select
            w.user_id,
            'Harga Tiket Turun!',
            'Harga tiket event favoritmu turun dari Rp'
            || old.price::text
            || ' menjadi Rp'
            || new.price::text,
            'PRICE_ALERT',
            new.event_id
        from public.wishlists w
        where w.event_id = new.event_id
          and w.user_id <> new.seller_id;

    end if;

    return new;

end;
$$;


-- 3. Trigger penurunan harga
create trigger on_ticket_price_drop
after update of price
on public.ticket_listings
for each row
execute function public.notify_price_drop();


-- 4. Fungsi pengingat event H-1
create or replace function public.send_event_reminders()
returns integer
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_count integer;
begin

    with marked as (
        update public.wishlists w
        set event_reminder_sent_at = now()
        from public.events e
        where w.event_id = e.id
          and e.date =
              (now() at time zone 'Asia/Jakarta')::date + 1
          and w.event_reminder_sent_at is null
        returning
            w.user_id,
            w.event_id
    )

    insert into public.notifications (
        user_id,
        title,
        message,
        type,
        event_id
    )
    select
        m.user_id,
        'Event Dimulai Besok!',
        e.name || ' akan berlangsung besok.',
        'EVENT_REMINDER',
        m.event_id
    from marked m
    join public.events e
        on e.id = m.event_id;

    get diagnostics v_count = row_count;

    return v_count;

end;
$$;


-- 5. Pengaturan keamanan
revoke execute on function
public.notify_price_drop()
from public, anon, authenticated, service_role;

revoke execute on function
public.send_event_reminders()
from public, anon, authenticated, service_role;

grant execute on function
public.send_event_reminders()
to postgres;

commit;
