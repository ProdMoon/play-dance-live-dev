import './Vote.css';
import { useState, useEffect, useRef } from 'react';
// import { useLoginContext } from '../../context/LoginContext';
import { useSocketContext } from '../../context/SocketContext';

const Vote = () => {
    // socket
    // const [userInfo, setUserInfo] = useLoginContext();
    // const socketContext = useSocketContext();
    // const client = socketContext.client;
    const [voteA, setVoteA] = useState(1);
    const [voteB, setVoteB] = useState(1);
    const [progA, setProgA] = useState(50);
    const [progB, setProgB] = useState(50);
  
    // vote UI
    const getProgress = (vote) => 
      (1 - (vote / (voteA + voteB)) || 0) * 100;
  
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
      // client.send('/app/chat.sendClick', {}, JSON.stringify(Click));
    };

    // useEffect(() => {
    //   setProgA(getProgress(voteA));
    //   setProgB(getProgress(voteB));
    // }, [voteA, voteB])
  
    const d = 5;
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
      <div className="vote">
        <div className="vote-progress-container">
          <div className="vote-progress vote-progress__a" style={{ transform: `translateX(-${progA}%)` }} />
          <div className="vote-progress vote-progress__b" style={{ transform: `translateX(${progB}%)` }} />
        </div>
        {/* <div className="vote-option vote-option__a" onClick={(event) => { sendClick(event, 'A') }}> */}
        <div className="vote-option vote-option__a hearts" onClick={(event) => { setVoteA((prevState)=>{return prevState+1}) }}>
          <div className="vote-option-button vote-option-button__a">X2</div>
        </div>
        {/* <div className="vote-option vote-option__b" onClick={(event) => { sendClick(event, 'B') }}> */}
        <div className="vote-option vote-option__b hearts" onClick={(event) => { setVoteB((prevState)=>{return prevState+1}) }}>
          <div className="vote-option-button vote-option-button__b">일반</div>
        </div>
        <div className="vote-count vote-count__a">{voteA}</div>
        <div className="vote-count vote-count__b">{voteB}</div>
      </div>
    );
}

export default Vote;