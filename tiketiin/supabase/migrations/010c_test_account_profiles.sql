-- TIKETIIN DATABASE
-- Tahap 10C: Test Account Profiles

begin;

-- Memastikan akun testing sudah dibuat
do $$
begin
    if (
        select count(*)
        from public.profiles
        where email in (
            'buyer@tiketiin.example',
            'seller@tiketiin.example',
            'buyer2@tiketiin.example'
        )
    ) <> 3 then
        raise exception
            'Buat ketiga akun testing di Authentication terlebih dahulu';
    end if;
end;
$$;

-- 1. Profil buyer pertama
update public.profiles
set
    name = 'Demo Buyer',
    city = 'Malang',
    level = 'Silver',
    referral_code = 'DEMO-BUYER'
where email = 'buyer@tiketiin.example';

-- 2. Profil seller
update public.profiles
set
    name = 'Demo Seller',
    city = 'Jakarta',
    level = 'Gold',
    referral_code = 'DEMO-SELLER'
where email = 'seller@tiketiin.example';

-- 3. Profil buyer kedua
update public.profiles
set
    name = 'Demo Buyer 2',
    city = 'Jakarta',
    level = 'Silver',
    referral_code = 'DEMO-BUYER2'
where email = 'buyer2@tiketiin.example';

-- 4. Pengajuan verifikasi seller simulasi
insert into public.verifications (user_id)
select id
from public.profiles
where email = 'seller@tiketiin.example'
on conflict (user_id) do nothing;

-- 5. Persetujuan verifikasi seller simulasi
select public.review_kyc(id, true)
from public.profiles
where email = 'seller@tiketiin.example'
and exists (
    select 1
    from public.verifications v
    where v.user_id = profiles.id
    and v.status = 'PENDING'
);

-- 6. Preferensi buyer pertama
insert into public.user_preferences (
    user_id, category
)
select p.id, c.category
from public.profiles p
cross join (
    values ('MUSIK'), ('SENI')
) as c(category)
where p.email = 'buyer@tiketiin.example'
on conflict (user_id, category) do nothing;

-- 7. Preferensi buyer kedua
insert into public.user_preferences (
    user_id, category
)
select p.id, c.category
from public.profiles p
cross join (
    values ('OLAHRAGA'), ('SEMINAR')
) as c(category)
where p.email = 'buyer2@tiketiin.example'
on conflict (user_id, category) do nothing;

commit;
