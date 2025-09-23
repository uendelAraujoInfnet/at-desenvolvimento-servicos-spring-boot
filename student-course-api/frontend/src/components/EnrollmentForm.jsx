
import React from 'react'
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, MenuItem, Select, InputLabel, FormControl, Box } from '@mui/material'
import { get, post } from '../api/fetcher'

export default function EnrollmentForm({ open, onClose, auth, onCreated }) {
    const [students, setStudents] = React.useState([])
    const [subjects, setSubjects] = React.useState([])
    const [studentId, setStudentId] = React.useState('')
    const [subjectId, setSubjectId] = React.useState('')
    const [loading, setLoading] = React.useState(false)

    React.useEffect(()=> {
        if(!open) return
        (async () => {
            try {
                const s = await get('/students', auth)
                const sub = await get('/subjects', auth)
                setStudents(Array.isArray(s) ? s : [])
                setSubjects(Array.isArray(sub) ? sub : [])
            } catch(err) {
                console.error(err)
            }
        })()
    }, [open])

    async function handleSubmit(e){
        e?.preventDefault()
        setLoading(true)
        try {
            await post('/enrollments', { studentId, subjectId }, auth)
            onCreated && onCreated()
            onClose()
        } catch(err) {
            alert(err.message || 'Erro')
        } finally { setLoading(false) }
    }

    return (
        <Dialog open={open} onClose={onClose}>
            <DialogTitle>Nova Matrícula</DialogTitle>
            <DialogContent>
                <Box sx={{ mt:1, minWidth: 360 }}>
                    <FormControl fullWidth sx={{ mb:2 }}>
                        <InputLabel id="sel-student">Aluno</InputLabel>
                        <Select labelId="sel-student" value={studentId} label="Aluno" onChange={(e)=>setStudentId(e.target.value)}>
                            {students.map(s => <MenuItem key={s.id} value={s.id}>{s.nome} — {s.cpf}</MenuItem>)}
                        </Select>
                    </FormControl>

                    <FormControl fullWidth>
                        <InputLabel id="sel-subject">Disciplina</InputLabel>
                        <Select labelId="sel-subject" value={subjectId} label="Disciplina" onChange={(e)=>setSubjectId(e.target.value)}>
                            {subjects.map(s => <MenuItem key={s.id} value={s.id}>{s.codigo} — {s.nome}</MenuItem>)}
                        </Select>
                    </FormControl>
                </Box>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Cancelar</Button>
                <Button variant="contained" onClick={handleSubmit} disabled={loading}>Matricular</Button>
            </DialogActions>
        </Dialog>
    )
}
