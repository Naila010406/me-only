-- TIKETIIN DATABASE
-- Tahap 10A: Seed Venues and Events

begin;

insert into public.venues
(name, city, latitude, longitude)
values
('ICE BSD City', 'Tangerang', -6.3003, 106.6362),
('Istora Senayan', 'Jakarta', -6.2206, 106.8022),
('The Kasablanka', 'Jakarta Selatan', -6.2241, 106.8432),
('Balai Sarbini', 'Jakarta', -6.2146, 106.8206),
('M Bloc Space', 'Jakarta Pusat', -6.2416, 106.7996),
('Ciputra Artpreneur', 'Jakarta Selatan', -6.2238, 106.8239),
('Wisma 46', 'Jakarta', -6.2034, 106.8198),
('Half Patiunus', 'Jakarta Selatan', -6.2297, 106.8085),
('Teater Jakarta', 'Jakarta Pusat', -6.1899, 106.8389),
('Graha Cakrawala', 'Malang', -7.9609, 112.6189),
('Lapangan Rampal', 'Malang', -7.9722, 112.6410),
('Gedung Kesenian Gajayana', 'Malang', -7.9826, 112.6304),
('Samantha Krida', 'Malang', -7.9527, 112.6140)
on conflict (name, city) do nothing;


with seed (
    name, category, venue_name, city,
    days_offset, event_time, description,
    poster_start, poster_end, flash_hours,
    base_sold, base_wishlist, view_count,
    views_last_7_days
) as (
    values
    (
        'LALALA FESTIVAL 2027', 'MUSIK',
        'ICE BSD City', 'Tangerang',
        62, '14.30 - 23.00',
        'Festival musik tiga hari dengan musisi internasional dan lokal.',
        4278917965, 4286263287, null,
        240, 96, 1650, 54
    ),
    (
        'GONG YOO ASIA FANMEETING', 'MUSIK',
        'Istora Senayan', 'Jakarta',
        12, '19.00 WIB',
        'Fanmeeting tur Asia dengan sesi bincang dan permainan.',
        4279308561, 4283058762, 8,
        180, 88, 1800, 61
    ),
    (
        'ROSETOPIA ASIA TOUR', 'MUSIK',
        'The Kasablanka', 'Jakarta Selatan',
        18, '19.30 WIB',
        'Konser tur Asia dengan panggung bertema taman mawar.',
        4293485689, 4294428832, 120,
        150, 70, 1200, 40
    ),
    (
        'Raisa Handmade Night', 'MUSIK',
        'Balai Sarbini', 'Jakarta',
        30, '20.00 WIB',
        'Malam intim bersama Raisa dengan aransemen akustik.',
        4292429934, 4294423236, 72,
        90, 41, 760, 22
    ),
    (
        'LOMBA SIHIR: KONSER UJUNG TAHUN', 'MUSIK',
        'M Bloc Space', 'Jakarta Pusat',
        35, '19.00 WIB',
        'Konser penutup tahun Lomba Sihir.',
        4279900718, 4293477728, null,
        60, 30, 540, 18
    ),
    (
        'Into the New World', 'SENI',
        'Ciputra Artpreneur', 'Jakarta Selatan',
        21, '16.00 WIB',
        'Pertunjukan orkestra simfoni.',
        4280250192, 4294099278, null,
        35, 22, 410, 12
    ),
    (
        'Fun Walk 5K 2026', 'OLAHRAGA',
        'Wisma 46', 'Jakarta',
        15, '06.00 WIB',
        'Jalan santai 5K bersama keluarga.',
        4279858898, 4284790262, null,
        48, 15, 380, 10
    ),
    (
        'Indonesia Golf Festival', 'OLAHRAGA',
        'ICE BSD City', 'Tangerang',
        8, '09.00 WIB',
        'Pameran dan festival golf.',
        4281236786, 4289058471, null,
        25, 9, 260, 7
    ),
    (
        'Rise & Bloom 2026: Midlife Renewal', 'SEMINAR',
        'Half Patiunus', 'Jakarta Selatan',
        44, '10.00 WIB',
        'Seminar tentang pengembangan diri.',
        4294366913, 4293436824, null,
        30, 12, 300, 9
    ),
    (
        'Home Sweet Loan The Musical', 'FILM',
        'Teater Jakarta', 'Jakarta Pusat',
        26, '15.00 WIB',
        'Adaptasi panggung musikal dari film Home Sweet Loan.',
        4284301367, 4292332744, null,
        40, 18, 350, 11
    ),
    (
        'Cooltura Festival 2026', 'MUSIK',
        'Graha Cakrawala', 'Malang',
        10, '15.00 WIB',
        'Festival musik dan budaya anak muda Malang.',
        4294930176, 4294953512, 24,
        110, 52, 900, 35
    ),
    (
        'FISTERIA 14', 'MUSIK',
        'Lapangan Rampal', 'Malang',
        16, '16.00 WIB',
        'Pentas seni tahunan dengan bintang tamu nasional.',
        4285143962, 4291728344, null,
        70, 28, 520, 20
    ),
    (
        'OPUS FEST VOL 5', 'SENI',
        'Gedung Kesenian Gajayana', 'Malang',
        4, '18.30 WIB',
        'Pertunjukan seni dan musik komunitas kampus.',
        4278217052, 4286630852, null,
        20, 8, 190, 6
    ),
    (
        'Jazz Kampus 2026', 'MUSIK',
        'Samantha Krida', 'Malang',
        -3, '19.00 WIB',
        'Event sudah selesai.',
        4281812815, 4287669422, null,
        300, 120, 2000, 80
    )
)

insert into public.events (
    name, category, venue_id, date, time,
    description, poster_start, poster_end,
    flash_ends_at, base_sold, base_wishlist,
    view_count, views_last_7_days
)
select
    s.name,
    s.category,
    v.id,
    (now() at time zone 'Asia/Jakarta')::date
        + s.days_offset,
    s.event_time,
    s.description,
    s.poster_start,
    s.poster_end,
    case
        when s.flash_hours is not null
        then now() + s.flash_hours * interval '1 hour'
        else null
    end,
    s.base_sold,
    s.base_wishlist,
    s.view_count,
    s.views_last_7_days
from seed s
join public.venues v
    on v.name = s.venue_name
    and v.city = s.city
where not exists (
    select 1
    from public.events e
    where e.name = s.name
      and e.venue_id = v.id
      and e.created_by is null
)
on conflict (name, venue_id, date)
do nothing;

commit;
