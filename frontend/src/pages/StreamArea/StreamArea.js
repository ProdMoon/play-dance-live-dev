import { OpenVidu } from "openvidu-browser";
import axios from "axios";
import React, { Component } from "react";

// Pages
import UserVideoComponent from "./UserVideoComponent";
import { Button, Grid, Typography } from "@mui/material";

// CSS
import "./StreamArea.css";

// 개발용과 배포용 코드가 다릅니다. 필요에 따라 주석을 해제하여 사용하세요.
// const APPLICATION_SERVER_URL = "https://192.168.0.62/"; // 개발용 URL
const APPLICATION_SERVER_URL = "https://boonthe.shop/"; // 배포용 URL

class StreamArea extends Component {
  constructor(props) {
    super(props);

    // These properties are in the state's component in order to re-render the HTML whenever their values change
    this.state = {
      mySessionId: "SessionA",
      myUserName: "Participant" + Math.floor(Math.random() * 100),
      session: undefined,
      mainStreamManager: undefined, // Main video of the page. Will be the 'publisher' or one of the 'subscribers'
      publisher: undefined,
      subscribers: [],
      myConnectionId: undefined,
    };

    this.joinSession = this.joinSession.bind(this);
    this.publishStream = this.publishStream.bind(this);
    this.unpublishStream = this.unpublishStream.bind(this);
    this.leaveSession = this.leaveSession.bind(this);
    this.switchCamera = this.switchCamera.bind(this);
    this.handleChangeSessionId = this.handleChangeSessionId.bind(this);
    this.handleChangeUserName = this.handleChangeUserName.bind(this);
    this.handleMainVideoStream = this.handleMainVideoStream.bind(this);
    this.onbeforeunload = this.onbeforeunload.bind(this);
    this.handleStreamMusic = this.handleStreamMusic.bind(this);
    this.handleChangeConnectionId = this.handleChangeConnectionId.bind(this);
  }

  componentDidMount() {
    window.addEventListener("beforeunload", this.onbeforeunload);
  }

  componentWillUnmount() {
    window.removeEventListener("beforeunload", this.onbeforeunload);
  }

  onbeforeunload(event) {
    this.leaveSession();
  }

  /* TODO:
   * handleChangeSessionId 와 handleChangeUserName 함수를
   * 대체해야 합니다.
   */

  handleChangeSessionId(e) {
    this.setState({
      mySessionId: e.target.value,
    });
  }

  handleChangeUserName(e) {
    this.setState({
      myUserName: e.target.value,
    });
  }

  // begin modified functions

  handleSetSessionId(id) {
    this.setState({
      mySessionId: id,
    });
  }

  handleSetUserName(name) {
    this.setState({
      myUserName: name,
    });
  }

  // end modified functions

  handleChangeConnectionId(connectionId) {
    this.setState({
      myConnectionId: connectionId,
    });
  }

  handleMainVideoStream(stream) {
    if (this.state.mainStreamManager !== stream) {
      this.setState({
        mainStreamManager: stream,
      });
    }
  }

  deleteSubscriber(streamManager) {
    let subscribers = this.state.subscribers;
    let index = subscribers.indexOf(streamManager, 0);
    if (index > -1) {
      subscribers.splice(index, 1);
      this.setState({
        subscribers: subscribers,
      });
    }
  }

  // 음악의 URL을 받아서 Session에 연결된 모든 사용자에게 playMusic signal을 보내고,
  // 현재 사용자의 브라우저에서도 재생합니다.
  handleStreamMusic(e, songUrl) {
    e.preventDefault();
    const song = new Audio(songUrl);
    this.state.session
      .signal({
        data: songUrl, // Any string (optional)
        to: [], // Array of Connection objects (optional. Broadcast to everyone if empty)
        type: "playMusic", // The type of message (optional)
      })
      .then(() => {
        console.log("playMusic signal successfully sent");
        song.loop = false;
        song.volume = 1.0;
        song.play();
      })
      .catch((error) => {
        console.error(error);
      });
  }

