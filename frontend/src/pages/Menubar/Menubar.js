import CreateRoom from './CreateRoom';
import GoogleLogin from './GoogleLogin';

const Menubar = () => {
  return (
    <>
      <GoogleLogin />
      <p />
      <CreateRoom />
    </>
  );
};

export default Menubar;
