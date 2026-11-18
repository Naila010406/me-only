-- TIKETIIN DATABASE
-- Tahap 09C: Checkout & Stock Reservation

begin;

-- Menambahkan status reservasi tiket
alter table public.ticket_listings
drop constraint ticket_listings_status_check;

alter table public.ticket_listings
add constraint ticket_listings_status_check
check (status in (
    'AKTIF',
    'TERJUAL',
    'NONAKTIF',
    'DIPESAN'
));

-- Mencegah seller mengubah listing
-- yang seluruh stoknya sedang dipesan
alter policy "Update own listings"
on public.ticket_listings
using (
    auth.uid() = seller_id
    and status not in ('TERJUAL', 'DIPESAN')
);

-- Fungsi membuat pesanan
create or replace function public.create_order(
    p_listing_id integer,
    p_quantity integer,
    p_buyer_name text,
    p_buyer_email text,
    p_buyer_phone text default ''
)
returns integer
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_listing public.ticket_listings%rowtype;
    v_order_id integer;
    v_order_code text;
    v_remaining integer;
    v_total bigint;
    v_service_fee bigint := 10000;
begin

    -- 1. Validasi pengguna
    if v_user_id is null then
        raise exception 'Pengguna harus login';
    end if;

    -- 2. Validasi jumlah tiket
    if p_quantity is null or p_quantity <= 0 then
        raise exception 'Jumlah tiket tidak valid';
    end if;

    -- 3. Validasi data pemesan
    if coalesce(trim(p_buyer_name), '') = ''
       or coalesce(trim(p_buyer_email), '') = '' then
        raise exception 'Data pemesan belum lengkap';
    end if;

    -- 4. Mengunci baris listing selama transaksi
    select *
    into v_listing
    from public.ticket_listings
    where id = p_listing_id
    for update;

    if not found then
        raise exception 'Listing tiket tidak ditemukan';
    end if;

    -- 5. Validasi status tiket
    if v_listing.status <> 'AKTIF' then
        raise exception 'Tiket tidak tersedia';
    end if;

    -- 6. Seller tidak dapat membeli tiket sendiri
    if v_listing.seller_id = v_user_id then
        raise exception 'Tidak dapat membeli tiket sendiri';
    end if;

    -- 7. Validasi stok
    if v_listing.quantity < p_quantity then
        raise exception 'Stok tiket tidak mencukupi';
    end if;

    -- 8. Validasi tanggal event
    if not exists (
        select 1
        from public.events
        where id = v_listing.event_id
          and date >= current_date
    ) then
        raise exception 'Event sudah berakhir';
    end if;

    -- 9. Membuat kode pemesanan
    v_order_code :=
        'TKT-' || upper(
            replace(gen_random_uuid()::text, '-', '')
        );

    -- 10. Membuat pesanan
    insert into public.orders (
        order_code,
        buyer_id,
        seller_id,
        event_id,
        listing_id,
        quantity,
        unit_price,
        service_fee,
        buyer_name,
        buyer_email,
        buyer_phone,
        status,
        expires_at
    )
    values (
        v_order_code,
        v_user_id,
        v_listing.seller_id,
        v_listing.event_id,
        v_listing.id,
        p_quantity,
        v_listing.price,
        v_service_fee,
        trim(p_buyer_name),
        trim(p_buyer_email),
        coalesce(p_buyer_phone, ''),
        'PENDING',
        now() + interval '10 minutes'
    )
    returning id, total_price
    into v_order_id, v_total;

    -- 11. Mengurangi stok yang tersedia
    v_remaining := v_listing.quantity - p_quantity;

    update public.ticket_listings
    set
        quantity = v_remaining,
        status = case
            when v_remaining = 0 then 'DIPESAN'
            else 'AKTIF'
        end
    where id = v_listing.id;

    -- 12. Membuat record pembayaran
    insert into public.payments (
        order_id,
        payment_method,
        amount,
        status,
        escrow_status
    )
    values (
        v_order_id,
        'SIMULATION',
        v_total,
        'PENDING',
        'PENDING'
    );

    -- 13. Membuat notifikasi
    insert into public.notifications (
        user_id,
        title,
        message,
        type,
        event_id,
        order_id
    )
    values (
        v_user_id,
        'Pesanan Dibuat',
        'Selesaikan pembayaran dalam waktu 10 menit.',
        'ORDER',
        v_listing.event_id,
        v_order_id
    );

    return v_order_id;

end;
$$;

-- Pengaturan keamanan function
revoke execute on function
public.create_order(integer, integer, text, text, text)
from public, anon, authenticated;

grant execute on function
public.create_order(integer, integer, text, text, text)
to authenticated;

commit;
