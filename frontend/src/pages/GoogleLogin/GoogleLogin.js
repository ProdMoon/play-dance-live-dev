import { Avatar, Box, Button, Typography } from "@mui/material";
import axios from "axios";
import { useEffect, useState } from "react";

const GoogleLogin = () => {
  const [userName, setUserName] = useState(undefined);
  const [userEmail, setUserEmail] = useState(undefined);
  const [userPicture, setUserPicture] = useState(undefined);

  useEffect(async () => {
    if (userName === undefined) {
      try {
        const response = await axios.get("/api/userinfo", {
          headers: { "Content-Type": "application/json" },
        });
        console.log(response);
        if (response !== null) {
          setUserName(response.data.name);
          setUserEmail(response.data.email);
          setUserPicture(response.data.picture);
        }
      } catch (e) {
        console.error(e);
      }
    }
  }, [userName]);

  const handleLogout = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post("/logout");
      if (response) {
        setUserName(undefined);
        setUserEmail(undefined);
        setUserPicture(undefined);
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <Box>
      {userName === undefined ? (
        <Box>
          <Button href="/oauth2/authorization/google">GOOGLE LOGIN</Button>
        </Box>
      ) : null}
      {userName !== undefined ? (
        <Box>
          <Avatar alt="profile image" src={userPicture} />
          <Typography>{userName}</Typography>
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
