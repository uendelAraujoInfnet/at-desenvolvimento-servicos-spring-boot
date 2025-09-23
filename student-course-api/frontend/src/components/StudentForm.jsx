
import React from 'react'
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Box } from '@mui/material'
import { post, put } from '../api/fetcher'

export default function StudentForm({ open, onClose, auth, onSaved, initial }) {
    const [nome, setNome] = React.useState(initial?.nome || '')
    const [cpf, setCpf] = React.useState(initial?.cpf || '')
    const [email, setEmail] = React.useState(initial?.email || '')
    const [telefone, setTelefone] = React.useState(initial?.telefone || '')
    const [endereco, setEndereco] = React.useState(initial?.endereco || '')
    const [loading, setLoading] = React.useState(false)

    React.useEffect(()=> {
        if(open) {
            setNome(initial?.nome || '')
            setCpf(initial?.cpf || '')
            setEmail(initial?.email || '')
            setTelefone(initial?.telefone || '')
            setEndereco(initial?.endereco || '')
        }
    }, [open, initial])

    async function handleSubmit(e){
        e?.preventDefault()
        setLoading(true)
        try {
            const body = { nome, cpf, email, telefone, endereco }
            if(initial?.id) {
                await put(`/students/${initial.id}`, body, auth)
            } else {
                await post('/students', body, auth)
            }
            onSaved && onSaved()
            onClose()
        } catch(err) {
            alert(err.message || 'Erro')
        } finally { setLoading(false) }
    }

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>{initial?.id ? 'Editar Aluno' : 'Novo Aluno'}</DialogTitle>
            <DialogContent>
                <Box component="form" sx={{ mt:1 }}>
                    <TextField label="Nome" fullWidth value={nome} onChange={e=>setNome(e.target.value)} sx={{ mb:2 }} />
                    <TextField label="CPF" fullWidth value={cpf} onChange={e=>setCpf(e.target.value)} sx={{ mb:2 }} />
                    <TextField label="E-mail" fullWidth value={email} onChange={e=>setEmail(e.target.value)} sx={{ mb:2 }} />
                    <TextField label="Telefone" fullWidth value={telefone} onChange={e=>setTelefone(e.target.value)} sx={{ mb:2 }} />
                    <TextField label="Endereço" fullWidth value={endereco} onChange={e=>setEndereco(e.target.value)} sx={{ mb:2 }} />
                </Box>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} disabled={loading}>Cancelar</Button>
                <Button onClick={handleSubmit} variant="contained" disabled={loading}>{initial?.id ? 'Salvar' : 'Criar'}</Button>
            </DialogActions>
        </Dialog>
    )
}
