import { useToastStore } from '../store/toastStore';

export const useToast = () => ({ showToast: useToastStore(state => state.showToast) });
