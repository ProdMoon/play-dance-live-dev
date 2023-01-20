import React from 'react';
import ReactDOM from 'react-dom';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

import './styles/index.css';
import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';

// import registerServiceWorker from "./registerServiceWorker";
import Home from './pages/Home/Home';
import StreamArea from './pages/StreamArea/StreamArea';
import Test from './modules/Test/Test';
import Slot from './modules/Test/Slot';

const router = createBrowserRouter([
  {
    path: '/',
    element: <Home />,
  },
  {
    path: '/streamarea',
    element: <StreamArea />,
  },
  // for test
  {
    path: '/test',
    element: <Test />,
  },
  {
    path: '/Slot',
    element: <Slot />,
  },

]);

ReactDOM.render(
  <RouterProvider router={router} />,
  document.getElementById('root'),
);
// registerServiceWorker();
