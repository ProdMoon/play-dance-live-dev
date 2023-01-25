import { Avatar, Grid, Typography } from '@mui/material';
import { useLoginContext } from '../../context/LoginContext';

function UserProfile() {
  const [userInfo, setUserInfo] = useLoginContext();

  return (
    <div>
      <Avatar
        className='menubar-item'
        alt='profile image'
        src={userInfo.userPicture}
        sx={{ width: 70, height: 70 }}
      />
      <Typography className='menubar-item' variant='h5'>
        {userInfo.userName}
      </Typography>
    </div>
  );
}

export default UserProfile;
