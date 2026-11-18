-- TIKETIIN DATABASE
-- Tahap 09D: Payment & Escrow Simulation

begin;

-- 1. Simulasi pembayaran tiket
create or replace function public.pay_order(
    p_order_id integer,
    p_payment_method text default 'SIMULATION'
)
returns text
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_order public.orders%rowtype;
    v_qr_code text;
begin

    if v_user_id is null then
        raise exception 'Pengguna harus login';
    end if;

    select *
    into v_order
    from public.orders
    where id = p_order_id
      and buyer_id = v_user_id
    for update;

    if not found then
        raise exception 'Pesanan tidak ditemukan';
    end if;

    if v_order.status <> 'PENDING' then
        raise exception 'Pesanan tidak dapat dibayar';
    end if;

    if v_order.expires_at <= clock_timestamp() then
        raise exception 'Waktu pembayaran telah habis';
    end if;

    if not exists (
        select 1
        from public.events
        where id = v_order.event_id
          and date >= current_date
    ) then
        raise exception 'Event sudah berakhir';
    end if;

    -- Membuat kode QR internal
    v_qr_code :=
        'TIKETIIN-' || gen_random_uuid()::text;

    -- Mengubah status pembayaran
    update public.payments
    set
        payment_method = coalesce(
            nullif(trim(p_payment_method), ''),
            'SIMULATION'
        ),
        payment_reference =
            'PAY-' || gen_random_uuid()::text,
        status = 'SUCCESS',
        escrow_status = 'HELD',
        paid_at = now()
    where order_id = p_order_id
      and status = 'PENDING';

    if not found then
        raise exception 'Pembayaran tidak valid';
    end if;

    -- Mengubah status pesanan
    update public.orders
    set
        status = 'PAID',
        qr_code = v_qr_code,
        paid_at = now()
    where id = p_order_id;

    -- Mengunci listing sebelum memperbarui status
    perform 1
    from public.ticket_listings
    where id = v_order.listing_id
    for update;

    -- Menandai listing habis jika tidak ada
    -- lagi pesanan yang masih pending
    update public.ticket_listings l
    set status = 'TERJUAL'
    where l.id = v_order.listing_id
      and l.quantity = 0
      and l.status = 'DIPESAN'
      and not exists (
          select 1
          from public.orders o
          where o.listing_id = l.id
            and o.status = 'PENDING'
      );

    -- Notifikasi buyer
    insert into public.notifications (
        user_id, title, message,
        type, event_id, order_id
    )
    values (
        v_order.buyer_id,
        'Pembayaran Berhasil',
        'Pembayaran berhasil. Dana ditahan dalam escrow simulasi.',
        'PAYMENT',
        v_order.event_id,
        p_order_id
    );

    -- Notifikasi seller
    insert into public.notifications (
        user_id, title, message,
        type, event_id, order_id
    )
    values (
        v_order.seller_id,
        'Tiket Berhasil Dibeli',
        'Buyer telah melakukan pembayaran simulasi.',
        'ORDER',
        v_order.event_id,
        p_order_id
    );

    return 'PAID';

end;
$$;


-- 2. Konfirmasi penerimaan tiket dan pelepasan escrow
create or replace function public.confirm_ticket(
    p_order_id integer
)
returns text
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_order public.orders%rowtype;
begin

    if v_user_id is null then
        raise exception 'Pengguna harus login';
    end if;

    select *
    into v_order
    from public.orders
    where id = p_order_id
      and buyer_id = v_user_id
    for update;

    if not found then
        raise exception 'Pesanan tidak ditemukan';
    end if;

    if v_order.status <> 'PAID' then
        raise exception 'Pesanan belum dibayar atau sudah selesai';
    end if;

    update public.payments
    set
        escrow_status = 'RELEASED',
        released_at = now()
    where order_id = p_order_id
      and status = 'SUCCESS'
      and escrow_status = 'HELD';

    if not found then
        raise exception 'Status escrow tidak valid';
    end if;

    update public.orders
    set
        status = 'COMPLETED',
        completed_at = now()
    where id = p_order_id;

    insert into public.notifications (
        user_id, title, message,
        type, event_id, order_id
    )
    values
    (
        v_order.buyer_id,
        'Pesanan Selesai',
        'Terima kasih telah menggunakan Tiketiin.',
        'ORDER',
        v_order.event_id,
        p_order_id
    ),
    (
        v_order.seller_id,
        'Dana Escrow Diteruskan',
        'Dana penjualan telah dilepaskan dalam simulasi.',
        'ESCROW',
        v_order.event_id,
        p_order_id
    );

    return 'COMPLETED';

end;
$$;


-- 3. Pengaturan hak akses
revoke execute on function
public.pay_order(integer, text)
from public, anon, authenticated;

grant execute on function
public.pay_order(integer, text)
to authenticated;

revoke execute on function
public.confirm_ticket(integer)
from public, anon, authenticated;

grant execute on function
public.confirm_ticket(integer)
to authenticated;

commit;
