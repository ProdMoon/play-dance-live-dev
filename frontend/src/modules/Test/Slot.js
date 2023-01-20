import './Test.css';
import { useEffect } from 'react';
import { Box, Grid } from '@mui/material';

const Test = () => {

    useEffect(() => {
        const normalBtn = document.getElementById("btn1");
        const video1Btn = document.getElementById("btn2");
        const video2Btn = document.getElementById("btn3");
        const videos = document.querySelectorAll(".video-comp");
        const video1 = document.getElementById("1");
        const video2 = document.getElementById("2");
    
        normalBtn.addEventListener("click", () => {
            videos.forEach((video) => {
                video.classList.remove("dancing", "resting");
            })
        });
    
        video1Btn.addEventListener("click", () => {
            video1.classList.remove("resting");
            video1.classList.add("dancing");
            
            video2.classList.remove("dancing");
            video2.classList.add("resting");
        });
    
        video2Btn.addEventListener("click", () => {
            video2.classList.remove("resting");
            video2.classList.add("dancing");
            
            video1.classList.remove("dancing");
            video1.classList.add("resting");
        });
      }, []);

  
    return (
    <Grid className='container' container spacing={4}>
      <Grid className='containerItem' item xs={3}>
        <Box className='backgroundPaper' elevation={5}>
            <button id="btn1">대기해라</button>
            <button id="btn2">video 1 춤춰라</button>
            <button id="btn3">video 2 춤춰라</button>
        </Box>
      </Grid>

      <Grid className='containerItem' item xs={6}>
        <Box className='backgroundPaper containerItem' elevation={5}>
        <div id="plane">
            <div class="container">
                <div class="flex">
                <div class="well content">
                    <p>Travel to...</p>

                    <div id="planeMachine">
                    <div class="text-center">
                        Madrid
                    </div>
                    <div class="text-center">
                        London
                    </div>
                    <div class="text-center">
                        NY
                    </div>
                    <div class="text-center">
                        Matrix
                    </div>
                    </div>
                </div>
                </div>
            </div>
        </div>
        </Box>
      </Grid>

      <Grid className='containerItem' item xs={3}>
        <Box className='backgroundPaper containerItem' elevation={5}>
        empty
        </Box>
      </Grid>
    </Grid> );
}

export default Slot;


