import React, { useEffect, useState } from 'react'
import { Table, TableBody, TableCell, TableHead, TableRow, IconButton, Box, Button } from '@mui/material'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import AddIcon from '@mui/icons-material/Add'
import { get, del } from '../api/fetcher'
import StudentForm from './StudentForm'

export default function StudentsTable({ auth, onRefresh = ()=>{} }) {
    const [list, setList] = useState([])
    const [openForm, setOpenForm] = useState(false)
    const [editing, setEditing] = useState(null)

    async function load(){
        try {
            const data = await get('/students', auth)
            console.log('[StudentsTable] loaded', Array.isArray(data) ? data.length : 'not-array', data)
            setList(Array.isArray(data) ? data : [])
        } catch(err) {
            console.error('Failed to load students', err)
            setList([])
        }
    }

    useEffect(()=>{ if(auth) load() }, [auth])

    async function handleDelete(id){
        if(!confirm('Confirmar exclusão do aluno?')) return
        await del(`/students/${id}`, auth)
        await load()
        onRefresh()
    }

    return (
        <Box sx={{ mt:2 }}>
            <Box sx={{ display:'flex', justifyContent:'space-between', mb:1 }}>
                <h3>Alunos</h3>
                <Button startIcon={<AddIcon />} variant="contained" onClick={()=>{ setEditing(null); setOpenForm(true) }}>Novo Aluno</Button>
            </Box>

            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Nome</TableCell>
                        <TableCell>CPF</TableCell>
                        <TableCell>E-mail</TableCell>
                        <TableCell>Telefone</TableCell>
                        <TableCell>Endereço</TableCell>
                        <TableCell align="right"></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {list.map(s => (
                        <TableRow key={s.id}>
                            <TableCell>{s.nome}</TableCell>
                            <TableCell>{s.cpf}</TableCell>
                            <TableCell>{s.email}</TableCell>
                            <TableCell>{s.telefone}</TableCell>
                            <TableCell>{s.endereco}</TableCell>
                            <TableCell align="right">
                                <IconButton size="small" onClick={()=>{ setEditing(s); setOpenForm(true) }}><EditIcon fontSize="small" /></IconButton>
                                <IconButton size="small" onClick={()=>handleDelete(s.id)}><DeleteIcon fontSize="small" /></IconButton>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>

            <StudentForm open={openForm} onClose={()=>setOpenForm(false)} auth={auth} onSaved={load} initial={editing} />
        </Box>
    )
}