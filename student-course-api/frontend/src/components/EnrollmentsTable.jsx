
import React, { useEffect, useState } from 'react'
import { Table, TableBody, TableCell, TableHead, TableRow, IconButton, Box, Button, TextField } from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import SaveIcon from '@mui/icons-material/Save'
import CloseIcon from '@mui/icons-material/Close'
import { get, del, put } from '../api/fetcher'
import EnrollmentForm from './EnrollmentForm'

export default function EnrollmentsTable({ auth, onRefresh = () => {} }) {
    const [list, setList] = useState([])
    const [openEnroll, setOpenEnroll] = useState(false)
    const [gradeEditingId, setGradeEditingId] = useState(null)
    const [gradeValue, setGradeValue] = useState('')
    const [saving, setSaving] = useState(false)

    async function load(){
        try {
            const data = await get('/enrollments', auth)
            setList(Array.isArray(data) ? data : [])
            console.log('[EnrollmentsTable] loaded', Array.isArray(data) ? data.length : typeof data, data)
        } catch(err) {
            console.error('Failed to load enrollments', err)
            setList([])
        }
    }

    useEffect(()=> { if(auth) load() }, [auth])

    async function handleDelete(id){
        if(!confirm('Confirmar exclusão da matrícula?')) return
        try {
            await del(`/enrollments/${id}`, auth)
            await load()
            onRefresh()
        } catch(err) {
            alert('Erro ao excluir: ' + (err.message || err))
        }
    }

    function startEditGrade(enr){
        setGradeEditingId(enr.id)
        const g = enr.grade ?? enr.nota ?? (enr.enrollment?.grade) ?? null
        setGradeValue(g != null ? String(g) : '')
    }

    async function saveGrade(){
        if(!gradeEditingId) return
        setSaving(true)
        try {
            const parsed = gradeValue === '' ? null : Number(gradeValue)
            console.log('[EnrollmentsTable] salvando grade', { id: gradeEditingId, grade: parsed })
            // usar endpoint do seu controller: PUT /api/enrollments/{id}/grade  com body { "grade": <number|null> }
            const updated = await put(`/enrollments/${gradeEditingId}/grade`, { grade: parsed }, auth)
            console.log('[EnrollmentsTable] save result', updated)
            // refresh
            await load()
            setGradeEditingId(null)
            setGradeValue('')
            onRefresh()
        } catch(err) {
            console.error('Erro ao salvar nota', err)
            // mostrar mensagem mais útil
            const msg = err.message || JSON.stringify(err)
            alert('Erro ao salvar nota: ' + msg)
        } finally { setSaving(false) }
    }

    return (
        <Box sx={{ mt:3 }}>
            <Box sx={{ display:'flex', justifyContent:'space-between', mb:1 }}>
                <h3>Matrículas</h3>
                <Button variant="contained" onClick={()=>setOpenEnroll(true)}>Nova Matrícula</Button>
            </Box>

            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Aluno</TableCell>
                        <TableCell>Disciplina</TableCell>
                        <TableCell>Nota</TableCell>
                        <TableCell align="right"></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {list.map(e => (
                        <TableRow key={e.id}>
                            <TableCell>{e.student?.nome ?? e.studentName ?? e.student_nome ?? '—'}</TableCell>
                            <TableCell>
                                {e.subject?.nome ?? e.subjectName ?? e.subject_nome ?? '—'}
                                {e.subject?.codigo ? ` (${e.subject.codigo})` : ''}
                            </TableCell>
                            <TableCell>
                                {gradeEditingId === e.id ? (
                                    <div style={{ display:'flex', gap:8, alignItems:'center' }}>
                                        <TextField size="small" value={gradeValue} onChange={ev=>setGradeValue(ev.target.value)} style={{ width:80 }} />
                                        <IconButton size="small" onClick={saveGrade} disabled={saving}><SaveIcon fontSize="small" /></IconButton>
                                        <IconButton size="small" onClick={()=>{ setGradeEditingId(null); setGradeValue('') }}><CloseIcon fontSize="small" /></IconButton>
                                    </div>
                                ) : (
                                    e.grade != null ? e.grade : '—'
                                )}
                            </TableCell>
                            <TableCell align="right">
                                <IconButton size="small" onClick={()=>startEditGrade(e)}><EditIcon fontSize="small" /></IconButton>
                                <IconButton size="small" onClick={()=>handleDelete(e.id)}><DeleteIcon fontSize="small" /></IconButton>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>

            <EnrollmentForm open={openEnroll} onClose={()=>setOpenEnroll(false)} auth={auth} onCreated={()=>{ load(); onRefresh() }} />
        </Box>
    )
}
