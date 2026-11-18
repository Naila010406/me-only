-- TIKETIIN DATABASE
-- Tahap 12C: Database Storage Integration

begin;

-- 1. Foto profil pengguna
alter table public.profiles
add column if not exists avatar_url text;


-- 2. Bukti kepemilikan tiket
alter table public.ticket_listings
add column if not exists proof_path text;


-- 3. Dokumen verifikasi e-KYC
alter table public.verifications
add column if not exists id_card_path text;

alter table public.verifications
add column if not exists selfie_path text;


-- 4. Hak akses foto profil
grant update (avatar_url)
on public.profiles
to authenticated;


-- 5. Hak akses bukti tiket
grant insert (proof_path)
on public.ticket_listings
to authenticated;


-- 6. Hak akses dokumen verifikasi
grant insert (id_card_path, selfie_path)
on public.verifications
to authenticated;


-- 7. Validasi lokasi file bukti tiket
alter table public.ticket_listings
add constraint valid_ticket_proof_path
check (
    proof_path is null
    or split_part(proof_path, '/', 1)
       = seller_id::text
);


-- 8. Validasi lokasi dokumen e-KYC
alter table public.verifications
add constraint valid_id_card_path
check (
    id_card_path is null
    or split_part(id_card_path, '/', 1)
       = user_id::text
);

alter table public.verifications
add constraint valid_selfie_path
check (
    selfie_path is null
    or split_part(selfie_path, '/', 1)
       = user_id::text
);

commit;
