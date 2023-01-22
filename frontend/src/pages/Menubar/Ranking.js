import { Typography } from '@mui/material';

const currentRanking = ['1위 아이브 3연승', '2위 뉴진스 2연승', '3위 괴벨스', '4위 리덕스'];

const Ranking = () => {
  return (
    <div>
      <Typography>현재 랭킹</Typography>
      {currentRanking.map((elem) => {
        return <Typography>{elem}</Typography>;
      })}
    </div>
  );
};

export default Ranking;
