import './Vote.css';
import React, { useEffect, useState } from 'react';

const Vote = () => {
    const [voteA, setVoteA] = useState(1);
    const [voteB, setVoteB] = useState(1);
    const [progA, setProgA] = useState(50);
    const [progB, setProgB] = useState(50);
    let total = voteA + voteB;
  
    const getProgress = (vote, total) => 
      (1 - (vote / total) || 0) * 100;
  
    const createBubbleEffect = (target) => {
      const $bubble = document.createElement('div');
      $bubble.className = 'vote-option-bubble';
      target.appendChild($bubble);
      setTimeout(() => {
        $bubble.remove();
      }, 500);
    }
  
    const updateVoteOption = (event, type) => {
      (type === 'A') ? setVoteA(voteA + 1) : setVoteB(voteB + 1);
      createBubbleEffect(event.currentTarget);
    }
  
    useEffect(() => {
      setProgA(getProgress(voteA , total));
      setProgB(getProgress(voteB , total));
    }, [voteA, voteB, total])
  
    return (
      <div className="vote">
        <div className="vote-progress-container">
          <div className="vote-progress vote-progress__a" style={{ transform: `translateX(-${progA}%)` }} />
          <div className="vote-progress vote-progress__b" style={{ transform: `translateX(${progB}%)` }} />
        </div>
        <div className="vote-option vote-option__a" onClick={(event) => { updateVoteOption(event, 'A') }}>
          <div className="vote-option-button vote-option-button__a" />
        </div>
        <div className="vote-option vote-option__b" onClick={(event) => { updateVoteOption(event, 'B') }}>
          <div className="vote-option-button vote-option-button__b" />
        </div>
        <div className="vote-count vote-count__a">{voteA}</div>
        <div className="vote-count vote-count__b">{voteB}</div>
      </div>
    );
}

export default Vote;