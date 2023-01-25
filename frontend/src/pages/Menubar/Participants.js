import { Typography } from '@mui/material';
import { useSocketContext } from '../../context/SocketContext';

const Participants = () => {
  const socketContext = useSocketContext();
  const currentWaiters = socketContext.participantList;

  return (
    <div className='menubar-item'>
      <Typography>다음 도전자</Typography>
      {currentWaiters !== null
        ? currentWaiters.map((elem) => {
            return <Typography>{elem.name}</Typography>;
          })
        : null}
    </div>
  );
};

export default Participants;
