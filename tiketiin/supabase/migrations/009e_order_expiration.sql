-- TIKETIIN DATABASE
-- Tahap 09E: Order Expiration

begin;

-- 1. Fungsi internal pengembalian stok
create or replace function public.restore_pending_order(
    p_order_id integer,
    p_new_status text
)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_order public.orders%rowtype;
begin

    if p_new_status is null
       or p_new_status not in ('EXPIRED', 'CANCELLED') then
        raise exception 'Status pembatalan tidak valid';
    end if;

    select *
    into v_order
    from public.orders
    where id = p_order_id
    for update;

    if not found then
        return false;
    end if;

    if v_order.status <> 'PENDING' then
        return false;
    end if;

    if p_new_status = 'EXPIRED'
       and v_order.expires_at > clock_timestamp() then
        return false;
    end if;

    -- Memperbarui status pembayaran
    update public.payments
    set status = case
        when p_new_status = 'EXPIRED'
            then 'EXPIRED'
        else 'FAILED'
    end
    where order_id = p_order_id
      and status = 'PENDING';

    if not found then
        raise exception 'Status pembayaran tidak valid';
    end if;

    -- Mengembalikan stok tiket
    update public.ticket_listings l
    set
        quantity = l.quantity + v_order.quantity,
        status = case
            when l.status = 'NONAKTIF'
                then 'NONAKTIF'
            when exists (
                select 1
                from public.events e
                where e.id = l.event_id
                  and e.date >= current_date
            ) then 'AKTIF'
            else 'NONAKTIF'
        end
    where l.id = v_order.listing_id;

    -- Mengubah status pesanan
    update public.orders
    set status = p_new_status
    where id = p_order_id;

    -- Notifikasi pembatalan
    insert into public.notifications (
        user_id, title, message,
        type, event_id, order_id
    )
    values (
        v_order.buyer_id,
        case
            when p_new_status = 'EXPIRED'
                then 'Pesanan Kedaluwarsa'
            else 'Pesanan Dibatalkan'
        end,
        'Pesanan dibatalkan dan stok tiket dikembalikan.',
        'ORDER',
        v_order.event_id,
        p_order_id
    );

    return true;

end;
$$;


-- 2. Fungsi membatalkan pesanan kedaluwarsa
create or replace function public.expire_orders()
returns integer
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_order record;
    v_count integer := 0;
begin

    -- Mencegah dua proses batch berjalan bersamaan
    if not pg_try_advisory_xact_lock(90501) then
        return 0;
    end if;

    for v_order in
        select id
        from public.orders
        where status = 'PENDING'
          and expires_at <= clock_timestamp()
        order by listing_id, id
        limit 100
        for update skip locked
    loop

        if public.restore_pending_order(
            v_order.id,
            'EXPIRED'
        ) then
            v_count := v_count + 1;
        end if;

    end loop;

    return v_count;

end;
$$;


-- 3. Fungsi pembatalan pesanan oleh buyer
create or replace function public.cancel_order(
    p_order_id integer
)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
begin

    if auth.uid() is null then
        raise exception 'Pengguna harus login';
    end if;

    if not exists (
        select 1
        from public.orders
        where id = p_order_id
          and buyer_id = auth.uid()
    ) then
        raise exception 'Pesanan tidak ditemukan';
    end if;

    return public.restore_pending_order(
        p_order_id,
        'CANCELLED'
    );

end;
$$;


-- 4. Pengaturan hak akses
revoke execute on function
public.restore_pending_order(integer, text)
from public, anon, authenticated, service_role;

revoke execute on function
public.expire_orders()
from public, anon, authenticated, service_role;

grant execute on function
public.expire_orders()
to postgres;

revoke execute on function
public.cancel_order(integer)
from public, anon, authenticated;

grant execute on function
public.cancel_order(integer)
to authenticated;

commit;
