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

  const gameInfoObject = useState({
    sender: null,
    type: null,
    currentRound: 0,
    songVersion: 'normal',
    connectionId: null,
    poll: null,
  });

  // for vote
  const voteAObject = useState(1);
  const voteBObject = useState(1);
  const progAObject = useState(50);
  const progBObject = useState(50);
  // for slot
  const slotNumObject = useState(undefined);

  // 자식 요소들에 read-only로 제공되는 States
  const [client, setClient] = useState(null);

  // 여기서만 사용하는 States
  const [subscription, setSubscription] = useState(null);

  function socketSubscription() {
    const [messages, setMessages] = messagesObject;
    const username = userInfo.userName ?? undefined;
    const [voteA, setVoteA] = voteAObject;
    const [voteB, setVoteB] = voteBObject;
    const [gameInfo, setGameInfo] = gameInfoObject;

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
          setGameInfo((prevState) => ({
            ...prevState,
            sender: messageBody.sender,
            type: messageBody.type, // 'ROUND_START'
            currentRound: messageBody.currentRound,
            songVersion: messageBody.songVersion,
            connectionId: messageBody.connectionId,
          }));
        }

        // 라운드 종료 신호...
        if (messageBody.type === 'ROUND_END') {
          setGameInfo((prevState) => ({
            ...prevState,
            sender: messageBody.sender,
            type: messageBody.type, // 'ROUND_END'
            currentRound: messageBody.currentRound,
            songVersion: messageBody.songVersion,
          }));
        }

        // 투표 시작 신호...
        if (messageBody.type === 'VOTE_START') {
          setGameInfo((prevState) => ({
            ...prevState,
            sender: messageBody.sender,
            type: messageBody.type, // 'VOTE_START'
            currentRound: messageBody.currentRound,
            songVersion: messageBody.songVersion,
          }));
        }

        // 투표 종료 신호...
        if (messageBody.type === 'VOTE_END') {
          setGameInfo((prevState) => ({
            ...prevState,
            sender: messageBody.sender,
            type: messageBody.type, // 'VOTE_END'
            currentRound: messageBody.currentRound,
            songVersion: messageBody.winner,
            poll: messageBody.poll,
          }));
        }

        // !! Temporary !! : 방장을 위한 투표 종료 신호...
        if (messageBody.type === 'VOTE_END_SIGNAL') {
          setGameInfo((prevState) => ({
            ...prevState,
            sender: messageBody.sender,
            type: messageBody.type, // 'VOTE_END_SIGNAL'
            currentRound: messageBody.currentRound,
            songVersion: messageBody.songVersion,
          }));
        }

        // 최종 투표 시작 신호...
        if (messageBody.type === 'FINAL_VOTE_START') {
          setGameInfo((prevState) => ({
            ...prevState,
            sender: messageBody.sender,
            type: messageBody.type, // 'FINAL_VOTE_START'
            currentRound: messageBody.currentRound,
            songVersion: messageBody.songVersion,
          }));
        }

        // 최종 투표 종료 신호...
        if (messageBody.type === 'FINAL_VOTE_END') {
          setGameInfo((prevState) => ({
            ...prevState,
            sender: messageBody.sender,
            type: messageBody.type, // 'FINAL_VOTE_END'
            currentRound: messageBody.currentRound,
            songVersion: messageBody.songVersion,
          }));
        }

        // vote animation
        if (messageBody.type === 'VOTE-click') {
          messageBody.value === 'A'
            ? setVoteA((prevState) => {
                return prevState + 1;
              })
            : setVoteB((prevState) => {
                return prevState + 1;
              });
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
        gameInfo: gameInfoObject,
        voteAs: voteAObject,
        voteBs: voteBObject,
        progAs: progAObject,
        progBs: progBObject,
        slotNums : slotNumObject,
      }}
    >
      {children}
    </SocketContext.Provider>
  );
}
