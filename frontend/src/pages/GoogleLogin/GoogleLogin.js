import { Avatar, Box, Button, Typography } from "@mui/material";
import axios from "axios";
import { useEffect } from "react";
import { useLoginContext } from "../Home/Home";


const GoogleLogin = () => {
  const [userInfo, setUserInfo] = useLoginContext();
  
  useEffect(async () => {
    if (userInfo.userName === undefined) {
      try {
        const response = await axios.get("/api/userinfo", {
          headers: { "Content-Type": "application/json" },
        });
        console.log(response);
        if (response !== null) {
          setUserInfo({
            userName: response.data.name,
            userEmail: response.data.email,
            userPicture: response.data.picture,
          });
        }
      } catch (e) {
        console.error(e);
      }
    }
  }, [userInfo.userName]);

  const handleLogout = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post("/logout");
      if (response) {
        setUserInfo({
          userName: undefined,
          userEmail: undefined,
          userPicture: undefined,
        });
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <Box>
      {userInfo.userName === undefined ? (
        <Box>
          <Button href="/oauth2/authorization/google">GOOGLE LOGIN</Button>
        </Box>
      ) : null}
      {userInfo.userName !== undefined ? (
        <Box>
          <Avatar alt="profile image" src={userInfo.userPicture} />
          <Typography>{userInfo.userName}</Typography>
          <p />
          <Button
            onClick={(e) => {
              handleLogout(e);
            }}
          >
            LOGOUT
          </Button>
        </Box>
      ) : null}
    </Box>
  );
};

export default GoogleLogin;
