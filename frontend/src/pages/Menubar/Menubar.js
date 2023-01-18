import { Grid } from '@mui/material';
import '../../styles/Menubar.css';

import CreateRoom from './CreateRoom';
import GoogleLogin from './GoogleLogin';

const Menubar = () => {
  return (
    <Grid container alignItems='center' direction='column' spacing={2} wrap='nowrap'>
      <Grid item xs={3}>
        <img
          className='menubar-logo'
          src={`${process.env.PUBLIC_URL}/resources/images/menubar-logo.png`}
        />
      </Grid>
      <Grid item xs={3}>
        <GoogleLogin />
      </Grid>
      <Grid item xs={6}>
        <CreateRoom />
      </Grid>
    </Grid>
  );
};

export default Menubar;
