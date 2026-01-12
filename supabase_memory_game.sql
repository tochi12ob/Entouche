-- Memory Game Tables for Entouche
-- Run this in Supabase SQL Editor

-- Card Decks table (stores flashcard decks created by users)
CREATE TABLE IF NOT EXISTS public.card_decks (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    cards JSONB NOT NULL DEFAULT '[]'::jsonb,
    times_played INTEGER DEFAULT 0,
    best_score INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Game Results table (stores individual game session results)
CREATE TABLE IF NOT EXISTS public.game_results (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    deck_id UUID REFERENCES public.card_decks(id) ON DELETE CASCADE NOT NULL,
    mode TEXT NOT NULL,
    score INTEGER NOT NULL DEFAULT 0,
    correct_answers INTEGER NOT NULL DEFAULT 0,
    total_cards INTEGER NOT NULL DEFAULT 0,
    max_streak INTEGER NOT NULL DEFAULT 0,
    time_taken_ms BIGINT NOT NULL DEFAULT 0,
    played_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_card_decks_user_id ON public.card_decks(user_id);
CREATE INDEX IF NOT EXISTS idx_game_results_user_id ON public.game_results(user_id);
CREATE INDEX IF NOT EXISTS idx_game_results_deck_id ON public.game_results(deck_id);
CREATE INDEX IF NOT EXISTS idx_game_results_played_at ON public.game_results(played_at DESC);

-- Enable Row Level Security
ALTER TABLE public.card_decks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.game_results ENABLE ROW LEVEL SECURITY;

-- RLS Policies for card_decks
DROP POLICY IF EXISTS "Users can view their own decks" ON public.card_decks;
CREATE POLICY "Users can view their own decks" ON public.card_decks
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can create their own decks" ON public.card_decks;
CREATE POLICY "Users can create their own decks" ON public.card_decks
    FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can update their own decks" ON public.card_decks;
CREATE POLICY "Users can update their own decks" ON public.card_decks
    FOR UPDATE USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can delete their own decks" ON public.card_decks;
CREATE POLICY "Users can delete their own decks" ON public.card_decks
    FOR DELETE USING (auth.uid() = user_id);

-- RLS Policies for game_results
DROP POLICY IF EXISTS "Users can view their own results" ON public.game_results;
CREATE POLICY "Users can view their own results" ON public.game_results
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can create their own results" ON public.game_results;
CREATE POLICY "Users can create their own results" ON public.game_results
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for card_decks updated_at
DROP TRIGGER IF EXISTS update_card_decks_updated_at ON public.card_decks;
CREATE TRIGGER update_card_decks_updated_at
    BEFORE UPDATE ON public.card_decks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions
GRANT ALL ON public.card_decks TO authenticated;
GRANT ALL ON public.game_results TO authenticated;
