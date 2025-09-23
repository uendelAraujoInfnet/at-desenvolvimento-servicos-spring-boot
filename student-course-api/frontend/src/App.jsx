
import React, { useState } from 'react'
import Login from './pages/Login'
import DefaultLayout from './layouts/DefaultLayout'
import Dashboard from './pages/Dashboard'

export default function App(){
    const stored = localStorage.getItem('auth')
    const [auth, setAuth] = useState(stored ? JSON.parse(stored) : null)

    const onLogin = (a) => {
        setAuth(a)
        localStorage.setItem('auth', JSON.stringify(a))
    }

    return (
        <>
            {!auth ? (
                <Login onLogin={onLogin} />
            ) : (
                <DefaultLayout user={auth.user} onLogout={() => { setAuth(null); localStorage.removeItem('auth') }}>
                    <Dashboard auth={auth} />
                </DefaultLayout>
            )}
        </>
    )
}
