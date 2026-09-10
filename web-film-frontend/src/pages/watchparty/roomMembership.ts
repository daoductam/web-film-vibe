import api from '../../services/api';
import { watchPartyService } from '../../services/watchParty.service';
import type { WatchRoomMember } from '../../services/watchParty.service';

interface Membership {
  promise: Promise<WatchRoomMember>;
  consumers: number;
  timer?: ReturnType<typeof setTimeout>;
  alreadyLeft: boolean;
}

const memberships = new Map<string, Membership>();

/** Share the pending join across React's effect replay, then leave after the last consumer. */
export function acquireRoomMembership(roomId: number, userId: number, token: string) {
  const key = `${roomId}:${userId}`;
  let membership = memberships.get(key);
  if (!membership) {
    membership = { promise: watchPartyService.joinRoom(roomId), consumers: 0, alreadyLeft: false };
    memberships.set(key, membership);
  }
  const current = membership;
  current.consumers += 1;
  clearTimeout(current.timer);
  return {
    joined: current.promise,
    release: (alreadyLeft: boolean) => {
      current.alreadyLeft ||= alreadyLeft;
      current.consumers -= 1;
      if (current.consumers > 0) return;
      current.timer = setTimeout(() => {
        void current.promise.then(async () => {
          if (current.consumers > 0) return;
          memberships.delete(key);
          if (current.alreadyLeft) return;
          // Retain the joining identity when logout/account switching has changed the auth store.
          const base = api.defaults.baseURL || '/api/v1';
          await fetch(`${base}/watch-rooms/${roomId}/leave`, {
            method: 'POST', headers: { Authorization: `Bearer ${token}` }, keepalive: true,
          });
        }).catch(() => { if (current.consumers === 0) memberships.delete(key); });
      }, 0);
    },
  };
}
