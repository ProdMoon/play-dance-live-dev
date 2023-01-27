import './Fire.css';
import React from 'react';

const Fire = () => {
    const parts = 50;
    const particles = [];
    for (let i = 0; i < parts; i++) {
        particles.push(<div className="particle" key={i} />);
    }

    return <div className="fire">{particles}</div>;
}

export default Fire;