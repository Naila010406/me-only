-- TIKETIIN DATABASE
-- Tahap 09B: e-KYC Simulation

begin;

-- 1. Fungsi pengajuan verifikasi
create or replace function public.submit_kyc()
returns text
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_status text;
begin
    if v_user_id is null then
        raise exception 'Pengguna harus login';
    end if;

    insert into public.verifications (user_id)
    values (v_user_id)

    on conflict (user_id)
    do update set
        status = 'PENDING',
        rejection_reason = null,
        submitted_at = now(),
        verified_at = null
    where public.verifications.status = 'REJECTED';

    select status into v_status
    from public.verifications
    where user_id = v_user_id;

    return v_status;
end;
$$;

-- 2. Fungsi review verifikasi oleh admin
create or replace function public.review_kyc(
    p_user_id uuid,
    p_approved boolean,
    p_reason text default null
)
returns text
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_status text;
begin
    if p_approved is null then
        raise exception 'Keputusan wajib diisi';
    end if;

    if p_approved = true then
        v_status := 'VERIFIED';
    else
        v_status := 'REJECTED';
    end if;

    update public.verifications
    set
        status = v_status,
        rejection_reason = case
            when p_approved then null
            else p_reason
        end,
        verified_at = case
            when p_approved then now()
            else null
        end
    where user_id = p_user_id
      and status = 'PENDING';

    if not found then
        raise exception 'Tidak ada pengajuan pending';
    end if;

    -- Sinkronisasi status profil pengguna
    update public.profiles
    set is_kyc_verified = p_approved
    where id = p_user_id;

    -- Membuat notifikasi otomatis
    insert into public.notifications (
        user_id,
        title,
        message,
        type
    )
    values (
        p_user_id,
        case
            when p_approved
                then 'Verifikasi Berhasil'
            else 'Verifikasi Ditolak'
        end,
        case
            when p_approved
                then 'Kamu sekarang dapat menjual tiket.'
            else coalesce(
                p_reason,
                'Silakan ajukan ulang verifikasi.'
            )
        end,
        'KYC'
    );

    return v_status;
end;
$$;

-- 3. Pengaturan hak akses
revoke execute on function
public.submit_kyc()
from public, anon, authenticated;

grant execute on function
public.submit_kyc()
to authenticated;

revoke execute on function
public.review_kyc(uuid, boolean, text)
from public, anon, authenticated, service_role;

-- Fungsi review hanya untuk administrator database
grant execute on function
public.review_kyc(uuid, boolean, text)
to postgres;

commit;
