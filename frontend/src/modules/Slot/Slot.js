 import './Slot.css';
import SlotMachine from 'jquery-slotmachine/dist/slotmachine.js';
import { SongListData } from '../../assets/songListData';

import { useEffect } from 'react';
import { useSocketContext } from '../../context/SocketContext';

const Slot = () => {
    const socketContextObjects = useSocketContext();
    const [slotNum, setSlotNum] = socketContextObjects.slotNums;

    // const listUp = () => {
    //     const list = SongListData;

    //     return (
    //       list.map((song) => {
    //         <div className="text-center">
    //           <img className='cd-icon' src={`${process.env.PUBLIC_URL}/resources/images/cd-icon.png`} />
    //           {song[1]['label']}
    //         </div>
    //       })
    //     )
    //   };

      useEffect(()=>{
        // machine.nextActive: Get the next active element (only while shuffling).
        // machine.nextIndex: Next element index according to the current direction.
        // machine.prevIndex: Prev element index according to the current direction.
        // machine.running: Check if the machine is running.
        // machine.stopping: Check if the machine is stopping.
        // machine.active: The current active element.

        // (async function runPlaneMachine() {
        //   await planeMachine.shuffle(6);
        //   setTimeout(null, 1000);
        //   runPlaneMachine();
        // })();
        // document.querySelector('#btn1')
        //       .addEventListener("click", ()=>{planeMachine.shuffle(10)});
        // document.querySelector('#btn2')
        //       .addEventListener("click", ()=>{planeMachine.prev()});
        // document.querySelector('#btn3')
        //       .addEventListener("click", ()=>{planeMachine.next()});
        // document.querySelector('#btn4')
        //       .addEventListener("click", ()=>{planeMachine.shuffle(Infinity);});
        // document.querySelector('#btn5')
        //       .addEventListener("click", ()=>{planeMachine.stop(4);});
        // document.querySelector('#btn6')
        //       .addEventListener("click", ()=>{console.log('prevIndex : ' + planeMachine.prevIndex)});
        // document.querySelector('#btn7')
        //       .addEventListener("click", ()=>{console.log('nextIndex : ' + planeMachine.nextIndex)});
        // document.querySelector('#btn8')
        //       .addEventListener("click", ()=>{console.log('nextActive : ' + planeMachine.nextActive)});
        // document.querySelector('#btn9')
        //       .addEventListener("click",
        //       ()=>{document.querySelectorAll('.modal-overlay, .modal').forEach(element => {element.classList.add('show')})
        //       });
        // document.querySelector('#btn10')
        //       .addEventListener("click",
        //       ()=>{document.querySelectorAll('.modal-overlay, .modal').forEach(element => {element.classList.remove('show')})
        //       });
        
        console.log('use effect : slotNum ' + slotNum);
        const planeMachineElement = document.querySelector('#planeMachine');
        const planeMachine = new SlotMachine(planeMachineElement, {
          delay: 500,
          randomize() {
            return slotNum;
          },
        });
        planeMachine.shuffle(10)  // 돌려돌려
      }, []);

   
    return (
      <>
      <div className="modal-overlay"></div>
      <div id="plane">
        <div className="container container-slot">
            <div className="content">

              <img className='play-logo' src={`${process.env.PUBLIC_URL}/resources/images/play-logo.png`} />
              <div id="planeMachine">
                <div className="text-center">
                <img className='cd-icon' src={`${process.env.PUBLIC_URL}/resources/images/cd-icon.png`} />
                  Newjeans - Attention
                </div>
                <div className="text-center">
                  <img className='cd-icon' src={`${process.env.PUBLIC_URL}/resources/images/cd-icon.png`} />
                  NCT DREAM - Candy
                </div>
                <div className="text-center">
                  <img className='cd-icon' src={`${process.env.PUBLIC_URL}/resources/images/cd-icon.png`} />
                  ZICO - 아무노래
                </div>
                <div className="text-center">
                  <img className='cd-icon' src={`${process.env.PUBLIC_URL}/resources/images/cd-icon.png`} />
                  Aespa - Next Level
                </div>
              </div>

            </div>
        </div>
      </div>
      </>
  );
}

export default Slot;
