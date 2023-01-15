import axios from 'axios';
import { useEffect } from 'react';
import { useLoginContext } from '../Home/Home';
import StreamArea from '../StreamArea/StreamArea';

const Room = () => {
  const [userInfo, setUserInfo] = useLoginContext();

  useEffect(() => {
    if (userInfo.roomId === undefined) {
      try {
        axios
          .post('/api/room/enter', {
            userId: userInfo.userEmail ?? null,
            roomId: 'default',
            direction: 'current',
          })
          .then((response) => {
            const data = response.data;
            console.log(data);
            if (data.empty === false) {
              console.info('Current roomId is ' + data.roomId);
              setUserInfo((prevState) => ({
                ...prevState,
                roomId: data.roomId,
              }));
            } else {
              console.info('There is no available room.');
            }
          });
      } catch (error) {
        console.error(error);
      }
    }
  }, [userInfo.roomId]);

  return (
    <div className='containerItem'>
      {userInfo.roomId !== undefined ? <div>열려있는 방이 없습니다</div> : null}
      {userInfo.roomId === undefined ? <StreamArea /> : null}
    </div>
  );
};

export default Room;
