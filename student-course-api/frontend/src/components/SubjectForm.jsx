
import React from 'react'
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Box } from '@mui/material'
import { post } from '../api/fetcher'

export default function SubjectForm({ open, onClose, auth, onCreated }) {
    const [nome, setNome] = React.useState('')
    const [codigo, setCodigo] = React.useState('')
    const [loading, setLoading] = React.useState(false)

    React.useEffect(() => {
        if(!open) { setNome(''); setCodigo('') }
    }, [open])

    async function handleSubmit(e){
        e?.preventDefault()
        setLoading(true)
        try {
            await post('/subjects', { nome, codigo }, auth)
            onCreated && onCreated()
            onClose()
        } catch(err) {
            alert(err.message || 'Erro')
        } finally { setLoading(false) }
    }

    return (
        <Dialog open={open} onClose={onClose}>
            <DialogTitle>Nova Disciplina</DialogTitle>
            <DialogContent>
                <Box component="form" sx={{ mt:1 }}>
                    <TextField label="Nome" fullWidth value={nome} onChange={(e)=>setNome(e.target.value)} sx={{ mb:2 }} />
                    <TextField label="Código" fullWidth value={codigo} onChange={(e)=>setCodigo(e.target.value)} />
                </Box>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} disabled={loading}>Cancelar</Button>
                <Button onClick={handleSubmit} variant="contained" disabled={loading}>Salvar</Button>
            </DialogActions>
        </Dialog>
    )
}
