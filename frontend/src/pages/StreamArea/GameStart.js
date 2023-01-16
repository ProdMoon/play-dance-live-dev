import { useEffect, useState } from 'react';

import { SongListData } from '../../assets/songListData';
import { useLoginContext } from '../../context/LoginContext';
import { useSocketContext } from '../../context/SocketContext';

function GameStart(props) {
  const handleSetCurrentSongUrl = props.handleSetCurrentSongUrl;

  const [userInfo, setUserInfo] = useLoginContext();
  const isRoomOwner = userInfo.isRoomOwner;
  const isPublisher = userInfo.isPublisher;

  const socketContextObjects = useSocketContext();
  const client = socketContextObjects.client;
  const [roundStart, setRoundStart] = socketContextObjects.roundStart;
  const [roundEnd, setRoundEnd] = socketContextObjects.roundEnd;
  const [songVersion, setSongVersion] = socketContextObjects.songVersion;
  const [isOwnerTurn, setIsOwnerTurn] = socketContextObjects.isOwnerTurn;

  const [songList, setSongList] = useState(new Map());

  // 처음 마운트되었을때만 실행하는 콜백 함수입니다.
  useEffect(() => {
    const listVersionSongList = SongListData;
    const newSongList = new Map();
    listVersionSongList.map((song) => {
      const [key, value] = song;
      newSongList.set(key, value);
    });
    setSongList(newSongList);
  }, []);

  useEffect(() => {
    if (isPublisher && (isRoomOwner === isOwnerTurn)) {
      myTurn();
    }
  }, [roundStart]);

  useEffect(() => {
    if (isPublisher) {
      if (isRoomOwner) {
        // TODO: 두번째 참가자에게 시작 신호 보내기
        // TODO: [Backend] chat.sendMessage 대신 다른 URI를 사용하는 것이 좋음.
        client.send(
          '/app/chat.sendMessage',
          {},
          JSON.stringify({
            type: 'ROUND_START',
            sender: userInfo.userName,
            roomId: userInfo.roomId,
            currentRound: roundStart,
            isOwnerTurn: false,
            songVersion: songVersion,
          }),
        );
      } else {
        // TODO: 투표 요청 보내기
        client.send(
          '/app/chat.sendMessage',
          {},
          JSON.stringify({
            type: 'VOTE_START',
            sender: userInfo.userName,
            roomId: userInfo.roomId,
          }),
        );
      }
    }
  }, [roundEnd]);

  function myTurn() {
    const songName = userInfo.songs[roundStart - 1];
    const songObject = songList.get(songName);
    switch (songVersion) {
      case 'normal':
        handleSetCurrentSongUrl(songObject.normalSrc);
      case 'double':
        handleSetCurrentSongUrl(songObject.doubleSrc);
      default:
        console.error('잘못된 song Version 요청입니다.');
    }
  }

  return <div id='game-div' />;
}

export default GameStart;
