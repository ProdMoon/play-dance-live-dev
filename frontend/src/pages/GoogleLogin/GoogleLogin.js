import { Box, Button, Typography } from '@mui/material';
import { useState } from 'react';

// const loginHandler = () => {
//   return (

//   )
// }

const GoogleLogin = () => {
  const [userName, setUserName] = useState(undefined);

  return (
    <Box>
      {userName === undefined ? (
        <Box>
          <Button
            onClick={() =>
              window.open('https://192.168.0.62/api/oauth2/authorization/google', '_blank')
            }
            variant='text'
          >
            Google Login
          </Button>
        </Box>
      ) : null}
      {userName !== undefined ? (
        <Typography>아직 로그아웃 구현 전임.</Typography>
      ) : null}
    </Box>
  );
};

export default GoogleLogin;
