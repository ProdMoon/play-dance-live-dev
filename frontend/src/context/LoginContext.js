import { createContext, useContext, useState } from 'react';

const LoginContext = createContext();

export function useLoginContext() {
  const value = useContext(LoginContext);
  return value;
}

export default function LoginContextProvider({ children }) {
  const userInfoObject = useState({
    userName: undefined,
    userEmail: undefined,
    userPicture: undefined,
    roomId: undefined,
    isPublisher: false,
  });
  return (
    <LoginContext.Provider value={userInfoObject}>
      {children}
    </LoginContext.Provider>
  );
}
