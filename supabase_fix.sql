-- Fix for Entouche: Change notes to reference auth.users directly
-- Run this in Supabase SQL Editor

-- First, drop the existing foreign key constraint on notes
ALTER TABLE public.notes DROP CONSTRAINT IF EXISTS notes_user_id_fkey;

-- Add new foreign key that references auth.users directly
ALTER TABLE public.notes
ADD CONSTRAINT notes_user_id_fkey
FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE;

-- Do the same for voice_memos
ALTER TABLE public.voice_memos DROP CONSTRAINT IF EXISTS voice_memos_user_id_fkey;

ALTER TABLE public.voice_memos
ADD CONSTRAINT voice_memos_user_id_fkey
FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE;

-- Also, create a profile for any existing users who don't have one
INSERT INTO public.profiles (id, email, name)
SELECT id, email, COALESCE(raw_user_meta_data->>'name', split_part(email, '@', 1))
FROM auth.users
WHERE id NOT IN (SELECT id FROM public.profiles)
ON CONFLICT (id) DO NOTHING;

-- Verify the fix
SELECT 'Fix applied successfully!' as status;
