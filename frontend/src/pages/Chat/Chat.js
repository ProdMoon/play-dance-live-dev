import { useState, useEffect, useRef } from 'react';

import { Typography, Box, Grid, TextField } from '@mui/material';

import PopoverComponent from '../../modules/PopoverComponent/PopoverComponent';
import { useLoginContext } from '../../context/LoginContext';
import { useSocketContext } from '../../context/SocketContext';

const Chat = () => {
  const [userInfo, setUserInfo] = useLoginContext();
  const socketContext = useSocketContext();
  const client = socketContext.client;
  const [messages, setMessages] = socketContext.messages;

  const [message, setMessage] = useState('');
  const [anchorEl, setAnchorEl] = useState(null); // for Popover

  const chattingViewRef = useRef();

  const username = userInfo.userName ?? undefined;
  const label = '채팅';

  useEffect(() => {
    scrollDown();
  }, [messages]);

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
          InputProps={{
            style: { color: 'white' },
          }}
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
    <Grid container direction='column' p={1.5} spacing={1} wrap='nowrap'>
      <Grid item xs>
        <Typography variant='h5'>{label}</Typography>
      </Grid>
      <Grid
        item
        xs={11}
        sx={{
          overflowY: 'scroll',
        }}
      >
        <Box sx={{ width: '100%', height: '100%' }}>
          <div ref={chattingViewRef}>{chattingView()}</div>
        </Box>
      </Grid>
      <Grid item xs>
        {inputBox()}
      </Grid>
    </Grid>
  );
};

export default Chat;
