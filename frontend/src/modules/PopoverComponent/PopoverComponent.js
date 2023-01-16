import { Popover, Typography } from "@mui/material";

const PopoverComponent = (props) => {
  const [anchorEl, setAnchorEl] = props.useStateForAnchor;
  const message = props.message;
  const position = props.position;

  const handleClosePopover = () => {
    setAnchorEl(null);
  };

  const open = Boolean(anchorEl);

  return (
    <Popover
      open={open}
      anchorEl={anchorEl}
      onClose={handleClosePopover}
      anchorOrigin={{
        vertical: position === 'top' ? 'top' : 'bottom',
        horizontal: 'left',
      }}
      transformOrigin={{
        vertical: position === 'top' ? 'bottom' : 'top',
        horizontal: 'left',
      }}
    >
      <Typography sx={{ p: 2 }}>
        {message}
      </Typography>
    </Popover>
  );
};

export default PopoverComponent;