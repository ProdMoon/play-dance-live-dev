import UserVideoComponent from './UserVideoComponent';

const VideoContainer = (props) => {
  const myConnectionId = props.myConnectionId;
  const publisher = props.publisher;
  const subscribers = props.subscribers;
  const champion = props.champion;
  const challenger = props.challenger;

  return (
    <>
      {publisher !== undefined && champion !== null ? (
        myConnectionId === champion ? (
          <div id='video-container'>
            <div
              id={publisher.stream.connection.connectionId}
              className='video-comp'
            >
              <UserVideoComponent streamManager={publisher} />
            </div>
            {subscribers.length > 0
              ? subscribers.map((sub, i) => {
                  if (sub.stream.connection.connectionId === challenger) {
                    return (
                      <div
                        id={sub.stream.connection.connectionId}
                        key={i}
                        className='video-comp'
                      >
                        <UserVideoComponent streamManager={sub} />
                      </div>
                    );
                  }
                })
              : null}
          </div>
        ) : (
          <div id='video-container'>
            {subscribers.length > 0
              ? subscribers.map((sub, i) => {
                  if (sub.stream.connection.connectionId === champion) {
                    return (
                      <div
                        id={sub.stream.connection.connectionId}
                        key={i}
                        className='video-comp'
                      >
                        <UserVideoComponent streamManager={sub} />
                      </div>
                    );
                  }
                })
              : null}
            <div
              id={publisher.stream.connection.connectionId}
              className='video-comp'
            >
              <UserVideoComponent streamManager={publisher} />
            </div>
          </div>
        )
      ) : null}
      {publisher === undefined && champion !== null ? (
        <div id='video-container'>
          {subscribers.length > 0 ? (
            subscribers.map((sub, i) => {
              if (sub.stream.connection.connectionId === champion) {
                return (
                  <div
                    id={sub.stream.connection.connectionId}
                    key={i}
                    className='video-comp'
                  >
                    <UserVideoComponent streamManager={sub} />
                  </div>
                );
              }
            })
          ) : (
            <div className='video-comp' />
          )}
          {subscribers.length > 0 ? (
            subscribers.map((sub, i) => {
              if (sub.stream.connection.connectionId === challenger) {
                return (
                  <div
                    id={sub.stream.connection.connectionId}
                    key={i}
                    className='video-comp'
                  >
                    <UserVideoComponent streamManager={sub} />
                  </div>
                );
              }
            })
          ) : (
            <div className='video-comp' />
          )}
        </div>
      ) : null}
    </>
  );
};

export default VideoContainer;
