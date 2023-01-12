// Pages
import Chat from "../Chat/Chat";

// MUI
import { Grid, Paper } from "@mui/material";
import GoogleLogin from "../GoogleLogin/GoogleLogin";
import StreamArea from "../StreamArea/StreamArea";

const Home = () => {
  return (
    <Grid className="container" container spacing={2}>
      <Grid className="containerItem" item xs={3}>
        <Paper className="containerItem" elevation={5}>
          <GoogleLogin />
        </Paper>
      </Grid>
      <Grid className="containerItem" item xs={6}>
        <Paper className="containerItem" elevation={5}>
          <StreamArea />
        </Paper>
      </Grid>
      <Grid className="containerItem" item xs={3}>
        <Paper className="containerItem" elevation={5}>
          <Chat />
        </Paper>
      </Grid>
    </Grid>
  );
};

export default Home;
