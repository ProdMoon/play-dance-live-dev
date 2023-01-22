import { Typography } from '@mui/material';

const currentWaiters = ['괴벨스', '리덕스'];

const Participants = () => {
  return (
    <div>
      <Typography>다음 도전자</Typography>
      {currentWaiters.map((elem) => {
        return <Typography>{elem}</Typography>;
      })}
    </div>
  );
};

export default Participants;
