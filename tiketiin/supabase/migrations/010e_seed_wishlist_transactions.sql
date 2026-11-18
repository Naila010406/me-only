-- TIKETIIN DATABASE
-- Tahap 10E: Seed Wishlist and Transactions

begin;

-- 1. Seed wishlist pengguna

with seed (email, event_name) as (
    values
    ('buyer@tiketiin.example', 'Cooltura Festival 2026'),
    ('buyer@tiketiin.example', 'FISTERIA 14'),
    ('buyer@tiketiin.example', 'OPUS FEST VOL 5'),
    ('buyer@tiketiin.example', 'ROSETOPIA ASIA TOUR'),
    ('buyer2@tiketiin.example', 'Fun Walk 5K 2026'),
    ('buyer2@tiketiin.example', 'Indonesia Golf Festival'),
    ('buyer2@tiketiin.example',
     'Rise & Bloom 2026: Midlife Renewal')
)

insert into public.wishlists (
    user_id,
    event_id
)
select
    p.id,
    e.id
from seed s
join public.profiles p
    on p.email = s.email
join public.events e
    on e.name = s.event_name
    and e.created_by is null
where e.date >= current_date
on conflict (user_id, event_id) do nothing;


-- 2. Seed riwayat event views

insert into public.event_views (
    user_id,
    event_id,
    viewed_at,
    viewed_on
)
select
    w.user_id,
    w.event_id,
    now() - g.day_offset * interval '1 day',
    current_date - g.day_offset
from public.wishlists w
join public.profiles p
    on p.id = w.user_id
cross join generate_series(0, 2)
    as g(day_offset)
where p.email in (
    'buyer@tiketiin.example',
    'buyer2@tiketiin.example'
)
on conflict (user_id, event_id, viewed_on)
do nothing;


-- 3. Seed transaksi simulasi
-- Menggunakan fungsi backend tahap 09C dan 09D

do $$
declare
    v_seed record;
    v_buyer public.profiles%rowtype;
    v_listing_id integer;
    v_order_id integer;
begin

    for v_seed in

        select *
        from (
            values

            ('DEMO-ORDER-001',
             'buyer@tiketiin.example',
             'LALALA FESTIVAL 2027',
             'Regular'),

            ('DEMO-ORDER-002',
             'buyer2@tiketiin.example',
             'LALALA FESTIVAL 2027',
             'Regular'),

            ('DEMO-ORDER-003',
             'buyer@tiketiin.example',
             'LALALA FESTIVAL 2027',
             'Regular'),

            ('DEMO-ORDER-004',
             'buyer@tiketiin.example',
             'Cooltura Festival 2026',
             'Festival'),

            ('DEMO-ORDER-005',
             'buyer2@tiketiin.example',
             'Fun Walk 5K 2026',
             'Regular')

        ) as seed (
            demo_code,
            buyer_email,
            event_name,
            seat_category
        )

    loop

        -- Hindari transaksi demo duplikat
        if exists (
            select 1
            from public.orders
            where order_code = v_seed.demo_code
        ) then
            continue;
        end if;

        -- Mendapatkan profil buyer
        select *
        into v_buyer
        from public.profiles
        where email = v_seed.buyer_email;

        if not found then
            raise exception 'Akun buyer testing belum tersedia';
        end if;

        -- Mendapatkan listing tiket demo
        select l.id
        into v_listing_id
        from public.ticket_listings l
        join public.events e
            on e.id = l.event_id
        join public.profiles s
            on s.id = l.seller_id
        where e.name = v_seed.event_name
          and e.created_by is null
          and e.date >= current_date
          and l.seat_category = v_seed.seat_category
          and s.email = 'seller@tiketiin.example'
          and l.status = 'AKTIF'
          and l.quantity > 0
        order by l.id
        limit 1;

        if not found then
            raise exception
                'Listing demo belum tersedia: %',
                v_seed.event_name;
        end if;

        -- Simulasi konteks buyer untuk seed admin
        perform set_config(
            'request.jwt.claim.sub',
            v_buyer.id::text,
            true
        );

        perform set_config(
            'request.jwt.claims',
            jsonb_build_object(
                'sub', v_buyer.id::text,
                'role', 'authenticated'
            )::text,
            true
        );

        -- Membuat pesanan melalui fungsi backend
        v_order_id := public.create_order(
            v_listing_id,
            1,
            v_buyer.name,
            v_buyer.email,
            ''
        );

        -- Memberi kode khusus transaksi demo
        update public.orders
        set order_code = v_seed.demo_code
        where id = v_order_id;

        -- Simulasi pembayaran
        perform public.pay_order(
            v_order_id,
            'SIMULATION'
        );

        -- Menyelesaikan transaksi dan escrow
        perform public.confirm_ticket(
            v_order_id
        );

    end loop;

    -- Menghapus konteks simulasi
    perform set_config(
        'request.jwt.claim.sub', '', true
    );

    perform set_config(
        'request.jwt.claims', '{}', true
    );

end;
$$;

commit;
