import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UserProfile } from '../types';

interface AuthState {
    token: string | null;
    user: UserProfile | null;
    setAuth: (token: string, user: UserProfile) => void;
    updateUser: (user: Partial<UserProfile>) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            token: null,
            user: null,
            setAuth: (token, user) => set({ token, user }),
            updateUser: (updatedUser) => set((state) => ({ 
                user: state.user ? { ...state.user, ...updatedUser } : null 
            })),
            logout: () => set({ token: null, user: null }),
        }),
        {
            name: 'auth-storage', // name of item in the storage (must be unique)
        }
    )
);
