import { OpenVidu } from 'openvidu-browser';
import axios from 'axios';
import { useEffect, useState, createRef } from 'react';

import './StreamArea.css';
import { Button, Grid, Typography } from '@mui/material';

import UserVideoComponent from './UserVideoComponent';
import { useLoginContext } from '../Home/Home';

const APPLICATION_SERVER_URL = `https://${process.env.REACT_APP_HOST}/`;

const StreamArea = () => {
  const [OV, setOV] = useState(null);
  const [mySessionId, setMySessionId] = useState('SessionA');
  const [myUserName, setMyUserName] = useState(
    '익명' + Math.floor(Math.random() * 100),
  );
  const [session, setSession] = useState(undefined);
  const [mainStreamManager, setMainStreamManager] = useState(undefined);
  const [publisher, setPublisher] = useState(undefined);
  const [subscribers, setSubscribers] = useState([]);
  const [currentVideoDevice, setCurrentVideoDevice] = useState(undefined);
  const [myConnectionId, setMyConnectionId] = useState(undefined);
  const [currentSongUrl, setCurrentSongUrl] = useState(
    `${process.env.PUBLIC_URL}/resources/musics/attention_normal.mp3`,
  );
  const [songPlayFlag, setSongPlayFlag] = useState(false);

  const [userInfo, setUserInfo] = useLoginContext();

  const audioRef = createRef();
  const localAudioRef = createRef();

  useEffect(() => {
    if (session === undefined) {
      joinSession();
    }

    window.addEventListener('beforeunload', onbeforeunload);
    return () => {
      window.removeEventListener('beforeunload', onbeforeunload);
    };
  }, []);

  const onbeforeunload = (event) => {
    leaveSession();
  };

  const handleSetSessionId = (id) => {
    setMySessionId(id);
  };

  const handleSetUserName = (name) => {
    setMyUserName(name);
  };

  const handleChangeConnectionId = (connectionId) => {
    setMyConnectionId(connectionId);
  };

  const handleMainVideoStream = (stream) => {
    if (mainStreamManager !== stream) {
      setMainStreamManager(stream);
    }
  };

  const deleteSubscriber = (streamManager) => {
    let newSubscribers = [...subscribers];
    let index = subscribers.indexOf(streamManager, 0);
    if (index > -1) {
      newSubscribers.splice(index, 1);
      setSubscribers(newSubscribers);
    }
  };

  const handleChangeSongPlayFlag = (e, boolean) => {
    e.preventDefault();
    setSongPlayFlag(boolean);
  };

  const createAudioSource = async () => {
    const audioCtx = new AudioContext();
    const dest = audioCtx.createMediaStreamDestination();
    const source = audioCtx.createMediaElementSource(audioRef.current);
    source.connect(dest);
    const audioSource = dest.stream; // audioSource 변수가 openvidu 연결에 사용할 audiosource 입니다.
    return audioSource;
  };

  const handleChangeAudioSource = async (e) => {
    e.preventDefault();
    // TODO: openvidu 현재 publisher의 audiosource를 source로 바꿔주어야...
    const source = await createAudioSource();
    const mediaStream = await OV.getUserMedia({
      audioSource: source.getTracks()[0],
    });
    await publisher.replaceTrack(mediaStream.getAudioTracks()[0]);
    console.info('changed audiosource to mp3!!');
  };

  const handlePlaySong = (e) => {
    e.preventDefault();
    audioRef.current.play();
    localAudioRef.current.play();
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
    setSession(undefined);
    setSubscribers([]);
    setMySessionId('SessionA');
    setMyUserName('Participant' + Math.floor(Math.random() * 100));
    setMainStreamManager(undefined);
    setPublisher(undefined);
    setMyConnectionId(undefined);
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
            mirror: false,
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
        <Grid
          id='session'
          className='containerItem'
          container
          spacing={2}
          direction='column'
        >
          <Grid id='session-header' container item xs={1}>
            <Grid item xs>
              <Typography id='session-title' variant='h5'>
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
              <Button
                id='buttonPublishStream'
                onClick={publishStream}
                variant='text'
              >
                스트리밍 시작
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
              <Button
                id='buttonStreamSong'
                onClick={(e) => handleChangeAudioSource(e)}
                variant='text'
              >
                audiosource를 mp3 file로 바꾸기
              </Button>
              <Button onClick={(e) => handlePlaySong(e)}>mp3 재생</Button>
              <audio ref={audioRef} src={currentSongUrl} controls />
              <audio ref={localAudioRef} src={currentSongUrl} controls />
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
      ) : null}
    </div>
  );
};

export default StreamArea;
