-- TIKETIIN DATABASE
-- Tahap 09G: Analytics & Recommendation Data

begin;

-- ==========================================
-- 1. RAISYA: HISTORI HARGA TIKET
-- ==========================================

create or replace function public.get_price_history(
    p_event_id integer,
    p_seat_category text
)
returns table (
    unit_price bigint
)
language sql
stable
security definer
set search_path = ''
as $$

    select o.unit_price

    from public.orders o

    join public.ticket_listings l
        on l.id = o.listing_id

    where o.event_id = p_event_id
      and lower(l.seat_category) =
          lower(p_seat_category)
      and o.status = 'COMPLETED'

    order by o.completed_at desc
    limit 100;

$$;


-- ==========================================
-- 2. NAILA: DATA HOT OPPORTUNITY
-- ==========================================

create or replace function public.get_event_demand_stats()
returns table (
    event_id integer,
    event_name text,
    sold_count bigint,
    wishlist_count bigint,
    view_count bigint,
    views_last_7_days bigint,
    available_stock bigint
)
language sql
stable
security definer
set search_path = ''
as $$

    select
        e.id,
        e.name,

        coalesce((
            select sum(o.quantity)
            from public.orders o
            where o.event_id = e.id
              and o.status in ('PAID', 'COMPLETED')
        ), 0),

        (
            select count(*)
            from public.wishlists w
            where w.event_id = e.id
        ),

        (
            select count(*)
            from public.event_views ev
            where ev.event_id = e.id
        ),

        (
            select count(*)
            from public.event_views ev
            where ev.event_id = e.id
              and ev.viewed_at >=
                  now() - interval '7 days'
        ),

        coalesce((
            select sum(l.quantity)
            from public.ticket_listings l
            where l.event_id = e.id
              and l.status = 'AKTIF'
        ), 0)

    from public.events e

    where e.date >= current_date

    order by e.id;

$$;


-- ==========================================
-- 3. NADA: DATA REKOMENDASI FOR YOU
-- ==========================================

create or replace function
public.get_user_recommendation_context()
returns jsonb
language sql
stable
security definer
set search_path = ''
as $$

    select jsonb_build_object(

        'user_id', p.id,

        'city', p.city,

        'preferred_categories',
        coalesce((
            select jsonb_agg(up.category)
            from public.user_preferences up
            where up.user_id = p.id
        ), '[]'::jsonb),

        'wishlist_event_ids',
        coalesce((
            select jsonb_agg(w.event_id)
            from public.wishlists w
            where w.user_id = p.id
        ), '[]'::jsonb),

        'wishlist_categories',
        coalesce((
            select jsonb_agg(distinct e.category)
            from public.wishlists w
            join public.events e
                on e.id = w.event_id
            where w.user_id = p.id
        ), '[]'::jsonb),

        'purchased_event_ids',
        coalesce((
            select jsonb_agg(distinct o.event_id)
            from public.orders o
            where o.buyer_id = p.id
              and o.status in ('PAID', 'COMPLETED')
        ), '[]'::jsonb),

        'purchased_categories',
        coalesce((
            select jsonb_agg(distinct e.category)
            from public.orders o
            join public.events e
                on e.id = o.event_id
            where o.buyer_id = p.id
              and o.status in ('PAID', 'COMPLETED')
        ), '[]'::jsonb)

    )

    from public.profiles p
    where p.id = auth.uid();

$$;


-- ==========================================
-- 4. NAILA: DASHBOARD PENJUALAN SELLER
-- ==========================================

create or replace function
public.get_seller_sales_summary()
returns table (
    total_orders bigint,
    tickets_sold bigint,
    total_revenue numeric,
    held_revenue numeric,
    released_revenue numeric
)
language sql
stable
security invoker
set search_path = ''
as $$

    select

        count(*),

        coalesce(sum(o.quantity), 0),

        coalesce(sum(
            o.quantity::numeric * o.unit_price
        ), 0),

        coalesce(sum(
            o.quantity::numeric * o.unit_price
        ) filter (
            where p.escrow_status = 'HELD'
        ), 0),

        coalesce(sum(
            o.quantity::numeric * o.unit_price
        ) filter (
            where p.escrow_status = 'RELEASED'
        ), 0)

    from public.orders o

    join public.payments p
        on p.order_id = o.id

    where o.seller_id = auth.uid()
      and o.status in ('PAID', 'COMPLETED')
      and p.status = 'SUCCESS';

$$;


-- ==========================================
-- 5. SASKIA: DATA HOTEL BERDASARKAN TANGGAL
-- ==========================================

create or replace function
public.get_available_hotels(
    p_event_id integer
)
returns table (
    hotel_id integer,
    hotel_name text,
    address text,
    city text,
    latitude double precision,
    longitude double precision,
    price_per_night bigint,
    rating numeric,
    available_rooms integer,
    image_url text,
    booking_url text
)
language sql
stable
security invoker
set search_path = ''
as $$

    select
        h.id,
        h.name,
        h.address,
        h.city,
        h.latitude,
        h.longitude,
        coalesce(a.price_per_night, h.price_per_night),
        h.rating,
        a.available_rooms,
        h.image_url,
        h.booking_url

    from public.hotels h

    join public.hotel_availability a
        on a.hotel_id = h.id

    join public.events e
        on e.date = a.date

    where e.id = p_event_id
      and e.date >= current_date
      and h.is_active = true
      and a.available_rooms > 0;

$$;


-- ==========================================
-- 6. PENGATURAN HAK AKSES
-- ==========================================

revoke execute on function
public.get_price_history(integer, text)
from public, anon, authenticated;

grant execute on function
public.get_price_history(integer, text)
to authenticated;


revoke execute on function
public.get_event_demand_stats()
from public, anon, authenticated;

grant execute on function
public.get_event_demand_stats()
to authenticated;


revoke execute on function
public.get_user_recommendation_context()
from public, anon, authenticated;

grant execute on function
public.get_user_recommendation_context()
to authenticated;


revoke execute on function
public.get_seller_sales_summary()
from public, anon, authenticated;

grant execute on function
public.get_seller_sales_summary()
to authenticated;


revoke execute on function
public.get_available_hotels(integer)
from public, anon, authenticated;

grant execute on function
public.get_available_hotels(integer)
to authenticated;

commit;
