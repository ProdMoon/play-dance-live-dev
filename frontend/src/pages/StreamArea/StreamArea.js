import { useEffect, useState, useRef } from 'react';
import { OpenVidu } from 'openvidu-browser';
import axios from 'axios';

import './StreamArea.css';
import { Button, Grid, Typography } from '@mui/material';

import UserVideoComponent from './UserVideoComponent';
import { useLoginContext } from '../../context/LoginContext';
import Vote from '../../modules/Vote/Vote';
import { useSocketContext } from '../../context/SocketContext';
import { SongListData } from '../../assets/songListData';

const APPLICATION_SERVER_URL = `https://${process.env.REACT_APP_HOST}/`;

// SongListData에서 곡 데이터를 가져오고, 이를 Map 자료형에 저장합니다.
const listVersionSongList = SongListData;
const songList = new Map(); // 우리가 사용할 곡 리스트
listVersionSongList.map((song) => {
  const [key, value] = song;
  songList.set(key, value);
});

const StreamArea = () => {
  const [userInfo, setUserInfo] = useLoginContext();
  const [OV, setOV] = useState(null);
  const [mySessionId, setMySessionId] = useState('default');
  const [myUserName, setMyUserName] = useState(
    userInfo.userName ?? '익명' + Math.floor(Math.random() * 100),
  );
  const [session, setSession] = useState(undefined);
  const [mainStreamManager, setMainStreamManager] = useState(undefined);
  const [publisher, setPublisher] = useState(undefined);
  const [subscribers, setSubscribers] = useState([]);
  const [currentVideoDevice, setCurrentVideoDevice] = useState(undefined);
  const [myConnectionId, setMyConnectionId] = useState(undefined);
  const [currentSongUrl, setCurrentSongUrl] = useState(null);

  // Websocket 관련 States
  const socketContextObjects = useSocketContext();
  const client = socketContextObjects.client;
  const [gameInfo, setGameInfo] = socketContextObjects.gameInfo;

  // Vote 관련 States
  const [voteA, setVoteA] = socketContextObjects.voteAs;
  const [voteB, setVoteB] = socketContextObjects.voteBs;
  const [voteView, setVoteView] = useState(false);
  const [winnerView, setWinnerView] = useState(false);
  const [songLabel, setSongLabel] = useState(null);

  const audioRef = useRef();
  const localAudioRef = useRef();

  useEffect(() => {
    if (userInfo.roomId !== undefined) {
      if (OV !== null) {
        narrowlyLeaveSession();
      }
      setMySessionId(userInfo.roomId);
      joinSession();
    }
    window.addEventListener('beforeunload', onbeforeunload);
    return () => {
      window.removeEventListener('beforeunload', onbeforeunload);
    };
  }, [userInfo.roomId]);

  const onbeforeunload = (event) => {
    leaveSession();
  };

  useEffect(() => {
    if (session !== undefined && userInfo.isPublisher === true) {
      publishStream();
    }
  }, [session]);

  useEffect(async () => {
    // 시작 신호 수신 시 행동
    if (gameInfo.type === 'ROUND_START') {
      // 먼저 열려있는 알림창을 닫습니다.
      setWinnerView(false);

      if (userInfo.isPublisher && gameInfo.sender !== userInfo.userEmail) {
        const songName = userInfo.songs[gameInfo.currentRound - 1];
        const songObject = songList.get(songName);

        // 틀어야 할 노래 version에 따라 알맞은 src를 넣어줍니다.
        switch (gameInfo.songVersion) {
          case 'normal':
            setCurrentSongUrl(songObject.normalSrc);
            break;
          case 'double':
            setCurrentSongUrl(songObject.doubleSrc);
            break;
          default:
            console.error('잘못된 song Version 요청입니다.');
        }

        // 스트림의 track을 audioSource로 설정해주고 노래를 재생합니다.
        await replaceTrackToAudioSource();
        handlePlaySong();
        localAudioRef.current.addEventListener('ended', sendRoundEndMessage);

        // Clean-up
        return () => {
          localAudioRef.current.removeEventListener(
            'ended',
            sendRoundEndMessage,
          );
        };
      }
    }

    // 종료 신호 수신 시 행동
    if (gameInfo.type === 'ROUND_END') {
      if (userInfo.isPublisher) {
        if (
          gameInfo.sender !== userInfo.roomOwner &&
          userInfo.userEmail === userInfo.roomOwner
        ) {
          // 신호를 보낸 사람이 방장이 아니고 내가 방장이면, 시작 신호를 보냅니다.
          client.send(
            '/app/chat.sendGameSignal',
            {},
            JSON.stringify({
              type: 'ROUND_START',
              sender: gameInfo.sender,
              roomId: userInfo.roomId,
              currentRound: gameInfo.currentRound,
              songVersion: gameInfo.songVersion,
            }),
          );
        } else if (
          gameInfo.sender === userInfo.roomOwner &&
          userInfo.userEmail !== userInfo.roomOwner
        ) {
          // 신호를 보낸 사람이 방장이고 내가 방장이 아니면, 투표 신호를 보냅니다.
          client.send(
            '/app/chat.sendGameSignal',
            {},
            JSON.stringify({
              type: 'VOTE_START',
              sender: userInfo.userEmail,
              roomId: userInfo.roomId,
              currentRound: gameInfo.currentRound,
              songVersion: gameInfo.songVersion,
            }),
          );
        }
      }
    }

    // 투표 시작 신호 수신 시 행동
    if (gameInfo.type === 'VOTE_START') {
      const songName = userInfo.songs[gameInfo.currentRound]; // songs는 index 0부터 시작하므로, currentRound를 사용하면 다음 라운드 곡을 지칭함.
      const songObject = songList.get(songName);

      setSongLabel(songObject.label);

      // 투표창을 띄웁니다.
      setVoteView(true);

      // 방장이라면, 8초를 센 후에 투표를 종료시킵니다.
      if (userInfo.roomOwner === userInfo.userEmail) {
        const tick = setTimeout(() => {
          sendVoteEndMessage();
        }, 8000);

        // Clean-up
        return () => clearTimeout(tick);
      }
    }

    // 투표 종료 신호 수신 시 행동
    if (gameInfo.type === 'VOTE_END') {
      // 투표창을 닫습니다.
      setVoteView(false);

      // 승리한 곡 버전을 표시합니다.
      setWinnerView(true);

      // 방장이라면, 3초 후에 다음 라운드 시작 신호를 보냅니다. (방장이 아닌 사람이 신호를 받을 예정)
      if (userInfo.isPublisher && userInfo.roomOwner === userInfo.userEmail) {
        const tick = setTimeout(() => {
          client.send(
            '/app/chat.sendGameSignal',
            {},
            JSON.stringify({
              type: 'ROUND_START',
              sender: userInfo.userEmail,
              roomId: userInfo.roomId,
              currentRound: gameInfo.currentRound + 1,
              songVersion: gameInfo.songVersion,
            }),
          );
        }, 3000);

        // Clean-up
        return () => clearTimeout(tick);
      }
    }
  }, [gameInfo.type]);

  const createAudioSource = async () => {
    const audioCtx = new AudioContext();
    const dest = audioCtx.createMediaStreamDestination();
    const source = audioCtx.createMediaElementSource(audioRef.current);
    source.connect(dest);
    const audioSource = dest.stream; // audioSource 변수가 openvidu 연결에 사용할 audiosource 입니다.
    return audioSource;
  };

  async function replaceTrackToAudioSource() {
    try {
      const source = await createAudioSource();
      const mediaStream = await OV.getUserMedia({
        audioSource: source.getTracks()[0],
      });
      await publisher.replaceTrack(mediaStream.getAudioTracks()[0]);
      console.info('Changed audiosource to mp3!!');
    } catch (e) {
      console.error(e);
    }
  }

  async function replaceTrackToAudioDevice() {
    try {
      const devices = await OV.getDevices();
      const audioDevices = devices.filter(
        (device) => device.kind === 'audioinput',
      );
      if (audioDevices) {
        const mediaStream = await OV.getUserMedia({
          audioSource: audioDevices[0].deviceId,
        });
        await publisher.replaceTrack(mediaStream.getAudioTracks()[0]);
        console.info('Changed audiosource to microphone!!');
      }
    } catch (e) {
      console.error(e);
    }
  }

  const handlePlaySong = () => {
    audioRef.current.play();
    localAudioRef.current.play();
  };

  const handleReady = (e) => {
    e.preventDefault();
    client.send(
      '/app/chat.sendGameSignal',
      {},
      JSON.stringify({
        type: 'ROUND_START',
        sender: userInfo.roomOwner,
        roomId: userInfo.roomId,
        currentRound: 1,
        songVersion: 'normal',
      }),
    );
  };

  function sendRoundEndMessage() {
    client.send(
      '/app/chat.sendGameSignal',
      {},
      JSON.stringify({
        type: 'ROUND_END',
        sender: userInfo.userEmail,
        roomId: userInfo.roomId,
        currentRound: gameInfo.currentRound,
        songVersion: gameInfo.songVersion,
      }),
    );
    replaceTrackToAudioDevice();
  }

  function sendVoteEndMessage() {
    client.send(
      '/app/chat.vote',
      {},
      JSON.stringify({
        type: 'VOTE_END',
        sender: userInfo.userEmail,
        roomId: userInfo.roomId,
        winner: voteA > voteB ? 'double' : 'normal',
        poll: voteA > voteB ? voteA : voteB,
        currentRound: gameInfo.currentRound,
      }),
    );
  }

  const deleteSubscriber = (streamManager) => {
    let newSubscribers = [...subscribers];
    let index = subscribers.indexOf(streamManager, 0);
    if (index > -1) {
      newSubscribers.splice(index, 1);
      setSubscribers(newSubscribers);
    }
  };

  const joinSession = () => {
    // 1) OpenVidu object를 받아옵니다.
    const newOV = new OpenVidu();

    // 2) Session을 초기화합니다.
    const mySession = newOV.initSession();

    // 3) Session에서 이벤트가 발생했을 때, 상황에 맞게 다음 동작들을 실행합니다.
    // 새로운 Stream을 수신하는 경우...
    mySession.on('streamCreated', (event) => {
      // Stream을 구독합니다. 두번째 인자가 undefined이므로
      // OpenVidu는 HTML video를 스스로 만들어내지 않습니다.
      const subscriber = mySession.subscribe(event.stream, undefined);
      let newSubscribers = [...subscribers];
      newSubscribers.push(subscriber);
      // 새로 만들어진 subscribers를 state에 반영합니다.
      setSubscribers(newSubscribers);
    });

    // Stream이 종료되는 경우...
    mySession.on('streamDestroyed', (event) => {
      // stream을 subscribers 리스트에서 제거합니다.
      deleteSubscriber(event.stream.streamManager);
    });

    // 비동기 예외가 발생하는 경우...
    mySession.on('exception', (exception) => {
      console.warn(exception);
    });

    // 4) 유효한 user token을 가지고 session에 연결합니다.

    // OpenVidu deployment (미디어 서버)에서 token을 받아옵니다.
    getToken().then((token) => {
      // 첫번째 인자는 OpenVidu deployment에서 받아온 token입니다.
      // 두번째 인자는 'streamCreated' 이벤트가 발생한 모든 유저로부터 받아올 수 있으며,
      // 이것이 DOM에 추가되는 user의 nickname입니다.
      mySession
        .connect(token, { clientData: myUserName })
        .then(() => {
          setOV(newOV);
          setSession(mySession);
        })
        .catch((error) => {
          console.error(
            'There was an error connecting to the session:',
            error.code,
            error.message,
          );
        });
    });
  };

  const publishStream = async () => {
    const mySession = session;

    // 5) 당신의 고유한 카메라 stream을 획득합니다.

    // undefined를 targetElement로 하여 publisher를 초기화합니다.
    // (OpenVidu가 video element를 삽입하도록 하고 싶지 않기 때문입니다. 우리가 직접 관리할 것입니다.)
    // 그리고 알맞은 속성값도 넣어줍니다.
    let publisher = await OV.initPublisherAsync(undefined, {
      audioSource: undefined, // 오디오 소스입니다. undefined이면 기본 마이크가 설정됩니다.
      videoSource: undefined, // 비디오 소스입니다. undefined이면 기본 웹캠이 설정됩니다.
      publishAudio: true, // false이면 오디오가 음소거인 상태로 시작됩니다.
      publishVideo: true, // false이면 비디오가 꺼진 상태로 시작됩니다.
      resolution: '320x470', // 비디오의 해상도를 조정합니다.
      frameRate: 30, // 비디오의 프레임레이트를 조정합니다.
      insertMode: 'APPEND', // 비디오가 target element인 'video-container'에 삽입되는 방식을 설정합니다.
      mirror: true, // 로컬 비디오를 미러링 할 것인지 여부를 설정합니다.
    });

    // 6) stream을 publish합니다.

    mySession.publish(publisher);

    // 현재 사용중인 비디오 디바이스를 획득합니다.
    const devices = await OV.getDevices();
    const videoDevices = devices.filter(
      (device) => device.kind === 'videoinput',
    );
    const currentVideoDeviceId = publisher.stream
      .getMediaStream()
      .getVideoTracks()[0]
      .getSettings().deviceId;
    const newCurrentVideoDevice = videoDevices.find(
      (device) => device.deviceId === currentVideoDeviceId,
    );

    // 우리의 웹캠을 표시하기 위해서 state를 페이지에서의 메인 비디오로 설정해주고, publisher를 저장해줍니다.
    setMainStreamManager(publisher);
    setPublisher(publisher);
    setCurrentVideoDevice(newCurrentVideoDevice);
  };

  const leaveSession = () => {
    // 7) Session object를 통해 'disconnect' 메서드를 호출함으로써 session을 종료합니다.
    const mySession = session;

    if (mySession) {
      mySession.disconnect();
    }

    // 모든 속성을 비워줍니다...
    setOV(null);
    setMySessionId('default');
    setMyUserName(
      userInfo.userName ?? '익명' + Math.floor(Math.random() * 100),
    );
    setSession(undefined);
    setMainStreamManager(undefined);
    setPublisher(undefined);
    setSubscribers([]);
    setCurrentVideoDevice(undefined);
    setMyConnectionId(undefined);

    // User 관련 속성도 비워줍니다...
    setUserInfo((prevState) => ({
      ...prevState,
      roomId: undefined,
      songs: undefined,
      isPublisher: false,
      roomOwner: false,
    }));

    // Game 관련 속성도 비워줍니다...
    setGameInfo((prevState) => ({
      ...prevState,
      sender: null,
      type: null,
      currentRound: 0,
      songVersion: 'normal',
      poll: null,
    }));
  };

  const narrowlyLeaveSession = () => {
    // 새로운 방송을 하기 위해 현재 방을 나갈 때만 호출되는 좁은 범위의 leaveSession입니다.
    const mySession = session;

    if (mySession) {
      mySession.disconnect();
    }

    // 모든 속성을 비워줍니다...
    setOV(null);
    setMySessionId('default');
    setMyUserName(
      userInfo.userName ?? '익명' + Math.floor(Math.random() * 100),
    );
    setSession(undefined);
    setMainStreamManager(undefined);
    setPublisher(undefined);
    setSubscribers([]);
    setCurrentVideoDevice(undefined);
    setMyConnectionId(undefined);

    // Game 관련 속성을 비워줍니다...
    setGameInfo((prevState) => ({
      ...prevState,
      sender: null,
      type: null,
      currentRound: 0,
      songVersion: 'normal',
      poll: null,
    }));
  };

  const switchCamera = async () => {
    try {
      const devices = await OV.getDevices();
      const videoDevices = devices.filter(
        (device) => device.kind === 'videoinput',
      );
      if (videoDevices && videoDevices.length > 1) {
        const newVideoDevice = videoDevices.filter(
          (device) => device.deviceId !== currentVideoDevice.deviceId,
        );

        if (newVideoDevice.length > 0) {
          // 비디오소스를 특정하여 새로운 publisher를 생성합니다.
          // 모바일 환경에서의 기본값은 전면 카메라입니다.
          const newPublisher = OV.initPublisher(undefined, {
            videoSource: newVideoDevice[0].deviceId,
            publishAudio: true,
            publishVideo: true,
            mirror: !publisher.properties.mirror,
          });

          // newPublisher.once('accessAllowed', () => {
          await session.unpublish(mainStreamManager);
          await session.publish(newPublisher);
          setCurrentVideoDevice(newVideoDevice[0]);
          setMainStreamManager(newPublisher);
          setPublisher(newPublisher);
        }
      }
    } catch (e) {
      console.error(e);
    }
  };

  /**
   * ----------------------------------------
   * 당신의 APPLICATION SERVER에서 TOKEN 받아오기
   * ----------------------------------------
   * The methods below request the creation of a Session and a Token to
   * your application server. This keeps your OpenVidu deployment secure.
   *
   * In this sample code, there is no user control at all. Anybody could
   * access your application server endpoints! In a real production
   * environment, your application server must identify the user to allow
   * access to the endpoints.
   *
   * Visit https://docs.openvidu.io/en/stable/application-server to learn
   * more about the integration of OpenVidu in your application server.
   */
  const getToken = async () => {
    const sessionId = await createSession(mySessionId);
    return await createToken(sessionId);
  };

  const createSession = async (sessionId) => {
    const response = await axios.post(
      APPLICATION_SERVER_URL + 'api/sessions',
      { customSessionId: sessionId },
      {
        headers: { 'Content-Type': 'application/json' },
      },
    );
    return response.data; // The sessionId
  };

  const createToken = async (sessionId) => {
    const response = await axios.post(
      APPLICATION_SERVER_URL + 'api/sessions/' + sessionId + '/connections',
      {},
      {
        headers: { 'Content-Type': 'application/json' },
      },
    );
    return response.data; // The token
  };
  /*********************************************************/

  return (
    <div className='containerItem'>
      {session !== undefined ? (
        <>
          <Grid
            id='session'
            className='containerItem'
            container
            spacing={2}
            direction='column'
          >
            <Grid id='session-header' container item xs={1}>
              <Grid item xs>
                <Typography id='session-title' variant='h6'>
                  방 번호 : {mySessionId}
                </Typography>
              </Grid>
              <Grid item xs>
                <Button
                  id='buttonLeaveSession'
                  onClick={leaveSession}
                  variant='text'
                >
                  세션 떠나기
                </Button>
              </Grid>
            </Grid>

            {mainStreamManager !== undefined ? (
              <Grid id='main-video' item xs={1}>
                {/* <UserVideoComponent
                  streamManager={mainStreamManager}
                /> */}
                <Button
                  id='buttonSwitchCamera'
                  onClick={switchCamera}
                  variant='text'
                >
                  카메라 전환
                </Button>
                <Button onClick={handleReady} variant='contained'>
                  READY
                </Button>
                <audio ref={audioRef} src={currentSongUrl} />
                <audio ref={localAudioRef} src={currentSongUrl} />
              </Grid>
            ) : null}
            <Grid id='video-container' container item xs='auto'>
              {publisher !== undefined ? (
                <Grid item xs>
                  <UserVideoComponent streamManager={publisher} />
                </Grid>
              ) : null}
              {subscribers.map((sub, i) => (
                <Grid item xs key={i}>
                  <UserVideoComponent streamManager={sub} />
                </Grid>
              ))}
            </Grid>
          </Grid>
        </>
      ) : null}
      {voteView ? (
        <div className='vote-container'>
          <Typography>다음 라운드 곡은 {songLabel} 입니다!</Typography>
          <Vote />
        </div>
      ) : null}
      {winnerView ? (
        <div className='vote-container'>
          <Typography>
            다음 라운드는 {songLabel}의 {gameInfo.songVersion}버전으로
            진행됩니다!
          </Typography>
          <Vote />
        </div>
      ) : null}
    </div>
  );
};

export default StreamArea;
