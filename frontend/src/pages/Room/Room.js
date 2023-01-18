import { useEffect } from 'react';
import axios from 'axios';

import { useLoginContext } from '../../context/LoginContext';
import StreamArea from '../StreamArea/StreamArea';
import { Box, Typography } from '@mui/material';

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
    <Box display='flex' justifyContent='center' alignItems='center' className='containerItem'>
      {userInfo.roomId === undefined ? (
        <Box display='flex'>
          <Typography>열려있는 방이 없습니다</Typography>
        </Box>
      ) : null}
      {userInfo.roomId !== undefined ? <StreamArea /> : null}
    </Box>
  );
};

export default Room;
