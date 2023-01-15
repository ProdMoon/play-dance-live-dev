import { useState } from 'react';
import axios from 'axios';

import { Button, Checkbox, FormControlLabel, FormGroup } from '@mui/material';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

import PopoverComponent from '../../modules/PopoverComponent/PopoverComponent';
import { useLoginContext } from '../../context/LoginContext';
import { SongListData } from '../../assets/songListData';

const CreateRoom = () => {
  const [userInfo, setUserInfo] = useLoginContext();
  const [formOpened, setFormOpened] = useState(false);

  const CreateRoomForm = () => {
    const [checkedList, setCheckedList] = useState(new Map());
    const [anchorEl, setAnchorEl] = useState(null); // for Popover

    const songList = SongListData;

    const handleSubmit = (e) => {
      e.preventDefault();
      if (checkedList.size < 3) {
        // 3개 미만의 곡이 선택되면 알림 popover를 띄웁니다.
        setAnchorEl(e.currentTarget);
      } else {
        // 선택한 곡이 3개 이상이면, 선택된 곡들을 서버로 보내서 새로운 방을 생성합니다(또는 이미 있는 방에 참가합니다).
        const confirmedList = [];
        checkedList.forEach((value, key) => {
          confirmedList.push(key);
        });
        try {
          axios
            .post('/api/room/match', {
              userId: userInfo.userEmail,
              songs: confirmedList,
            })
            .then((response) => {
              const data = response.data;
              console.info('Created room (roomId: ' + data.roomId + ')');
              setUserInfo((prevState) => ({
                ...prevState,
                roomId: data.roomId,
                isPublisher: true,
                isRoomOwner: data.isRoomOwner, // TODO: 서버는 isRoomOwner 값을 줘야 합니다.
                songs: data.songs,
              }));
            });
        } catch (error) {
          console.error(error);
        }
      }
    };

    const handleCheck = (song) => {
      const [key, value] = song;
      if (checkedList.has(key) === false) {
        setCheckedList((prevState) => new Map([...prevState, [key, value]]));
      } else {
        setCheckedList((prevState) => {
          const newState = new Map(prevState);
          newState.delete(key);
          return newState;
        });
      }
    };

    return (
      <form onSubmit={handleSubmit}>
        <FormGroup>
          {songList.map((song) => {
            const [key, props] = song;
            return (
              <FormControlLabel
                key={key}
                label={props.label}
                control={
                  <Checkbox
                    onChange={() => handleCheck(song)}
                    icon={<CheckCircleOutlineIcon />}
                    checkedIcon={<CheckCircleIcon />}
                  />
                }
              />
            );
          })}
        </FormGroup>
        <Button type='submit' variant='contained'>
          GO FOR THE DANCE BATTLE
        </Button>
        <PopoverComponent
          useStateForAnchor={[anchorEl, setAnchorEl]}
          message='3개 이상의 곡을 선택하세요!'
          position='bottom'
        />
      </form>
    );
  };

  const toggleOpen = (e) => {
    e.preventDefault();
    setFormOpened((prevState) => !prevState);
  };

  return (
    <>
      {userInfo.userName !== undefined ? (
        <div>
          <Button onClick={(e) => toggleOpen(e)} variant='contained'>
            {formOpened ? '닫기' : '참가하기'}
          </Button>
          {formOpened ? <CreateRoomForm /> : null}
        </div>
      ) : null}
    </>
  );
};

export default CreateRoom;
