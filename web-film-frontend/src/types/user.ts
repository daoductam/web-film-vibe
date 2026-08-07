export interface UserProfile {
    username: string;
    email: string;
    fullName: string;
    role: string;
    avatarUrl: string;
}

export interface AuthResponse {
    token: string;
    user: UserProfile;
}

export interface UpdateProfileRequest {
    fullName: string;
}
