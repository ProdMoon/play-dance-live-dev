import { Box, Button, Typography } from "@mui/material";
import { useState } from "react";

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
          <a href="/oauth2/authorization/google">Google Login</a>
        </Box>
      ) : null}
      {userName !== undefined ? (
        <Typography>아직 로그아웃 구현 전임.</Typography>
      ) : null}
    </Box>
  );
};

export default GoogleLogin;
