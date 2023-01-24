import './Test.css';
import { useEffect } from 'react';
import { Box, Grid } from '@mui/material';

import Vote from '../Vote/Vote';

const Test = () => {

    useEffect(() => {
        const normalBtn = document.getElementById("btn1");
        const championBtn = document.getElementById("btn2");
        const challengerBtn = document.getElementById("btn3");
        const stage = document.querySelector(".stage");

        const videos = document.querySelectorAll(".video-comp");
        const champion = document.getElementById("1");
        const challenger = document.getElementById("2");
    
        normalBtn.addEventListener("click", () => {
            videos.forEach((video) => {
                video.classList.remove("dancing", "champion-resting", "challenger-resting");
              })
            stage.classList.remove("background-effect");
        });
    
        championBtn.addEventListener("click", () => {
            champion.classList.remove("champion-resting");
            champion.classList.add("dancing");
            stage.classList.add("background-effect");
            
            challenger.classList.remove("dancing");
            challenger.classList.add("challenger-resting");
        });
    
        challengerBtn.addEventListener("click", () => {
            challenger.classList.remove("challenger-resting");
            challenger.classList.add("dancing");
            
            champion.classList.remove("dancing");
            champion.classList.add("champion-resting");
        });

      }, []);

  
    return (
    <Grid className='container' container spacing={4}>
      <Grid className='containerItem' item xs={3}>
        <Box className='backgroundPaper' elevation={5}>
            <button id="btn1">대기해라</button>
            <button id="btn2">챔피언 춤춰라</button>
            <button id="btn3">도전자 춤춰라</button>
        </Box>
      </Grid>

      <Grid className='containerItem' item xs={6}>
        <Box className='backgroundPaper containerItem flex-column stage' elevation={5}>
        <div id='video-container'>

            <div id={1} className='video-comp'>
                <div className='streamcomponent'>video 1</div>
            </div>

            <div id={2} className='video-comp'>
                <div className='streamcomponent'>video 2</div>
            </div>

        </div>
        <Vote />
        </Box>
      </Grid>

      <Grid className='containerItem' item xs={3}>
        <Box className='backgroundPaper containerItem' elevation={5}>
        empty
        </Box>
      </Grid>
    </Grid> );
}

export default Test;


