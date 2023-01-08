// MUI
import { Grid, Paper } from "@mui/material";

const Home = () => {
  return (
    <Grid className="container" container spacing={2}>
      <Grid className="containerItem" item xs={3}>
        <Paper className="containerItem" elevation={5}>
          메뉴가 들어갈 공간입니다.
        </Paper>
      </Grid>
      <Grid className="containerItem" item xs={6}>
        <Paper className="containerItem" elevation={5}>
          메인 영상이 들어갈 공간입니다.
        </Paper>
      </Grid>
      <Grid className="containerItem" item xs={3}>
        <Paper className="containerItem" elevation={5}>
          채팅창이 들어갈 공간입니다.
        </Paper>
      </Grid>
    </Grid>
  );
};

export default Home;