  joinSession() {
    // --- 1) Get an OpenVidu object ---

    this.OV = new OpenVidu();

    // --- 2) Init a session ---

    this.setState(
      {
        session: this.OV.initSession(),
      },
      () => {
        var mySession = this.state.session;

        // --- 3) Specify the actions when events take place in the session ---

        // On every new Stream received...
        mySession.on("streamCreated", (event) => {
          // Subscribe to the Stream to receive it. Second parameter is undefined
          // so OpenVidu doesn't create an HTML video by its own
          var subscriber = mySession.subscribe(event.stream, undefined);
          var subscribers = this.state.subscribers;
          subscribers.push(subscriber);

          // Update the state with the new subscribers
          this.setState({
            subscribers: subscribers,
          });
        });

        // On every Stream destroyed...
        mySession.on("streamDestroyed", (event) => {
          // Remove the stream from 'subscribers' array
          this.deleteSubscriber(event.stream.streamManager);
        });

        // On every asynchronous exception...
        mySession.on("exception", (exception) => {
          console.warn(exception);
        });

        // connection이 만들어질 때마다... (사용자 본인의 커넥션 포함)
        mySession.on("connectionCreated", (event) => {
          if (this.myConnectionId === undefined) {
            this.handleChangeConnectionId(event.connection.connectionId);
          }
        })

        // playMusic 시그널이 수신될 때마다...
        mySession.on("signal:playMusic", (event) => {
          const song = new Audio(event.data);
          song.loop = false;
          song.volume = 1.0;
          if (event.from !== this.state.myConnectionId) {
            song.play();
          }
          console.log(event.data); // Message
          console.log(event.from); // Connection object of the sender
          console.log(event.type); // The type of message
        });

        // --- 4) Connect to the session with a valid user token ---

        // Get a token from the OpenVidu deployment
        this.getToken().then((token) => {
          // First param is the token got from the OpenVidu deployment. Second param can be retrieved by every user on event
          // 'streamCreated' (property Stream.connection.data), and will be appended to DOM as the user's nickname
          mySession
            .connect(token, { clientData: this.state.myUserName })
            .then()
            .catch((error) => {
              console.log(
                "There was an error connecting to the session:",
                error.code,
                error.message
              );
            });
        });
      }
    );
  }

  async publishStream() {
    var mySession = this.state.session;

    // --- 5) Get your own camera stream ---

    // Init a publisher passing undefined as targetElement (we don't want OpenVidu to insert a video
    // element: we will manage it on our own) and with the desired properties
    let publisher = await this.OV.initPublisherAsync(undefined, {
      audioSource: undefined, // The source of audio. If undefined default microphone
      videoSource: undefined, // The source of video. If undefined default webcam
      publishAudio: true, // Whether you want to start publishing with your audio unmuted or not
      publishVideo: true, // Whether you want to start publishing with your video enabled or not
      resolution: "320x470", // The resolution of your video
      frameRate: 30, // The frame rate of your video
      insertMode: "APPEND", // How the video is inserted in the target element 'video-container'
      mirror: false, // Whether to mirror your local video or not
    });

    // --- 6) Publish your stream ---

    mySession.publish(publisher);

    // Obtain the current video device in use
    var devices = await this.OV.getDevices();
    var videoDevices = devices.filter((device) => device.kind === "videoinput");
    var currentVideoDeviceId = publisher.stream
      .getMediaStream()
      .getVideoTracks()[0]
      .getSettings().deviceId;
    var currentVideoDevice = videoDevices.find(
      (device) => device.deviceId === currentVideoDeviceId
    );

    // Set the main video in the page to display our webcam and store our Publisher
    this.setState({
      currentVideoDevice: currentVideoDevice,
      mainStreamManager: publisher,
      publisher: publisher,
    });
  }

  async unpublishStream() {
    // TODO: unpublish 구현
  }

  leaveSession() {
    // --- 7) Leave the session by calling 'disconnect' method over the Session object ---

    const mySession = this.state.session;

    if (mySession) {
      mySession.disconnect();
    }

    // Empty all properties...
    this.OV = null;
    this.setState({
      session: undefined,
      subscribers: [],
      mySessionId: "SessionA",
      myUserName: "Participant" + Math.floor(Math.random() * 100),
      mainStreamManager: undefined,
      publisher: undefined,
      myConnectionId: undefined,
    });
  }

