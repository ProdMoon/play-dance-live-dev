import { useEffect } from 'react';
import axios from 'axios';

import { Avatar, Box, Button, Grid, Typography } from '@mui/material';

import { useLoginContext } from '../../context/LoginContext';
import { useSocketContext } from '../../context/SocketContext';

const GoogleLogin = () => {
  const [userInfo, setUserInfo] = useLoginContext();
  const socketContext = useSocketContext();
  const [gameInfo, setGameInfo] = socketContext.gameInfo;

  useEffect(async () => {
    if (userInfo.userName === undefined) {
      try {
        const response = await axios.get('/api/userinfo', {
          headers: { 'Content-Type': 'application/json' },
        });
        if (response !== null) {
          setUserInfo((prevState) => ({
            ...prevState,
            userName: response.data.name,
            userEmail: response.data.email,
            userPicture: response.data.picture,
          }));
        }
      } catch (e) {
        console.error(e);
      }
    }
  }, [userInfo.userName]);

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
          isPublisher: false,
          isRoomOwner: false,
          songs: undefined,
        });
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <>
      {userInfo.userName === undefined ? (
        <Grid item xs>
          <Button href='/oauth2/authorization/google' variant='outlined'>
            GOOGLE LOGIN
          </Button>
        </Grid>
      ) : null}
      {userInfo.userName !== undefined ? (
        <>
          <Grid
            container
            direction='column'
            spacing={2}
            item
            justifyContent='center'
            alignItems='center'
            xs
          >
            <Grid item xs>
              <Avatar
                alt='profile image'
                src={userInfo.userPicture}
                sx={{ width: 70, height: 70 }}
              />
            </Grid>
            <Grid item xs>
              <Typography variant='h5'>{userInfo.userName}</Typography>
            </Grid>
            <Grid item xs>
              <Button
                onClick={(e) => {
                  handleLogout(e);
                }}
                variant='outlined'
              >
                LOGOUT
              </Button>
            </Grid>
          </Grid>
        </>
      ) : null}
    </>
  );
};

export default GoogleLogin;
