
import React, { useState } from 'react'
import { Container, Paper, Typography, TextField, Button, Box, Alert } from '@mui/material'

export default function Login({ onLogin }) {
    const [user, setUser] = useState('')
    const [pass, setPass] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    async function submit(e){
        e?.preventDefault()
        setError('')
        if(!user || !pass) return setError('Informe usuário e senha')
        setLoading(true)
        try {
            const res = await fetch('/api/students', {
                method: 'GET',
                headers: { 'Authorization': 'Basic ' + btoa(`${user}:${pass}`) }
            })
            if(res.status === 401) { setError('Credenciais inválidas'); setLoading(false); return }
            if(!res.ok){ const txt = await res.text(); setError('Erro ao autenticar: ' + txt); setLoading(false); return }
            onLogin({ user, pass })
        } catch(err) {
            console.error(err); setError('Erro de conexão')
        } finally { setLoading(false) }
    }

    return (
        <Container maxWidth="xs" sx={{ mt:8 }}>
            <Paper elevation={3} sx={{ p:3 }}>
                <Typography variant="h6" gutterBottom>Entrar — Student Course</Typography>
                {error && <Alert severity="error" sx={{ mb:2 }}>{error}</Alert>}
                <Box component="form" onSubmit={submit} noValidate>
                    <TextField label="Usuário" fullWidth margin="normal" value={user} onChange={e=>setUser(e.target.value)} />
                    <TextField label="Senha" type="password" fullWidth margin="normal" value={pass} onChange={e=>setPass(e.target.value)} />
                    <Button type="submit" variant="contained" fullWidth sx={{ mt:2 }} disabled={loading}>
                        {loading ? 'Entrando...' : 'Entrar'}
                    </Button>
                </Box>
            </Paper>
        </Container>
    )
}