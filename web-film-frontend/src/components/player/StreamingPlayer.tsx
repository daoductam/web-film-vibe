import { useEffect, useRef, useState } from 'react';
import type { RefObject } from 'react';
import Hls from 'hls.js';

export interface PlaybackProgress { currentTime: number; duration: number }

interface Props {
    src: string;
    title: string;
    poster?: string;
    startTime?: number;
    autoPlay?: boolean;
    controls?: boolean;
    videoRef?: RefObject<HTMLVideoElement | null>;
    onProgress?: (progress: PlaybackProgress, force?: boolean) => void;
    onEnded?: () => void;
    onPlay?: () => void;
    onPause?: () => void;
    onSeeked?: () => void;
    onRateChange?: () => void;
}

export function StreamingPlayer({ src, title, poster, startTime = 0, autoPlay = true,
    controls = true, videoRef, onProgress, onEnded, onPlay, onPause, onSeeked, onRateChange }: Props) {
    const localRef = useRef<HTMLVideoElement>(null);
    const ref = videoRef ?? localRef;
    const [error, setError] = useState(false);
    const [attempt, setAttempt] = useState(0);
    const initialTime = useRef(startTime);
    const progressCallback = useRef(onProgress);
    useEffect(() => { progressCallback.current = onProgress; }, [onProgress]);

    useEffect(() => {
        const video = ref.current;
        if (!video) return;
        const restore = () => {
            if (Number.isFinite(video.duration) && initialTime.current > 0) {
                video.currentTime = Math.min(initialTime.current, Math.max(0, video.duration - 1));
            }
        };
        video.addEventListener('loadedmetadata', restore);
        let hls: Hls | undefined;
        // Some browsers report native HLS support but cannot play this stream.
        // Prefer hls.js when MediaSource is available, with native playback as fallback.
        if (Hls.isSupported()) {
            hls = new Hls();
            hls.loadSource(src);
            hls.attachMedia(video);
            hls.on(Hls.Events.ERROR, (_event, data) => { if (data.fatal) setError(true); });
        } else {
            video.src = src;
        }
        return () => {
            if (video.currentTime > 0 && Number.isFinite(video.duration)) {
                progressCallback.current?.({ currentTime: video.currentTime, duration: video.duration }, true);
            }
            video.removeEventListener('loadedmetadata', restore);
            hls?.destroy();
            video.removeAttribute('src');
            video.load();
        };
    }, [src, attempt, ref]);

    const report = (force = false) => {
        const video = ref.current;
        if (video && Number.isFinite(video.duration)) {
            onProgress?.({ currentTime: video.currentTime, duration: video.duration }, force);
        }
    };

    return <div className="relative h-full w-full bg-black">
        <video ref={ref} title={title} aria-label={title} poster={poster} controls={controls}
            autoPlay={autoPlay} playsInline className="h-full w-full" onError={() => setError(true)}
            onTimeUpdate={() => report()} onPlay={onPlay} onPause={() => { report(true); onPause?.(); }}
            onSeeked={() => { report(true); onSeeked?.(); }} onRateChange={onRateChange}
            onEnded={() => { report(true); onEnded?.(); }} />
        {error && <div role="alert" className="absolute inset-0 flex flex-col items-center justify-center gap-4 bg-black/90 p-6 text-center text-white">
            <p>Không thể phát video. Vui lòng thử lại hoặc chọn server khác.</p>
            <button onClick={() => { setError(false); setAttempt(value => value + 1); }} className="rounded-lg bg-neon px-4 py-2 text-obsidian">Thử lại</button>
        </div>}
    </div>;
}
