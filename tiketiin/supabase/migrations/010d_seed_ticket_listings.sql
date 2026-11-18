-- TIKETIIN DATABASE
-- Tahap 10D: Seed Ticket Listings

begin;

do $$
begin
    if not exists (
        select 1
        from public.profiles
        where email = 'seller@tiketiin.example'
        and is_kyc_verified = true
    ) then
        raise exception 'Selesaikan tahap 10C terlebih dahulu';
    end if;
end;
$$;

with seed (
    event_name,
    seat_category,
    seat_row,
    face_value,
    price,
    quantity
) as (
    values

    ('LALALA FESTIVAL 2027',
     'Regular', 'A', 750000, 950000, 8),

    ('LALALA FESTIVAL 2027',
     'VIP', 'B', 1500000, 1950000, 3),

    ('GONG YOO ASIA FANMEETING',
     'CAT 1', 'C', 1200000, 1500000, 5),

    ('GONG YOO ASIA FANMEETING',
     'VIP', 'A', 2000000, 2600000, 2),

    ('ROSETOPIA ASIA TOUR',
     'Festival', 'A', 1000000, 1300000, 6),

    ('ROSETOPIA ASIA TOUR',
     'VIP', 'B', 1750000, 2200000, 3),

    ('Raisa Handmade Night',
     'Regular', 'D', 500000, 650000, 10),

    ('LOMBA SIHIR: KONSER UJUNG TAHUN',
     'Festival', 'A', 350000, 420000, 12),

    ('Into the New World',
     'CAT 1', 'C', 600000, 750000, 5),

    ('Fun Walk 5K 2026',
     'Regular', '-', 150000, 180000, 15),

    ('Indonesia Golf Festival',
     'Regular', '-', 200000, 250000, 8),

    ('Rise & Bloom 2026: Midlife Renewal',
     'Regular', '-', 300000, 375000, 7),

    ('Cooltura Festival 2026',
     'Festival', 'A', 250000, 320000, 10),

    ('FISTERIA 14',
     'Festival', 'B', 200000, 260000, 8),

    ('OPUS FEST VOL 5',
     'Regular', 'A', 100000, 130000, 6)

)

insert into public.ticket_listings (
    event_id,
    seller_id,
    seat_category,
    seat_row,
    face_value,
    price,
    quantity,
    status
)

select
    e.id,
    p.id,
    s.seat_category,
    s.seat_row,
    s.face_value,
    s.price,
    s.quantity,
    'AKTIF'

from seed s

join public.events e
    on e.name = s.event_name
    and e.created_by is null

join public.profiles p
    on p.email = 'seller@tiketiin.example'

where e.date >= current_date
and p.is_kyc_verified = true

and not exists (
    select 1
    from public.ticket_listings l
    where l.event_id = e.id
      and l.seller_id = p.id
      and l.seat_category = s.seat_category
      and l.seat_row = s.seat_row
);

commit;
