// frontend/src/App.jsx
import React, { useState, useEffect } from 'react'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'

export default function App(){

    const stored = typeof window !== 'undefined' ? localStorage.getItem('auth') : null
    const [auth, setAuth] = useState(stored ? JSON.parse(stored) : null)

    useEffect(() => {
        if(auth) localStorage.setItem('auth', JSON.stringify(auth))
        else localStorage.removeItem('auth')
    }, [auth])

    return (
        <div style={{padding:20,fontFamily:'Arial'}}>
            <h1>Student Course - Professor UI</h1>
            {!auth ? (
                <Login onLogin={setAuth} />
            ) : (
                <Dashboard auth={auth} onLogout={() => setAuth(null)} />
            )}
        </div>
    )
}
