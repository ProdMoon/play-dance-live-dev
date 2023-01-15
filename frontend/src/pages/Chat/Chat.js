import { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { Typography, Box, Grid, TextField } from '@mui/material';
import { useLoginContext } from '../Home/Home';
import PopoverComponent from '../../components/PopoverComponent/PopoverComponent';

const Chat = () => {
  const [userInfo, setUserInfo] = useLoginContext();

  const [messages, setMessages] = useState([]);
  const [message, setMessage] = useState('');
  const [client, setClient] = useState(null);

  const [anchorEl, setAnchorEl] = useState(null); // for Popover

  const username = userInfo.userName ?? undefined;
  const chattingViewRef = useRef();
  const label = '채팅';

  useEffect(() => {
    if (userInfo.roomId !== undefined) {
      const socket = new SockJS(`https://${process.env.REACT_APP_HOST}/api/ws`);
      const stompClient = Stomp.over(socket);
      setClient(stompClient);
      stompClient.connect({}, (frame) => {
        console.info('[Chat] Connected: ' + frame);
        stompClient.subscribe(`/topic/public`, (message) => {
          const messageBody = JSON.parse(message.body);
          if (messageBody.type === 'CHAT') {
            setMessages((prevMessages) => [...prevMessages, message]);
            scrollDown();
          }
        });
        stompClient.send(
          '/app/chat.addUser',
          {},
          JSON.stringify({ sender: username, type: 'JOIN' }),
        );
      });
    }
  }, [userInfo.roomId]);

  const sendMessage = () => {
    const chatMessage = {
      sender: username,
      content: message,
      type: 'CHAT',
      roomId: userInfo.roomId,
    };
    client.send('/app/chat.sendMessage', {}, JSON.stringify(chatMessage));
    setMessage('');
  };

  const handleKeyPress = (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      if (username === undefined) {
        setAnchorEl(event.currentTarget);
      } else {
        sendMessage();
      }
    }
  };

  const scrollDown = () => {
    chattingViewRef.current.scrollIntoView(false);
  };

  const chattingView = () => {
    return (
      <div>
        {messages.map((message) => {
          const messageBody = JSON.parse(message.body);
          return (
            <Typography>
              {messageBody.sender + ' : ' + messageBody.content}
            </Typography>
          );
        })}
      </div>
    );
  };

  const inputBox = () => {
    return (
      <div>
        <TextField
          fullWidth
          multiline
          rows={2}
          placeholder='Enter키로 메시지 전송'
          value={message}
          onChange={(event) => {
            setMessage(event.target.value);
          }}
          onKeyPress={handleKeyPress}
        />
        <PopoverComponent
          useStateForAnchor={[anchorEl, setAnchorEl]}
          message='채팅에 참여하기 위해서는 로그인이 필요합니다!'
          position='top'
        />
      </div>
    );
  };

  return (
    <Grid container direction='column' p={1.5} spacing={1}>
      <Grid item xs='auto'>
        <Typography variant='h5'>{label}</Typography>
      </Grid>
      <Grid
        item
        xs={9}
        sx={{
          overflowY: 'scroll',
        }}
      >
        <Box sx={{ height: 500 }}>
          <div ref={chattingViewRef}>{chattingView()}</div>
        </Box>
      </Grid>
      <Grid item xs='auto'>
        {inputBox()}
      </Grid>
    </Grid>
  );
};

export default Chat;
