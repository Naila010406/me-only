-- TIKETIIN DATABASE
-- Tahap 10B: Seed Hotels and Availability

begin;

insert into public.hotels (
    name, address, city, latitude, longitude,
    price_per_night, rating
)
values
('Citadines Sudirman',
 'Setiabudi, Jakarta', 'Jakarta',
 -6.2125, 106.8128, 1600000, 4.6),

('Favehotel Gatot Subroto',
 'Gatot Subroto, Jakarta', 'Jakarta',
 -6.2262, 106.8145, 761000, 4.2),

('Ambhara Hotel',
 'Blok M, Jakarta', 'Jakarta',
 -6.2431, 106.8009, 440600, 4.0),

('Hotel Mulia Senayan',
 'Senayan, Jakarta', 'Jakarta',
 -6.2166, 106.7975, 3200000, 4.8),

('The Sultan Hotel',
 'Gelora, Jakarta', 'Jakarta',
 -6.2159, 106.8085, 1150000, 4.4),

('Ibis Styles Tanah Abang',
 'Tanah Abang, Jakarta', 'Jakarta',
 -6.1893, 106.8133, 520000, 4.1),

('Santika Premiere ICE BSD',
 'BSD City, Tangerang', 'Tangerang',
 -6.3015, 106.6384, 980000, 4.5),

('Hotel Tugu Malang',
 'Klojen, Malang', 'Malang',
 -7.9771, 112.6340, 1250000, 4.7),

('Ibis Styles Malang',
 'Blimbing, Malang', 'Malang',
 -7.9467, 112.6430, 480000, 4.3)

on conflict (name, address, city)
do nothing;


with room_seed (hotel_name, rooms) as (
    values
        ('Citadines Sudirman', 5),
        ('Favehotel Gatot Subroto', 8),
        ('Ambhara Hotel', 3),
        ('Hotel Mulia Senayan', 0),
        ('The Sultan Hotel', 6),
        ('Ibis Styles Tanah Abang', 4),
        ('Santika Premiere ICE BSD', 7),
        ('Hotel Tugu Malang', 2),
        ('Ibis Styles Malang', 9)
),
event_dates as (
    select distinct date
    from public.events
    where date >= current_date
)

insert into public.hotel_availability (
    hotel_id, date,
    available_rooms, price_per_night
)
select
    h.id,
    e.date,
    r.rooms,
    h.price_per_night
from public.hotels h
join room_seed r
    on r.hotel_name = h.name
cross join event_dates e
on conflict (hotel_id, date)
do nothing;

commit;
