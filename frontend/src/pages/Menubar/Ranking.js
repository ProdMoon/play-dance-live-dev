import { Typography } from '@mui/material';
import { useSocketContext } from '../../context/SocketContext';

const Ranking = () => {
  const socketContext = useSocketContext();
  const currentRanking = socketContext.rankingList;

  return (
    <div className='menubar-item'>
      <Typography>현재 랭킹</Typography>
      {currentRanking !== null
        ? currentRanking.map((elem, i) => {
            return (
              <Typography>
                {i + 1}위 {elem.name}{' '}
                {elem.bestWinNums > 0 ? (
                  <span style={{ color: 'red' }}>{elem.bestWinNums}연승</span>
                ) : (
                  <span style={{ color: 'skyblue' }}>new</span>
                )}
              </Typography>
            );
          })
        : null}
    </div>
  );
};

export default Ranking;
