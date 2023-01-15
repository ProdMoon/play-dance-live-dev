const SocketContext = createContext();

const SocketContextProvider = ({ children }) => {
  const SocketObject = useState({
    userName: undefined,
    userEmail: undefined,
    userPicture: undefined,
    roomId: undefined,
    isPublisher: false,
  });
  return (
    <SocketContext.Provider value={userInfoObject}>
      {children}
    </SocketContext.Provider>
  );
};

export function useSocketContext() {
  const value = useContext(LoginContext);
  return value;
}