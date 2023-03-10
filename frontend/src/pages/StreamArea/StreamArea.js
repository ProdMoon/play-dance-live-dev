import { useEffect, useState, useRef } from 'react';
import { OpenVidu } from 'openvidu-browser';
import axios from 'axios';

import './StreamArea.css';
import {
  Button,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Typography,
} from '@mui/material';

import UserVideoComponent from './UserVideoComponent';
import { useLoginContext } from '../../context/LoginContext';
import Vote from '../../modules/Vote/Vote';
import Slot from '../../modules/Slot/Slot';

import { useSocketContext } from '../../context/SocketContext';
import { SongListData } from '../../assets/songListData';
import VideoContainer from './VideoContainer';

const APPLICATION_SERVER_URL = `https://${process.env.REACT_APP_HOST}/`;

// SongListData에서 곡 데이터를 가져오고, 이를 Map 자료형에 저장합니다.
const listVersionSongList = SongListData;
const songList = new Map(); // 우리가 사용할 곡 리스트
listVersionSongList.map((song) => {
  const [key, value] = song;
  songList.set(key, value);
});

const AudioTag = (props) => {
  const audioRef = props.audioRef;
  const localAudioRef = props.localAudioRef;
  const currentSongUrl = props.currentSongUrl;

  return (
    <div>
      <audio ref={audioRef} src={currentSongUrl} volume={0.5} />
      <audio ref={localAudioRef} src={currentSongUrl} volume={0.5} />
    </div>
  );
};

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

  // 곡 선택용
  const [selectedSong, setSelectedSong] = useState('');

  // Websocket 관련 States
  const socketContextObjects = useSocketContext();
  const client = socketContextObjects.client;
  const [gameInfo, setGameInfo] = socketContextObjects.gameInfo;

  // Vote 관련 States
  const [voteA, setVoteA] = socketContextObjects.voteAs;
  const [voteB, setVoteB] = socketContextObjects.voteBs;
  const [progA, setProgA] = socketContextObjects.progAs;
  const [progB, setProgB] = socketContextObjects.progBs;
  const [voteView, setVoteView] = useState(false);
  const [winnerView, setWinnerView] = useState(false);

  // Slot 관련 States
  const [slotView, setSlotView] = useState(false);
  const [slotNum, setSlotNum] = socketContextObjects.slotNums;

  // slotNum이 설정되고 나서 발동됩니다.
  useEffect(() => {
    let timeout = null;
    if (slotNum !== undefined) {
      // 슬롯을 표시해줍니다.
      setSlotView(true);
      slotAudio.current.pause();
      slotAudio.current.currentTime = 0;
      slotAudio.current.play();

      timeout = setTimeout(() => {
        setSlotView(false);
        setSlotNum(undefined);

        // champion의 차례이면 투표창을 띄워줍니다.
        if (gameInfo.connectionId === gameInfo.champion.connectionId) {
          setVoteView(true);
        }

        // 자기 차례면 시작
        if (gameInfo.connectionId === myConnectionId) {
          const songName = gameInfo.song;
          const songObject = songList.get(songName);
          setCurrentSongUrl(songObject.normalSrc);
        }

        // 카운트다운을 합니다.
        setCount(3);
      }, 7500);
    }
    return () => (timeout ? clearTimeout(timeout) : null);
  }, [slotNum]);

  // Audio 관련
  const [currentSongUrl, setCurrentSongUrl] = useState(null);
  const [currentAudioSource, setCurrentAudioSource] = useState(null);
  const audioRef = useRef();
  const localAudioRef = useRef();
  const slotAudio = useRef();
  const gameEndAudio = useRef();

  const onbeforeunload = (event) => {
    leaveSession();
  };

  useEffect(() => {
    if (session === undefined) {
      joinSession();
      window.addEventListener('beforeunload', onbeforeunload);
    }
    return () => window.removeEventListener('beforeunload', onbeforeunload);
  }, []);

  // joinSession()이 완료되었을 때 발동됩니다.
  useEffect(() => {
    if (session !== undefined) {
      if (userInfo.isParticipant === true) {
        // 본인이 참가하기를 통해 들어온 참가자이면 publish합니다. 영상은 바로 송출되지 않습니다.
        publishStream();
      } else {
        // 시청하기를 통해 들어온 시청자이면 join 요청을 보냅니다.
        client.send(
          '/app/enter',
          {},
          JSON.stringify({
            sender: userInfo.userEmail,
          }),
        );
      }
    }
  }, [session]);

  // waiters에 들어가기 위해 join 메시지를 보냅니다.
  // useEffect(() => {
  //   if (session !== undefined && userInfo.isParticipant === true) {
  //     client.send(
  //       '/app/join',
  //       {},
  //       JSON.stringify({
  //         sender: userInfo.userEmail,
  //         song: 'candy',
  //         connectionId: myConnectionId,
  //       }),
  //     );
  //   }
  // }, [myConnectionId]);

  function handleJoin() {
    client.send(
      '/app/join',
      {},
      JSON.stringify({
        sender: userInfo.userEmail,
        song: selectedSong,
        connectionId: myConnectionId,
      }),
    );
  }

  function handleSongChange() {
    client.send(
      '/app/song/change',
      {},
      JSON.stringify({
        sender: userInfo.userEmail,
        song: selectedSong,
        connectionId: myConnectionId,
      }),
    );
  }

  // 카운트다운 이펙트
  const [count, setCount] = useState(0);
  useEffect(() => {
    let timer = null;
    if (count > 0) {
      timer = setTimeout(() => {
        setCount(count - 1);
      }, 1000);
    }
    return () => (timer ? clearTimeout(timer) : null);
  }, [count]);

  /*************************
   *    게임 진행 관련 부분    *
   *************************/
  useEffect(async () => {
    let timeout = null;

    // 노래 시작 신호...
    if (gameInfo.type === 'SONG_START') {
      // 강조 표시를 모두 지워줍니다.
      const videos = document.querySelectorAll('.video-comp');
      const fires = document.querySelectorAll('.fire');
      videos.forEach((video) => {
        video.classList.remove(
          'dancing',
          'champion-resting',
          'challenger-resting',
        );
      });
      // fires.forEach((fire) => {
      //   fire.classList.remove('show');
      // })
      setWinnerView(false);

      // 수신한 connectionId가 방송할 차례이므로, 그 사람의 스트림을 강조해줍니다.
      let dancer = document.querySelector(
        `.video-comp#${gameInfo.connectionId}`,
      );
      let notDancer = document.querySelector(
        `.video-comp:not(#${gameInfo.connectionId})`,
      );
      if (gameInfo.champion.connectionId === gameInfo.connectionId) {
        dancer.classList.remove('champion-resting');
        dancer.classList.add('dancing');
        // fires.forEach((fire) => {
        //   fire.classList.add('show');
        // })

        notDancer.classList.remove('dancing');
        notDancer.classList.add('challenger-resting');
      } else if (gameInfo.challenger.connectionId === gameInfo.connectionId) {
        dancer.classList.remove('challenger-resting');

        dancer.classList.add('dancing');
        // fires.forEach((fire) => {
        //   fire.classList.remove('show');
        // })
        notDancer.classList.remove('dancing');
        notDancer.classList.add('champion-resting');
      }

      // 룰렛머신을 돌립니다.
      const songObjectIndex = songList.get(gameInfo.song).index;
      setSlotNum(songObjectIndex);
    }

    // 게임 종료 신호...
    if (gameInfo.type === 'GAME_END') {
      // 투표창을 닫고 투표기록을 초기화합니다.
      setVoteView(false);
      setVoteA(0);
      setVoteB(0);
      setProgA(50);
      setProgB(50);

      // 강조 표시를 모두 지워줍니다.
      const videos = document.querySelectorAll('.video-comp');
      videos.forEach((video) => {
        video.classList.remove(
          'dancing',
          'champion-resting',
          'challenger-resting',
        );
      });
      // fires.forEach((fire) => {
      //   fire.classList.remove('show');
      // })

      // 결과 효과음을 재생합니다.
      gameEndAudio.current.pause();
      gameEndAudio.current.currentTime = 0;
      gameEndAudio.current.play();

      // 결과창을 보여줍니다.
      setWinnerView(true);
    }

    // 새로운 게임 챌린지 신호...
    if (gameInfo.type === 'GAME_CHALLENGE') {
      if (gameInfo.challenger.connectionId === myConnectionId) {
        timeout = setTimeout(() => {
          console.info('나는 새로운 챌린저입니다.');
          songStart();
        }, 5000);
      }
    }

    // 참가자 리스트의 갱신... (join 시에도 발동)
    if (gameInfo.type === 'REFRESH_WAITER_LIST') {
      // 처음 들어왔는데 게임이 진행중인 상태라면...
      if (
        gameInfo.sender === userInfo.userEmail &&
        gameInfo.champion !== null
      ) {
        // 투표창을 보여줍니다.
        setVoteView(true);

        // 수신한 connectionId가 방송할 차례이므로, 그 사람의 스트림을 강조해줍니다.
        let dancer = document.querySelector(
          `.video-comp#${gameInfo.connectionId}`,
        );
        let notDancer = document.querySelector(
          `.video-comp:not(#${gameInfo.connectionId})`,
        );
        if (gameInfo.champion.connectionId === gameInfo.connectionId) {
          dancer.classList.remove('champion-resting');
          dancer.classList.add('dancing');
          // fires.forEach((fire) => {
          //   fire.classList.add('show');
          // })

          notDancer.classList.remove('dancing');
          notDancer.classList.add('challenger-resting');
        } else if (gameInfo.challenger.connectionId === gameInfo.connectionId) {
          dancer.classList.remove('challenger-resting');
          dancer.classList.add('dancing');

          // fires.forEach((fire) => {
          //   fire.classList.remove('show');
          // })
          notDancer.classList.remove('dancing');
          notDancer.classList.add('champion-resting');
        }
      }
    }
    return () => (timeout ? clearTimeout(timeout) : null);
  }, [gameInfo]);

  useEffect(async () => {
    if (currentSongUrl !== null) {
      if (currentAudioSource === null) {
        await replaceTrackToAudioSource();
      }
      handlePlaySong();
      localAudioRef.current.addEventListener('ended', sendRoundEndMessage);
    }
  }, [currentSongUrl]);

  const createAudioSource = async () => {
    const audioCtx = new AudioContext();
    const dest = audioCtx.createMediaStreamDestination();
    const source = audioCtx.createMediaElementSource(audioRef.current);
    source.connect(dest);
    const audioSource = dest.stream; // audioSource 변수가 openvidu 연결에 사용할 audiosource 입니다.
    setCurrentAudioSource(audioSource);
    return audioSource;
  };

  async function replaceTrackToAudioSource() {
    try {
      const source = currentAudioSource ?? (await createAudioSource());
      const mediaStream = await OV.getUserMedia({
        audioSource: source.getTracks()[0],
      });
      await publisher.replaceTrack(mediaStream.getAudioTracks()[0]);
      console.info('Changed audiosource to mp3!!');
    } catch (e) {
      console.error(e);
    }
  }

  /** 이 함수는 원래 sendRoundEndMessage에서 실행됨으로써,
   * 다시 디바이스의 마이크를 활성화 시키고자 하는 목적으로 만들었습니다.
   * 그러나 그렇게 하면 다시 replaceTrackToAudioSource를 실행했을 때,
   * 이미 mediaElement가 생성된 오디오 소스에 대해서는 재생성이 제한되는 이슈가 있어서
   * 부득이하게 이 함수를 사용하지 않기로 결정했습니다. */
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

  const handleStart = (e) => {
    e.preventDefault();
    client.send(
      '/app/start',
      {},
      JSON.stringify({
        type: 'GAME_START',
        sender: userInfo.userEmail,
      }),
    );
  };

  const handleCreate = (e) => {
    e.preventDefault();
    axios.post(
      '/api/room/create',
      {},
      {
        headers: { 'Content-Type': 'application/json' },
      },
    );
  };

  const handleSongStart = (e) => {
    e.preventDefault();
    client.send(
      '/app/song/start',
      {},
      JSON.stringify({
        type: 'SONG_START',
        sender: userInfo.userEmail,
      }),
    );
  };

  function songStart() {
    client.send(
      '/app/song/start',
      {},
      JSON.stringify({
        type: 'SONG_START',
        sender: userInfo.userEmail,
        // connectionId: myConnectionId,
      }),
    );
  }

  function sendRoundEndMessage() {
    client.send(
      '/app/song/end',
      {},
      JSON.stringify({
        type: 'SONG_END',
        sender: userInfo.userEmail,
      }),
    );
    // replaceTrackToAudioDevice();
    setCurrentSongUrl(null);
    localAudioRef.current.removeEventListener('ended', sendRoundEndMessage);
  }

  // 곡 선택용
  const handleSongSelect = (event) => {
    setSelectedSong(event.target.value);
  };

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
      // 새로 만들어진 subscribers를 state에 반영합니다.
      setSubscribers((prevSubscribers) => [...prevSubscribers, subscriber]);
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

    // Reconnection의 타임아웃을 없앱니다.
    newOV.setAdvancedConfiguration({
      noStreamPlayingEventExceptionTimeout: 5000000,
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
      resolution: '360x640', // 비디오의 해상도를 조정합니다.
      frameRate: 30, // 비디오의 프레임레이트를 조정합니다.
      insertMode: 'APPEND', // 비디오가 target element인 'video-container'에 삽입되는 방식을 설정합니다.
      mirror: true, // 로컬 비디오를 미러링 할 것인지 여부를 설정합니다.
    });

    // 6) stream을 publish합니다.

    mySession.publish(publisher);
    setMyConnectionId(publisher.stream.connection.connectionId);

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

  const unpublishStream = () => {
    const mySession = session;
    mySession.unpublish(publisher);
    setPublisher(undefined);
    setCurrentVideoDevice(undefined);
    setMainStreamManager(undefined);
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

    // api room leave 요청을 보냅니다.
    axios.post(
      '/api/room/leave',
      { userId: userInfo.userEmail, roomId: userInfo.roomId },
      {
        headers: { 'Content-Type': 'application/json' },
      },
    );

    // User 관련 속성도 비워줍니다...
    setUserInfo((prevState) => ({
      ...prevState,
      isPublisher: false,
      isParticipant: false,
      song: null,
      roomOwner: null,
    }));

    // Game 관련 속성도 비워줍니다...
    setGameInfo((prevState) => ({
      ...prevState,
      sender: null,
      type: null,
      currentRound: 0,
      songVersion: 'normal',
      poll: null,
      connectionId: null,
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
          const mediaStream = await OV.getUserMedia({
            videoSource: newVideoDevice[0].deviceId,
          });
          await publisher.replaceTrack(mediaStream.getVideoTracks()[0]);
          setCurrentVideoDevice(newVideoDevice[0]);
        }
      }
    } catch (e) {
      console.error(e);
    }
  };

  // const switchCamera = async () => {
  //   try {
  //     const devices = await OV.getDevices();
  //     const videoDevices = devices.filter(
  //       (device) => device.kind === 'videoinput',
  //     );
  //     if (videoDevices && videoDevices.length > 1) {
  //       const newVideoDevice = videoDevices.filter(
  //         (device) => device.deviceId !== currentVideoDevice.deviceId,
  //       );

  //       if (newVideoDevice.length > 0) {
  //         // 비디오소스를 특정하여 새로운 publisher를 생성합니다.
  //         // 모바일 환경에서의 기본값은 전면 카메라입니다.
  //         const newPublisher = OV.initPublisher(undefined, {
  //           videoSource: newVideoDevice[0].deviceId,
  //           publishAudio: true,
  //           publishVideo: true,
  //           mirror: !publisher.properties.mirror,
  //         });

  //         // newPublisher.once('accessAllowed', () => {
  //         await session.unpublish(mainStreamManager);
  //         await session.publish(newPublisher);
  //         setCurrentVideoDevice(newVideoDevice[0]);
  //         setMainStreamManager(newPublisher);
  //         setPublisher(newPublisher);
  //       }
  //     }
  //   } catch (e) {
  //     console.error(e);
  //   }
  // };

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
            wrap='nowrap'
          >
            {userInfo.isParticipant === true ? (
              <Grid id='session-header' item>
                <>
                  <FormControl fullWidth>
                    <InputLabel>곡 선택</InputLabel>
                    <Select
                      labelId='demo-simple-select-label'
                      id='demo-simple-select'
                      value={selectedSong}
                      label='곡 선택'
                      onChange={handleSongSelect}
                    >
                      <MenuItem value='antifragile'>ANTIFRAGILE</MenuItem>
                      <MenuItem value='pop'>POP!</MenuItem>
                      <MenuItem value='hypeboy'>Hype Boy</MenuItem>
                      <MenuItem value='thefeels'>The Feels</MenuItem>
                      <MenuItem value='russianroulette'>러시안 룰렛</MenuItem>
                      <MenuItem value='ditto'>Ditto</MenuItem>
                      <MenuItem value='nextlevel'>Next Level</MenuItem>
                    </Select>
                  </FormControl>
                  <Button
                    id='buttonLeaveSession'
                    onClick={leaveSession}
                    variant='outlined'
                    sx={{ margin: '5px' }}
                  >
                    세션 떠나기
                  </Button>
                  <Button
                    onClick={handleCreate}
                    variant='contained'
                    color='secondary'
                    sx={{ margin: '5px' }}
                  >
                    CREATE
                  </Button>
                  <Button
                    onClick={handleJoin}
                    variant='contained'
                    color='secondary'
                    sx={{ margin: '5px' }}
                  >
                    JOIN
                  </Button>
                  <Button
                    onClick={handleStart}
                    variant='contained'
                    color='secondary'
                    sx={{ margin: '5px' }}
                  >
                    START
                  </Button>
                  <Button
                    onClick={handleSongStart}
                    variant='contained'
                    color='secondary'
                    sx={{ margin: '5px' }}
                  >
                    SONG START
                  </Button>
                  <Button
                    onClick={handleSongChange}
                    variant='contained'
                    color='secondary'
                    sx={{ margin: '5px' }}
                  >
                    SONG CHANGE
                  </Button>
                </>
                {mainStreamManager !== undefined ? (
                  <>
                    <Button
                      id='buttonSwitchCamera'
                      onClick={switchCamera}
                      variant='outlined'
                      sx={{ margin: '5px' }}
                    >
                      카메라 전환
                    </Button>
                    <AudioTag
                      audioRef={audioRef}
                      localAudioRef={localAudioRef}
                      currentSongUrl={currentSongUrl}
                    />
                  </>
                ) : null}
              </Grid>
            ) : null}

            <Grid
              item
              xs='auto'
              sx={{
                height: '80%',
              }}
            >
              <VideoContainer
                myConnectionId={myConnectionId}
                publisher={publisher}
                subscribers={subscribers}
                champion={
                  gameInfo.champion ? gameInfo.champion.connectionId : null
                }
                challenger={
                  gameInfo.challenger ? gameInfo.challenger.connectionId : null
                }
                currentWinNums={
                  gameInfo.challenger ? gameInfo.champion.currentWinNums : null
                }
              />
            </Grid>
          </Grid>
        </>
      ) : null}
      {voteView ? (
        <div className='vote-container'>
          <Vote
            leftText={gameInfo.champion.name}
            rightText={gameInfo.challenger.name}
          />
        </div>
      ) : null}
      {winnerView ? (
        <div className='result-container'>
          <div className='result-background'>
            <Typography variant='h3'>축하합니다!</Typography>
            <Typography variant='h4'>
              챔피언 <span color='orange'>{gameInfo.champion.name}</span>{' '}
              {gameInfo.champion.currentWinNums}연승 중!
            </Typography>
          </div>
        </div>
      ) : null}

      {count === 3 ? (
        <img
          src={`${process.env.PUBLIC_URL}/resources/images/count3.png`}
          className={'countdown'}
        />
      ) : null}
      {count === 2 ? (
        <img
          src={`${process.env.PUBLIC_URL}/resources/images/count2.png`}
          className={'countdown'}
        />
      ) : null}
      {count === 1 ? (
        <img
          src={`${process.env.PUBLIC_URL}/resources/images/count1.png`}
          className={'countdown'}
        />
      ) : null}
      {slotView ? <Slot /> : null}
      <audio
        ref={slotAudio}
        src={`${process.env.PUBLIC_URL}/resources/musics/roulette.mp3`}
      />
      <audio
        ref={gameEndAudio}
        src={`${process.env.PUBLIC_URL}/resources/musics/game_end.mp3`}
      />
    </div>
  );
};

export default StreamArea;
