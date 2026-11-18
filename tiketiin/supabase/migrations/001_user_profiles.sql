-- TIKETIIN DATABASE
-- Tahap 1: User Profiles

begin;

create table public.profiles (
    id uuid primary key
        references auth.users(id) on delete cascade,
    name text not null default '',
    email text,
    phone text not null default '',
    city text not null default '',
    level text not null default 'Silver',
    referral_code text unique,
    is_kyc_verified boolean not null default false,
    created_at timestamptz not null default now()
);

-- Mengaktifkan keamanan database
alter table public.profiles
enable row level security;

revoke all on public.profiles
from anon, authenticated;

grant select on public.profiles
to authenticated;

grant update (name, phone, city)
on public.profiles to authenticated;

-- Pengguna hanya bisa melihat profil sendiri
create policy "Read own profile"
on public.profiles
for select
to authenticated
using (auth.uid() = id);

-- Pengguna hanya bisa mengedit profil sendiri
create policy "Update own profile"
on public.profiles
for update
to authenticated
using (auth.uid() = id)
with check (auth.uid() = id);

-- Membuat profil otomatis setelah register
create function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.profiles (id, name, email)
    values (
        new.id,
        coalesce(new.raw_user_meta_data ->> 'name', ''),
        new.email
    );
    return new;
end;
$$;

revoke execute on function public.handle_new_user()
from public, anon, authenticated;

create trigger on_auth_user_created
after insert on auth.users
for each row
execute function public.handle_new_user();

commit;
