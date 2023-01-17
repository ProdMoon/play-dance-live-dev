import '../../styles/LogoPage.css'

const LogoPage = (props) => {
  const handleEnter = props.handler;
  return (
    <>
      <div className='spotlight'></div>
      <div className='searchlight1'></div>
      <div className='searchlight2'></div>
      <img
        className='logoimage'
        onClick={handleEnter}
        src={`${process.env.PUBLIC_URL}/resources/images/menubar-logo.png`}
      />
    </>
  );
};

export default LogoPage;