  async switchCamera() {
    try {
      const devices = await this.OV.getDevices();
      var videoDevices = devices.filter(
        (device) => device.kind === "videoinput"
      );

      if (videoDevices && videoDevices.length > 1) {
        var newVideoDevice = videoDevices.filter(
          (device) => device.deviceId !== this.state.currentVideoDevice.deviceId
        );

        if (newVideoDevice.length > 0) {
          // Creating a new publisher with specific videoSource
          // In mobile devices the default and first camera is the front one
          var newPublisher = this.OV.initPublisher(undefined, {
            videoSource: newVideoDevice[0].deviceId,
            publishAudio: true,
            publishVideo: true,
            mirror: true,
          });

          //newPublisher.once("accessAllowed", () => {
          await this.state.session.unpublish(this.state.mainStreamManager);

          await this.state.session.publish(newPublisher);
          this.setState({
            currentVideoDevice: newVideoDevice[0],
            mainStreamManager: newPublisher,
            publisher: newPublisher,
          });
        }
      }
    } catch (e) {
      console.error(e);
    }
  }

  render() {
    const mySessionId = this.state.mySessionId;
    const myUserName = this.state.myUserName;

    return (
      <div className="containerItem">
        {this.state.session === undefined ? (
          <div id="join">
            {/* TODO: handleSetSessionId 와 handleSetUserName 으로
             * id와 name을 전달해야 합니다. */}
            {this.joinSession()}
          </div>
        ) : null}

        {this.state.session !== undefined ? (
          <Grid
            id="session"
            className="containerItem"
            container
            spacing={2}
            direction="column"
          >
            <Grid id="session-header" container item xs={1}>
              <Grid item xs>
                <Typography id="session-title" variant="h5">
                  방 번호 : {mySessionId}
                </Typography>
              </Grid>
              <Grid item xs>
                <Button
                  id="buttonLeaveSession"
                  onClick={this.leaveSession}
                  variant="text"
                >
                  세션 떠나기
                </Button>
                <Button
                  id="buttonPublishStream"
                  onClick={this.publishStream}
                  variant="text"
                >
                  스트리밍 시작
                </Button>
              </Grid>
            </Grid>

            {this.state.mainStreamManager !== undefined ? (
              <Grid id="main-video" item xs={1}>
                {/* <UserVideoComponent
                  streamManager={this.state.mainStreamManager}
                /> */}
                <Button
                  id="buttonSwitchCamera"
                  onClick={this.switchCamera}
                  variant="text"
                >
                  카메라 전환
                </Button>
                <Button
                  id="buttonStreamMusic"
                  onClick={(e) =>
                    this.handleStreamMusic(
                      e,
                      `${process.env.PUBLIC_URL}/resources/musics/attention_normal.mp3`
                    )
                  }
                  variant="text"
                >
                  어텐션을 틀어보자
                </Button>
              </Grid>
            ) : null}
            <Grid id="video-container" container item xs="auto">
              {this.state.publisher !== undefined ? (
                <Grid item xs>
                  <UserVideoComponent streamManager={this.state.publisher} />
                </Grid>
              ) : null}
              {this.state.subscribers.map((sub, i) => (
                <Grid item xs key={i}>
                  <UserVideoComponent streamManager={sub} />
                </Grid>
              ))}
            </Grid>
          </Grid>
        ) : null}
      </div>
    );
  }

  /**
   * --------------------------------------------
   * GETTING A TOKEN FROM YOUR APPLICATION SERVER
   * --------------------------------------------
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
  async getToken() {
    const sessionId = await this.createSession(this.state.mySessionId);
    return await this.createToken(sessionId);
  }

  async createSession(sessionId) {
    const response = await axios.post(
      APPLICATION_SERVER_URL + "api/sessions",
      {
        customSessionId: sessionId,
        songs: ["song1", "song2", "song3"],
      },
      {
        headers: { "Content-Type": "application/json" },
      }
    );
    return response.data; // The sessionId
  }

  async createToken(sessionId) {
    const response = await axios.post(
      APPLICATION_SERVER_URL + "api/sessions/" + sessionId + "/connections",
      {},
      {
        headers: { "Content-Type": "application/json" },
      }
    );
    return response.data; // The token
  }
}

export default StreamArea;
