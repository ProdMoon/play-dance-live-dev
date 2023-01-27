import { Typography } from '@mui/material';
import { useSocketContext } from '../../context/SocketContext';

const Participants = () => {
  const socketContext = useSocketContext();
  const currentWaiters = socketContext.participantList;

  return (
    <div className='menubar-item'>
      <div className='list-table'>
        <Typography variant='h5'>
          다음 <span style={{ color: 'skyblue' }}>도전자</span>
        </Typography>
        {currentWaiters !== null
          ? currentWaiters.map((elem) => {
              return <Typography>{elem.name}</Typography>;
            })
          : null}
      </div>
    </div>
  );
};

export default Participants;
