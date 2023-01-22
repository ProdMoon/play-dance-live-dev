import { useEffect } from 'react';
import axios from 'axios';

import { Button, Grid } from '@mui/material';

import { useLoginContext } from '../../context/LoginContext';

const GoogleLogin = () => {
  const [userInfo, setUserInfo] = useLoginContext();

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

  return (
    <>
      {userInfo.userName === undefined ? (
        <Grid item xs>
          <Button href='/oauth2/authorization/google' variant='contained'>
            GOOGLE LOGIN
          </Button>
        </Grid>
      ) : null}
    </>
  );
};

export default GoogleLogin;
