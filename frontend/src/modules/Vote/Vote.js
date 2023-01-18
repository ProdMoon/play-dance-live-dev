import './Vote.css';
import { useState, useEffect, useRef } from 'react';
import { useLoginContext } from '../../context/LoginContext';
import { useSocketContext } from '../../context/SocketContext';
import { Typography } from '@mui/material';

const Vote = (props) => {
  const upperText = props.upperText;
  const leftText = props.leftText;
  const rightText = props.rightText;

  // socket
  const [userInfo, setUserInfo] = useLoginContext();
  const socketContext = useSocketContext();
  const client = socketContext.client;
  const [voteA, setVoteA] = socketContext.voteAs;
  const [voteB, setVoteB] = socketContext.voteBs;
  const [progA, setProgA] = socketContext.progAs;
  const [progB, setProgB] = socketContext.progBs;

  // vote UI
  const getProgress = (vote) => (1 - vote / (voteA + voteB) || 0) * 100;

  const createBubbleEffect = (target) => {
    const $bubble = document.createElement('div');
    $bubble.className = 'vote-option-bubble';
    target.appendChild($bubble);
    setTimeout(() => {
      $bubble.remove();
    }, 500);
  };

  const sendClick = (event, value) => {
    createBubbleEffect(event.currentTarget);
    const Click = {
      type: 'VOTE-click',
      value: value,
      roomId: userInfo.roomId,
    };
    client.send('/app/chat.sendClick', {}, JSON.stringify(Click));
  };

  // useEffect(() => {
  //   setProgA(getProgress(voteA));
  //   setProgB(getProgress(voteB));
  // }, [voteA, voteB])

  const d = 5;
  useEffect(() => {
    setProgB((prevState) => {
      return prevState < 100 ? prevState + d : 100;
    });
    setProgA((prevState) => {
      return 0 < prevState ? prevState - d : 0;
    });
  }, [voteA]);

  useEffect(() => {
    setProgA((prevState) => {
      return prevState < 100 ? prevState + d : 100;
    });
    setProgB((prevState) => {
      return 0 < prevState ? prevState - d : 0;
    });
  }, [voteB]);

  return (
    <div className='vote'>
      {upperText.map((text) => {
        return (
          <Typography className='vote-upper-text' variant='h5'>
            {text}
          </Typography>
        );
      })}
      <div className='vote-main'>
        <div className='vote-progress-container'>
          <div
            className='vote-progress vote-progress__a'
            style={{ transform: `translateX(-${progA}%)` }}
          />
          <div
            className='vote-progress vote-progress__b'
            style={{ transform: `translateX(${progB}%)` }}
          />
        </div>
        <div
          className='vote-option vote-option__a'
          onClick={(event) => {
            sendClick(event, 'A');
          }}
        >
          <div className='vote-option-button vote-option-button__a'>
            <Typography variant='h5'>{leftText}</Typography>
          </div>
        </div>
        <div
          className='vote-option vote-option__b'
          onClick={(event) => {
            sendClick(event, 'B');
          }}
        >
          <div className='vote-option-button vote-option-button__b'>
            <Typography variant='h5'>{rightText}</Typography>
          </div>
        </div>
        <div className='vote-count vote-count__a'>{voteA}</div>
        <div className='vote-count vote-count__b'>{voteB}</div>
      </div>
    </div>
  );
};

export default Vote;
