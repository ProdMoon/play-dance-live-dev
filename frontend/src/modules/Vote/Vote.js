import './Vote.css';
import { useEffect } from 'react';
import { useLoginContext } from '../../context/LoginContext';
import { useSocketContext } from '../../context/SocketContext';
import { Typography } from '@mui/material';

const Vote = (props) => {
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
    // const getProgress = (vote) => 
    //   (1 - (vote / (voteA + voteB)) || 0) * 100;
  
    const createBubbleEffect = (target) => {
      const $bubble = document.createElement('div');
      $bubble.className = 'vote-option-bubble';
      target.appendChild($bubble);
      setTimeout(() => {
        $bubble.remove();
      }, 500);
    }

    const makeHeart = (option) => {
          let b = Math.floor((Math.random() * 100) + 1);
          let d = ["flowOne", "flowTwo", "flowThree"];
          let a = ["colOne", "colTwo", "colThree", "colFour", "colFive", "colSix"];
          let c = (Math.random() * (1.6 - 1.2) + 1.2).toFixed(1);

          let newDiv = document.createElement("div");
            newDiv.className = "heart part-" + b + " " + a[Math.floor((Math.random() * 6))];
            newDiv.style.fontSize = Math.floor(Math.random() * (60 - 30) + 30) + "px";
            newDiv.innerHTML = '<span>❤</span>';
            document.querySelector(`.vote-option__${option}.hearts`).appendChild(newDiv);
            newDiv.style.animation = "" + d[Math.floor((Math.random() * 3))] + " " + c + "s linear";
            newDiv.style.display = 'block';

          setTimeout(function() {
              newDiv.remove()
          }, c * 900);
    }
  
    const sendClick = (event, value) => {
      createBubbleEffect(event.currentTarget);
      const Click = {
        type: 'VOTE-click',
        value: value,
        roomId: userInfo.roomId
      };
      client.send('/app/chat.sendClick', {}, JSON.stringify(Click));
    };

  // useEffect(() => {
  //   setProgA(getProgress(voteA));
  //   setProgB(getProgress(voteB));
  // }, [voteA, voteB])
  
    const d = 2;
    useEffect(() => {
      makeHeart('a');
      setProgB((prevState) => {return prevState < 100 ? prevState + d : 100});
      setProgA((prevState) => {return 0 < prevState ? prevState - d : 0});
    }, [voteA]);
    
    useEffect(() => {
      makeHeart('b');
      setProgA((prevState) => {return prevState < 100 ? prevState + d : 100});
      setProgB((prevState) => {return 0 < prevState ? prevState - d : 0});
    }, [voteB]);
  
  return (
    <div className='vote'>
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
          className='vote-option vote-option__a hearts'
          onClick={(event) => {
            sendClick(event, 'A');
          }}
        >
          <div className='vote-option-button vote-option-button__a'>
            <Typography variant='h6'>{leftText}</Typography>
          </div>
        </div>
        <div
          className='vote-option vote-option__b hearts'
          onClick={(event) => {
            sendClick(event, 'B');
          }}
        >
          <div className='vote-option-button vote-option-button__b'>
            <Typography variant='h6'>{rightText}</Typography>
          </div>
        </div>
        <div className='vote-count vote-count__a'>{voteA}</div>
        <div className='vote-count vote-count__b'>{voteB}</div>
      </div>
    </div>
  );
};

export default Vote;
