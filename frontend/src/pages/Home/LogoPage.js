const LogoPage = (props) => {
  const handleEnter = props.handler;
  return (
    <img style={{width: '100%', height: '100%', objectFit: 'cover',}} onClick={handleEnter} src={`${process.env.PUBLIC_URL}/resources/images/newjeans_minji.jpeg`}></img>
  )
}

export default LogoPage;