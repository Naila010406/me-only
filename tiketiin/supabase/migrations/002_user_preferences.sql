-- TIKETIIN DATABASE
-- Tahap 02: User Preferences

begin;

create table public.user_preferences (
    id bigint generated always as identity primary key,
    user_id uuid not null
        references public.profiles(id) on delete cascade,
    category text not null check (
        category in (
            'MUSIK',
            'OLAHRAGA',
            'SEMINAR',
            'PAMERAN',
            'SENI',
            'FILM'
        )
    ),
    created_at timestamptz not null default now(),
    unique (user_id, category)
);

-- Mengaktifkan RLS
alter table public.user_preferences
enable row level security;

revoke all on public.user_preferences
from anon, authenticated;

grant select, insert, delete
on public.user_preferences
to authenticated;

-- Pengguna hanya dapat mengakses
-- preferensinya sendiri
create policy "Manage own preferences"
on public.user_preferences
for all
to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

commit;
