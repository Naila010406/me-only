-- TIKETIIN DATABASE
-- Tahap 11C: Authentication Update

begin;

-- 1. Memperbarui function pembuatan profil
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin

    insert into public.profiles (
        id,
        name,
        email,
        phone
    )
    values (
        new.id,
        coalesce(new.raw_user_meta_data ->> 'name', ''),
        new.email,
        coalesce(new.phone, '')
    );

    return new;

end;
$$;


-- 2. Function sinkronisasi data Authentication
create or replace function public.sync_auth_user_profile()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin

    update public.profiles
    set
        email = new.email,
        phone = coalesce(new.phone, '')
    where id = new.id;

    return new;

end;
$$;


-- 3. Trigger sinkronisasi profil pengguna
drop trigger if exists on_auth_user_updated
on auth.users;

create trigger on_auth_user_updated
after update of email, phone
on auth.users
for each row
when (
    old.email is distinct from new.email
    or old.phone is distinct from new.phone
)
execute function public.sync_auth_user_profile();


-- 4. Pengaturan keamanan function
revoke execute on function
public.handle_new_user()
from public, anon, authenticated;

revoke execute on function
public.sync_auth_user_profile()
from public, anon, authenticated;

commit;
