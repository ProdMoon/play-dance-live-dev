import { Avatar, Grid, Typography } from '@mui/material';
import { useLoginContext } from '../../context/LoginContext';

function UserProfile() {
  const [userInfo, setUserInfo] = useLoginContext();

  return (
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
    </Grid>
  );
}

export default UserProfile;
