-- TIKETIIN DATABASE
-- Tahap 09A: User Interaction Statistics

begin;

create or replace function
public.get_event_interaction_stats(
    p_event_id integer default null
)
returns table (
    event_id integer,
    wishlist_count bigint,
    view_count bigint,
    views_last_7_days bigint
)
language sql
stable
security definer
set search_path = ''
as $$
    select
        e.id,

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
            and ev.viewed_at >= now() - interval '7 days'
        )

    from public.events e

    where p_event_id is null
       or e.id = p_event_id

    order by e.id;
$$;

-- Mengatur hak akses function
revoke execute on function
public.get_event_interaction_stats(integer)
from public, anon, authenticated;

grant execute on function
public.get_event_interaction_stats(integer)
to authenticated;

commit;
