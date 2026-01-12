-- Mood Tracker Table for Entouche
-- Run this in Supabase SQL Editor

-- Create mood_logs table
CREATE TABLE IF NOT EXISTS public.mood_logs (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    mood INTEGER NOT NULL CHECK (mood >= 1 AND mood <= 5),
    note TEXT,
    logged_at DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, logged_at)  -- One mood per day per user
);

-- Create index for faster queries
CREATE INDEX idx_mood_logs_user_id ON public.mood_logs(user_id);
CREATE INDEX idx_mood_logs_logged_at ON public.mood_logs(logged_at DESC);

-- Enable Row Level Security
ALTER TABLE public.mood_logs ENABLE ROW LEVEL SECURITY;

-- RLS Policies for mood_logs
CREATE POLICY "Users can view own mood logs" ON public.mood_logs
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can create own mood logs" ON public.mood_logs
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own mood logs" ON public.mood_logs
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own mood logs" ON public.mood_logs
    FOR DELETE USING (auth.uid() = user_id);

-- Grant permissions
GRANT ALL ON public.mood_logs TO authenticated;

SELECT 'Mood logs table created successfully!' as result;
