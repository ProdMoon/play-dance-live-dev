import { Button, Grid } from '@mui/material';
import { useLoginContext } from '../../context/LoginContext';
import '../../styles/LogoPage.css';
import GoogleLogin from '../Menubar/GoogleLogin';
import GoogleLogout from '../Menubar/GoogleLogout';

const LogoPage = (props) => {
  const [userInfo, setUserInfo] = useLoginContext();
  const handleEnter = props.handler;

  function handleParticipantEnter() {
    setUserInfo((prevState) => ({
      ...prevState,
      isPublisher: true,
    }));
    handleEnter();
  }

  return (
    <>
      <div className='searchlight1'></div>
      <div className='searchlight2'></div>
      <div className='spotlight'>
        <img
          className='logoimage'
          src={`${process.env.PUBLIC_URL}/resources/images/menubar-logo.png`}
        />
      </div>
      <div className='buttonarea'>
        {userInfo.userEmail === undefined ? <GoogleLogin /> : null}
        {userInfo.userEmail !== undefined ? (
          <>
            <Button
              variant='outlined'
              sx={{ margin: '5px' }}
              onClick={handleParticipantEnter}
            >
              참가하기
            </Button>
            <Button
              variant='contained'
              sx={{ margin: '5px' }}
              onClick={handleEnter}
            >
              입장하기
            </Button>
          </>
        ) : null}
      </div>
    </>
  );
};

export default LogoPage;
