import { Divider } from '@mui/material';
import '../../styles/Menubar.css';

import GoogleLogout from './GoogleLogout';
import Participants from './Participants';
import Ranking from './Ranking';
import UserProfile from './UserProfile';

const Menubar = () => {
  return (
    <div className='menubar-container'>
      <div className='menubar-item'>
        <img
          className='menubar-logo'
          src={`${process.env.PUBLIC_URL}/resources/images/menubar-logo.png`}
        />
      </div>
      <GoogleLogout />
      <UserProfile />
      <Divider
        flexItem={true}
        sx={{ margin: '10px', backgroundColor: 'black' }}
      />
      <Ranking />
      <Divider
        flexItem={true}
        sx={{ margin: '10px', backgroundColor: 'black' }}
      />
      <Participants />
    </div>
  );
};

export default Menubar;
