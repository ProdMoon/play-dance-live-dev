import { createContext, useContext, useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

import { useLoginContext } from './LoginContext';

const SocketContext = createContext();

export function useSocketContext() {
  const value = useContext(SocketContext);
  return value;
}

export default function SocketContextProvider({ children }) {
  const [userInfo, setUserInfo] = useLoginContext();

  // 자식 요소들에 read/set이 제공되는 States
  const messagesObject = useState([]);
  const roundStartObject = useState(0);
  const roundEndObject = useState(0);
  const songVersionObject = useState('normal');
  const isOwnerTurnObject = useState(true);

  // 자식 요소들에 read-only로 제공되는 States
  const [client, setClient] = useState(null);

  // 여기서만 사용하는 States
  const [subscription, setSubscription] = useState(null);

  function socketSubscription() {
    const [messages, setMessages] = messagesObject;
    const username = userInfo.userName ?? undefined;

    setSubscription(
      // 다음과 같은 type들을 구독합니다.
      client.subscribe(`/topic/${userInfo.roomId}`, (message) => {
        const messageBody = JSON.parse(message.body);

        // 채팅 요청...
        if (messageBody.type === 'CHAT') {
          setMessages((prevMessages) => [...prevMessages, message]);
        }

        // 라운드 시작 신호...
        if (messageBody.type === 'ROUND_START') {
          const setRoundStart = roundStartObject[1];
          const setSongVersion = songVersionObject[1];
          const setIsOwnerTurn = isOwnerTurnObject[1];
          setRoundStart(messageBody.currentRound);
          setSongVersion(messageBody.songVersion);
          setIsOwnerTurn(messageBody.isOwnerTurn);
        }

        // 라운드 종료 신호...
        if (messageBody.type === 'ROUND_END') {
          const setRoundEnd = roundEndObject[1];
          const setSongVersion = songVersionObject[1];
          const setIsOwnerTurn = isOwnerTurnObject[1];
          setRoundEnd(messageBody.currentRound);
          setSongVersion(messageBody.songVersion);
          setIsOwnerTurn(messageBody.isOwnerTurn);
        }

        // TODO: 투표 시작 신호...
        if (messageBody.type === 'VOTE_START') {
          console.log('투표 시작 신호를 받았습니다.');
        }
      }),
    );

    client.send(
      '/app/chat.addUser',
      {},
      JSON.stringify({
        roomId: userInfo.roomId,
        sender: username,
        type: 'JOIN',
      }),
    );
  }

  function initSocket() {
    const socket = new SockJS(`https://${process.env.REACT_APP_HOST}/api/ws`);
    const stompClient = Stomp.over(socket);
    stompClient.connect({}, (frame) => {
      console.info('[WebSocket] Connected: ' + frame);
      setClient(stompClient);
    });
  }

  useEffect(() => {
    if (client !== null) {
      socketSubscription();
    }
  }, [client]);

  useEffect(() => {
    if (userInfo.roomId !== undefined) {
      if (client === null) {
        // 소켓에 처음 연결하는 경우
        initSocket();
      } else {
        // 소켓에 연결한 적이 있는 경우 (room 전환)
        client.unsubscribe(subscription.id);
        socketSubscription();
      }
    }
  }, [userInfo.roomId]);

  return (
    <SocketContext.Provider
      value={{
        messages: messagesObject,
        client: client,
        roundStart: roundStartObject,
        roundEnd: roundEndObject,
        songVersion: songVersionObject,
        isOwnerTurn: isOwnerTurnObject,
      }}
    >
      {children}
    </SocketContext.Provider>
  );
}
