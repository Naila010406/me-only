-- TIKETIIN DATABASE
-- Tahap 12B: Storage Security Policies

begin;

-- 1. Pengguna dapat mengunggah file sendiri
create policy "Tiketiin upload own files"
on storage.objects
for insert
to authenticated
with check (
    (storage.foldername(name))[1] = auth.uid()::text
    and (
        bucket_id in (
            'avatars',
            'ticket-proofs',
            'kyc-documents'
        )
        or (
            bucket_id = 'event-posters'
            and exists (
                select 1
                from public.verifications v
                where v.user_id = auth.uid()
                  and v.status = 'VERIFIED'
            )
        )
    )
);


-- 2. Pengguna dapat membaca file sendiri
create policy "Tiketiin read own files"
on storage.objects
for select
to authenticated
using (
    bucket_id in (
        'avatars',
        'event-posters',
        'ticket-proofs',
        'kyc-documents'
    )
    and owner_id = auth.uid()::text
    and (storage.foldername(name))[1] = auth.uid()::text
);


-- 3. Pengguna dapat memperbarui file sendiri
create policy "Tiketiin update own files"
on storage.objects
for update
to authenticated
using (
    bucket_id in (
        'avatars',
        'event-posters',
        'ticket-proofs',
        'kyc-documents'
    )
    and owner_id = auth.uid()::text
    and (storage.foldername(name))[1] = auth.uid()::text
)
with check (
    owner_id = auth.uid()::text
    and (storage.foldername(name))[1] = auth.uid()::text
    and (
        bucket_id in (
            'avatars',
            'ticket-proofs',
            'kyc-documents'
        )
        or (
            bucket_id = 'event-posters'
            and exists (
                select 1
                from public.verifications v
                where v.user_id = auth.uid()
                  and v.status = 'VERIFIED'
            )
        )
    )
);


-- 4. Pengguna dapat menghapus file sendiri
create policy "Tiketiin delete own files"
on storage.objects
for delete
to authenticated
using (
    bucket_id in (
        'avatars',
        'event-posters',
        'ticket-proofs',
        'kyc-documents'
    )
    and owner_id = auth.uid()::text
    and (storage.foldername(name))[1] = auth.uid()::text
);

commit;
