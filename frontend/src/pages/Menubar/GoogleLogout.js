import axios from 'axios';

import { Button } from '@mui/material';

import { useLoginContext } from '../../context/LoginContext';
import { useSocketContext } from '../../context/SocketContext';

function GoogleLogout() {
  const [userInfo, setUserInfo] = useLoginContext();
  const socketContext = useSocketContext();
  const [gameInfo, setGameInfo] = socketContext.gameInfo;

  const handleLogout = async (e) => {
    e.preventDefault();
    try {
      if (userInfo.roomId !== undefined) {
        // api room leave 요청을 보냅니다.
        await axios.post(
          '/api/room/leave',
          { userId: userInfo.userEmail, roomId: userInfo.roomId },
          {
            headers: { 'Content-Type': 'application/json' },
          },
        );

        // Game 관련 속성을 비워줍니다...
        setGameInfo((prevState) => ({
          ...prevState,
          sender: null,
          type: null,
          currentRound: 0,
          songVersion: 'normal',
          poll: null,
          connectionId: null,
        }));
      }

      // User 속성을 비워줍니다...
      const response = await axios.post('/logout');
      if (response) {
        setUserInfo({
          userName: undefined,
          userEmail: undefined,
          userPicture: undefined,
          roomId: undefined,
          songs: undefined,
          isPublisher: false,
          roomOwner: undefined,
        });
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className='menubar-item'>
      <Button
        onClick={(e) => {
          handleLogout(e);
        }}
        variant='outlined'
      >
        LOGOUT
      </Button>
    </div>
  );
}

export default GoogleLogout;
