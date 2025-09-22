import React from 'react'

export default function Notification({ message, type = 'info', onClose }) {
    if(!message) return null
    const bg = type === 'error' ? '#ffdddd' : type === 'success' ? '#ddffdd' : '#eef'
    const style = { background: bg, padding: '8px 12px', margin: '8px 0', borderRadius: 6 }
    return (
        <div style={style}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>{message}</div>
                {onClose && <button onClick={onClose} style={{ marginLeft: 12 }}>x</button>}
            </div>
        </div>
    )
}