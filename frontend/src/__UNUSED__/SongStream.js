// 주의 : 현재 사용되지 않는 파일입니다.

import React, { useRef, useEffect } from 'react';

function SongStream(props) {
  const playFlag = props.playFlag;
  const songUrl = props.songUrl;
  const changeAudioSource = props.onAudioSourceChange;

  const audioRef = useRef(null);

  useEffect(() => {
    console.log('playFlag changed! ' + playFlag);
    if (playFlag === true) {
      audioRef.current.play();
    }
  }, [playFlag]);

  useEffect(async () => {
    const audioCtx = new AudioContext();
    const dest = audioCtx.createMediaStreamDestination();
    const source = audioCtx.createMediaElementSource(audioRef.current);
    await source.connect(dest);
    const audioSource = dest.stream; // audioSource 변수가 openvidu 연결에 사용할 audiosource 입니다.
    console.log(dest);
    changeAudioSource(audioSource);

    // cleanup function
    return () => {
      source.disconnect();
      dest.disconnect();
    };
  }, [songUrl]);

  return (
    <div>
      <audio ref={audioRef} src={songUrl} controls />
    </div>
  );
}

export default SongStream;
