import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { useSocketContext } from '../../context/SocketContext';

const Ranking = () => {
  const socketContext = useSocketContext();
  const currentRanking = socketContext.rankingList;

  return (
    <div className='menubar-item'>
      <div className='list-table'>
        {/* <TableContainer component={Paper}>
        <Table sx={{ minWidth: 650 }} size='small' aria-label='a dense table'>
          <TableHead>
            <TableRow>
              <TableCell>Dessert (100g serving)</TableCell>
              <TableCell align='right'>Calories</TableCell>
              <TableCell align='right'>Fat&nbsp;(g)</TableCell>
              <TableCell align='right'>Carbs&nbsp;(g)</TableCell>
              <TableCell align='right'>Protein&nbsp;(g)</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow
                key={row.name}
                sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
              >
                <TableCell component='th' scope='row'>
                  {row.name}
                </TableCell>
                <TableCell align='right'>{row.calories}</TableCell>
                <TableCell align='right'>{row.fat}</TableCell>
                <TableCell align='right'>{row.carbs}</TableCell>
                <TableCell align='right'>{row.protein}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer> */}

        <Typography variant='h5'>현재 랭킹</Typography>
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
    </div>
  );
};

export default Ranking;
