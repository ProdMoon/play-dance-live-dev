import { Typography, Box, Paper, Grid, TextField } from "@mui/material";
import { useState, useEffect, useRef } from "react";
import SockJS from "sockjs-client";
import Stomp from "stompjs";

const Chat = () => {
  const [messages, setMessages] = useState([]);
  const [message, setMessage] = useState("");
  const [client, setClient] = useState(null);
  const username = sessionStorage.getItem("id");
  const chattingViewRef = useRef();
  const label = "채팅";

  useEffect(() => {
    // 개발용과 배포용 코드가 다릅니다. 필요에 따라 주석을 해제하여 사용하세요.
    // const socket = new SockJS("https://192.168.0.62/api/ws"); // 개발용 URL
    const socket = new SockJS("https://boonthe.shop/api/ws"); // 배포용 URL
    const stompClient = Stomp.over(socket);
    setClient(stompClient);
    stompClient.connect({}, (frame) => {
      console.log("Connected: " + frame);
      // TODO : "topic/public"대신 "topic/{roomId}"로 바꿔야 함.
      stompClient.subscribe("/topic/public", (message) => {
        const messageBody = JSON.parse(message.body);
        if (messageBody.type === "CHAT") {
          // TODO: if (messageBody.roomId === currentRoomId) 일때만 채팅 수신
          setMessages((prevMessages) => [...prevMessages, message]);
          scrollDown();
        }
      });
      stompClient.send(
        "/app/chat.addUser",
        {},
        JSON.stringify({ sender: username, type: "JOIN" })
      );
    });
  }, []);

  const handleKeyPress = (event) => {
    if (event.key === "Enter") {
      event.preventDefault();
      sendMessage();
    }
  };

  const scrollDown = () => {
    chattingViewRef.current.scrollIntoView(false);
  };

  const sendMessage = () => {
    // e.preventDefault();
    const chatMessage = {
      sender: username,
      content: message,
      type: "CHAT",
    };
    client.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
    setMessage("");
  };

  const chattingView = () => {
    return (
      <div>
        {messages.map((message) => {
          const messageBody = JSON.parse(message.body);
          return (
            <Typography>
              {messageBody.sender + " : " + messageBody.content}
            </Typography>
          );
        })}
      </div>
    );
  };

  const inputBox = () => {
    return (
      <TextField
        fullWidth
        multiline
        rows={2}
        placeholder="Enter키로 메시지 전송"
        value={message}
        onChange={(event) => {
          setMessage(event.target.value);
        }}
        onKeyPress={handleKeyPress}
      />
    );
  };

  return (
    <Grid container direction="column" p={1.5} spacing={1}>
      <Grid item xs="auto">
        <Typography variant="h5">{label}</Typography>
      </Grid>
      <Grid
        item
        xs={9}
        sx={{
          overflowY: "scroll",
        }}
      >
        <Box sx={{ height: 500 }}>
          <div ref={chattingViewRef}>{chattingView()}</div>
        </Box>
      </Grid>
      <Grid item xs="auto">
        {inputBox()}
      </Grid>
    </Grid>
  );
};

export default Chat;
