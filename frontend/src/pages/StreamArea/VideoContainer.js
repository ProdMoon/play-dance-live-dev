import UserVideoComponent from './UserVideoComponent';
import Fire from '../../modules/Fire/Fire'

const VideoContainer = (props) => {
  const myConnectionId = props.myConnectionId;
  const publisher = props.publisher;
  const subscribers = props.subscribers;
  const champion = props.champion;
  const challenger = props.challenger;
  const currentWinNums = props.currentWinNums;

  return (
    <>
      {champion !== null &&
      (champion === myConnectionId || challenger === myConnectionId) ? (
        myConnectionId === champion ? (
          <div id='video-container'>
            <div
              id={publisher.stream.connection.connectionId}
              className='video-comp'
            >
              <Fire />
              <Fire />
              <div className='winning-banner'>{currentWinNums ? currentWinNums + ' 연승' : '챔피언'}</div>
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
                        <div className='trying-banner'>도전자</div>
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
                        <Fire />
                        <Fire />
                        <div className='winning-banner'>{currentWinNums ? currentWinNums + ' 연승' : '챔피언'}</div>
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
              <div className='trying-banner'>도전자</div>
              <UserVideoComponent streamManager={publisher} />
            </div>
          </div>
        )
      ) : null}
      {champion !== null &&
      champion !== myConnectionId &&
      challenger !== myConnectionId ? (
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
                    <Fire />
                    <Fire />
                    <div className='winning-banner'>{currentWinNums ? currentWinNums + ' 연승' : '챔피언'}</div>
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
                    <div className='trying-banner'>도전자</div>
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
