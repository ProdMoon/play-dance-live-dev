import { Button } from '@mui/material';
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
            console.log('current roomId : ' + data.roomId);
            setUserInfo((prevState) => ({
              ...prevState,
              roomId: data.roomId,
            }));
          });
      } catch (error) {
        console.error(error);
      }
    }
  }, []);

  const createRoom = (e) => {
    e.preventDefault();
    try {
      axios
        .post('/api/room/create', {
          userId: userInfo.userEmail ?? null,
          songs: ['attention', 'hype boy', 'ditto'],
        })
        .then((response) => {
          const data = response.data;
          console.log('created roomId : ' + data.roomId);
          setUserInfo((prevState) => ({
            ...prevState,
            roomId: data.roomId,
          }));
        });
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className='containerItem'>
      {userInfo.roomId === undefined ? (
        <div>
          열려있는 방이 없습니다
          <Button onClick={(e) => createRoom(e)} variant='text'>
            (로그인먼저하고) 방 생성하기
          </Button>
        </div>
      ) : null}
      {userInfo.roomId !== undefined ? <StreamArea /> : null}
    </div>
  );
};

export default Room;
