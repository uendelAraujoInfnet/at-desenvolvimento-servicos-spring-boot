import React, { useState } from 'react'

export default function Login({ onLogin }) {
    const [user, setUser] = useState('')
    const [pass, setPass] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    async function submit() {
        setError('')
        if (!user || !pass) return setError('Informe usuário e senha')
        setLoading(true)
        try {
            const res = await fetch('/api/students', {
                method: 'GET',
                headers: {
                    'Authorization': 'Basic ' + btoa(`${user}:${pass}`),
                    'Accept': 'application/json'
                }
            })

            if (res.status === 401) {
                setError('Credenciais inválidas')
                setLoading(false)
                return
            }
            if (!res.ok) {
                const txt = await res.text()
                setError(`Erro ao autenticar: ${res.status} ${txt}`)
                setLoading(false)
                return
            }

            onLogin({ user, pass })
        } catch (err) {
            console.error(err)
            setError('Erro ao conectar ao servidor: ' + err.message)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div>
            <h3>Login</h3>
            <div>
                <input placeholder="username" value={user} onChange={e => setUser(e.target.value)} />
            </div>
            <div>
                <input placeholder="password" type="password" value={pass} onChange={e => setPass(e.target.value)} />
            </div>
            <div style={{ marginTop: 8 }}>
                <button onClick={submit} disabled={loading}>{loading ? 'Entrando...' : 'Entrar'}</button>
            </div>
            {error && <div style={{ color: 'red', marginTop: 8 }}>{error}</div>}
        </div>
    )
}
