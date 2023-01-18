import { useState } from 'react';

import { Box, Grid, Paper } from '@mui/material';
import { createTheme, ThemeProvider } from '@mui/material/styles';

import Menubar from '../Menubar/Menubar';
import Room from '../Room/Room';
import Chat from '../Chat/Chat';
import LogoPage from './LogoPage';
import LoginContextProvider from '../../context/LoginContext';
import SocketContextProvider from '../../context/SocketContext';

const theme = createTheme({
  palette: {
    primary: {
      main: '#c083d7',
    },
  },
  typography: {
    fontFamily: ['SBAggroB'].join(','),
  },
});

const Home = () => {
  const [entered, setEntered] = useState(false);

  const handleEnter = () => {
    setEntered(true);
  };

  return entered ? (
    <ThemeProvider theme={theme}>
      <LoginContextProvider>
        <SocketContextProvider>
          <Grid className='container' container spacing={2}>
            <Grid className='containerItem' item xs={3}>
              <Box className='backgroundPaper containerItem' elevation={5}>
                <Menubar />
              </Box>
            </Grid>
            <Grid className='containerItem' item xs={6}>
              <Box className='backgroundPaper containerItem' elevation={5}>
                <Room />
              </Box>
            </Grid>
            <Grid className='containerItem' item xs={3}>
              <Box className='backgroundPaper containerItem' elevation={5}>
                <Chat />
              </Box>
            </Grid>
          </Grid>
        </SocketContextProvider>
      </LoginContextProvider>
    </ThemeProvider>
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
