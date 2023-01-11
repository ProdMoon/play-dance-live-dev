import React, { useRef, useEffect } from 'react';

function AudioStream() {
  const audioRef = useRef(null);

  useEffect(() => {
    const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    const source = audioCtx.createMediaElementSource(audioRef.current);
    const dest = audioCtx.createMediaStreamDestination();
    source.connect(dest);
    const stream = dest.stream;
    // Use the `stream` variable as the audio source in your WebRTC connection
    // ...
    // cleanup function
    return () => {
        source.disconnect();
        dest.disconnect();
    }
  }, []);

  return (
    <div>
      <audio ref={audioRef} src='audiofile.mp3' />
    </div>
  );
}

export default AudioStream;
