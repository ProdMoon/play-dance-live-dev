import '../../styles/Menubar.css';

import GoogleLogin from './GoogleLogin';
import GoogleLogout from './GoogleLogout';
import Participants from './Participants';
import Ranking from './Ranking';
import UserProfile from './UserProfile';

const Menubar = () => {
  return (
    <div className='menubar-container'>
      <img
        className='menubar-logo'
        src={`${process.env.PUBLIC_URL}/resources/images/menubar-logo.png`}
      />
      <GoogleLogout />
      <UserProfile />
      <Ranking />
      <Participants />
    </div>
  );
};

export default Menubar;
