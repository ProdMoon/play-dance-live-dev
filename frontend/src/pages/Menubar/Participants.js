import { Typography } from '@mui/material';
import { useSocketContext } from '../../context/SocketContext';

const Participants = () => {
  
  const socketContext = useSocketContext();
  const currentWaiters = socketContext.participantList;

  return (
    <div>
      <Typography>다음 도전자</Typography>
      {currentWaiters.map((elem) => {
        return <Typography>{elem.name}</Typography>;
      })}
    </div>
  );
};

export default Participants;
