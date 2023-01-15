import { useState } from 'react';

import { Grid, Paper } from '@mui/material';

import Menubar from '../Menubar/Menubar';
import Room from '../Room/Room';
import Chat from '../Chat/Chat';
import LogoPage from './LogoPage';
import LoginContextProvider from '../../context/LoginContext';
import SocketContextProvider from '../../context/SocketContext';

const Home = () => {
  const [entered, setEntered] = useState(false);

  const handleEnter = () => {
    setEntered(true);
  };

  return entered ? (
    <LoginContextProvider>
      <SocketContextProvider>
        <Grid className='container' container spacing={2}>
          <Grid className='containerItem' item xs={3}>
            <Paper className='containerItem' elevation={5}>
              <Menubar />
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
      </SocketContextProvider>
    </LoginContextProvider>
  ) : (
    <div
      className='container'
      style={{
        display: 'flex',
        justifyContent: 'center',
      }}
    >
      <LogoPage handler={handleEnter} />
    </div>
  );
};

export default Home;
