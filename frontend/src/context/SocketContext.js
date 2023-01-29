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
    type: null,
    sender: null,
    song: null,
    champion: null,
    challenger: null,
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
  const [participantList, setParticipantList] = useState([]);
  const [rankingList, setRankingList] = useState([]);

  // 여기서만 사용하는 States
  const [subscription, setSubscription] = useState(null);

  function socketSubscription() {
    const [messages, setMessages] = messagesObject;
    const [voteA, setVoteA] = voteAObject;
    const [voteB, setVoteB] = voteBObject;
    const [gameInfo, setGameInfo] = gameInfoObject;

    setSubscription(
      // 다음과 같은 type들을 구독합니다.
      client.subscribe('/topic/public', (message) => {
        const messageBody = JSON.parse(message.body);

        // 채팅 요청...
        if (messageBody.type === 'CHAT') {
          setMessages((prevMessages) => [...prevMessages, message]);
        }

        // 참가자 리스트 변동...
        if (messageBody.type === 'REFRESH_WAITER_LIST') {
          const waiters = messageBody.waiters;
          const ranking = messageBody.rankingList;
          setParticipantList(waiters);
          setRankingList(ranking);
          setGameInfo((prevState) => ({
            ...prevState,
            type: messageBody.type, // REFRESH_WAITER_LIST
            sender: messageBody.sender,
            champion: messageBody.champion,
            challenger: messageBody.challenger,
            connectionId: messageBody.connectionId,
          }));
        }

        // 랭킹 리스트 변동...
        if (messageBody.type === 'REFRESH_RANKING_LIST') {
          const ranking = messageBody.ranking;
          setRankingList(ranking);
        }

        // 노래 재생 신호...
        if (messageBody.type === 'SONG_START') {
          setGameInfo((prevState) => ({
            ...prevState,
            type: messageBody.type, // SONG_START
            sender: messageBody.sender,
            connectionId: messageBody.connectionId,
            song: messageBody.song,
          }));
        }

        // 게임 종료 신호...
        if (messageBody.type === 'GAME_END') {
          if (messageBody.winner !== userInfo.userEmail) {
            setGameInfo((prevState) => ({
              ...prevState,
              type: messageBody.type, // GAME_END
              champion: messageBody.champion,
            }));
            const ranking = messageBody.rankingList;
            setRankingList(ranking);
          }
        }

        // 새로운 챌린지 신호...
        if (messageBody.type === 'GAME_CHALLENGE') {
          setGameInfo((prevState) => ({
            ...prevState,
            type: messageBody.type, // GAME_CHALLENGE
            sender: messageBody.sender,
            champion: messageBody.champion,
            challenger: messageBody.challenger,
            song: messageBody.song,
          }));
          const ranking = messageBody.rankingList;
          setRankingList(ranking);
          const waiters = messageBody.waiters;
          setParticipantList(waiters);
        }

        // 예외 케이스 : 처음에 start 상황
        if (messageBody.type === 'GAME_START') {
          setGameInfo((prevState) => ({
            ...prevState,
            type: messageBody.type, // GAME_START
            sender: messageBody.sender,
            champion: messageBody.champion,
            challenger: messageBody.challenger,
            song: messageBody.song,
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

        /*************************
         * DEPRECATED SIGNALS... *
         *************************/

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
            songVersion: messageBody.winner,
            poll: messageBody.poll,
          }));
        }
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
    if (client === null) {
      // 소켓에 처음 연결하는 경우
      initSocket();
    }
  }, []);

  return (
    <SocketContext.Provider
      value={{
        messages: messagesObject,
        client: client,
        gameInfo: gameInfoObject,
        rankingList: rankingList,
        participantList: participantList,
        voteAs: voteAObject,
        voteBs: voteBObject,
        progAs: progAObject,
        progBs: progBObject,
        slotNums: slotNumObject,
      }}
    >
      {children}
    </SocketContext.Provider>
  );
}
