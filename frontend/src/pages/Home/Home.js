import { createContext, useContext, useState } from 'react';

import { Grid, Paper } from '@mui/material';

import GoogleLogin from '../GoogleLogin/GoogleLogin';
import Room from '../Room/Room';
import Chat from '../Chat/Chat';

export const LoginContext = createContext();

const LoginContextProvider = ({ children }) => {
  const userInfoObject = useState({
    userName: undefined,
    userEmail: undefined,
    userPicture: undefined,
    roomId: undefined,
  });
  return (
    <LoginContext.Provider value={userInfoObject}>
      {children}
    </LoginContext.Provider>
  );
};

export function useLoginContext() {
  const value = useContext(LoginContext);
  return value;
}

const Home = () => {
  return (
    <LoginContextProvider>
      <Grid className='container' container spacing={2}>
        <Grid className='containerItem' item xs={3}>
          <Paper className='containerItem' elevation={5}>
            <GoogleLogin />
          </Paper>
        </Grid>
        <Grid className='containerItem' item xs={6}>
          <Paper className='containerItem' elevation={5}>
            <Room />
          </Paper>
        </Grid>
        <Grid className='containerItem' item xs={3}>
          <Paper className='containerItem' elevation={5}>
            <Chat />
          </Paper>
        </Grid>
      </Grid>
    </LoginContextProvider>
  );
};

export default Home;
