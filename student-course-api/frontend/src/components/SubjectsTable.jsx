
import React, { useEffect, useState } from 'react'
import { Table, TableBody, TableCell, TableHead, TableRow, IconButton, Box, Button, Stack } from '@mui/material'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import { get, del } from '../api/fetcher'
import SubjectForm from './SubjectForm'
import SubjectStudentsModal from './SubjectStudentsModal'

export default function SubjectsTable({ auth, onEdit = () => {}, onRefresh = () => {} }) {
    const [list, setList] = useState([])
    const [openForm, setOpenForm] = useState(false)
    const [editing, setEditing] = useState(null)
    const [modalOpen, setModalOpen] = useState(false)
    const [modalSubject, setModalSubject] = useState(null)
    const [modalMode, setModalMode] = useState('approved') // 'approved' | 'failed'

    async function load() {
        try {
            const data = await get('/subjects', auth)
            setList(Array.isArray(data) ? data : [])
        } catch (err) {
            console.error('Failed to load subjects', err)
            setList([])
        }
    }

    useEffect(() => { if (auth) load() }, [auth])

    async function handleDelete(id) {
        if (!confirm('Confirma exclusão?')) return
        await del(`/subjects/${id}`, auth)
        await load()
        onRefresh()
    }

    function openModal(subject, mode) {
        setModalSubject(subject)
        setModalMode(mode)
        setModalOpen(true)
    }

    return (
        <Box sx={{ mt: 2 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <h3>Disciplinas</h3>
                <Button variant="contained" onClick={() => { setEditing(null); setOpenForm(true) }}>Nova Disciplina</Button>
            </Box>

            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Código</TableCell>
                        <TableCell>Nome</TableCell>
                        <TableCell align="center">Aprovados / Reprovados</TableCell>
                        <TableCell align="right">Ações</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {list.map(s => (
                        <TableRow key={s.id}>
                            <TableCell>{s.codigo}</TableCell>
                            <TableCell>{s.nome}</TableCell>
                            <TableCell align="center">
                                <Stack direction="row" spacing={1} justifyContent="center">
                                    <Button size="small" variant="outlined" onClick={() => openModal(s, 'approved')}>Aprovados</Button>
                                    <Button size="small" color="error" variant="outlined" onClick={() => openModal(s, 'failed')}>Reprovados</Button>
                                </Stack>
                            </TableCell>
                            <TableCell align="right">
                                <IconButton size="small" onClick={() => { setEditing(s); setOpenForm(true); onEdit(s) }}><EditIcon fontSize="small" /></IconButton>
                                <IconButton size="small" onClick={() => handleDelete(s.id)}><DeleteIcon fontSize="small" /></IconButton>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>

            <SubjectForm open={openForm} onClose={() => setOpenForm(false)} auth={auth} onCreated={() => { load(); onRefresh(); }} initial={editing} />

            <SubjectStudentsModal open={modalOpen} onClose={() => setModalOpen(false)} subject={modalSubject} mode={modalMode} auth={auth} />
        </Box>
    )
}
