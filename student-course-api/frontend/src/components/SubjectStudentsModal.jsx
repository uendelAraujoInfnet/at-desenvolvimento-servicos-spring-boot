
import React, { useEffect, useState } from 'react'
import {
    Dialog, DialogTitle, DialogContent, DialogActions,
    Button, Table, TableBody, TableCell, TableHead, TableRow,
    Box, Typography, Alert
} from '@mui/material'
import { get } from '../api/fetcher'

/**
 * Normaliza lista para [{ student:{id,nome,cpf}, grade, enrollmentId }]
 */
function normalizeEnrollments(data) {
    if (!data || !Array.isArray(data)) return []
    // enrollment with nested student
    if (data.length > 0 && data[0].student) {
        return data.map(e => ({
            student: e.student,
            grade: e.grade ?? e.nota ?? null,
            enrollmentId: e.id ?? null,
            raw: e
        }))
    }
    // flat enrollment shape
    if (data.length > 0 && (data[0].studentId || data[0].studentName || data[0].student_name)) {
        return data.map(e => ({
            student: {
                id: e.studentId ?? e.student_id ?? null,
                nome: e.studentName ?? e.student_nome ?? e.student_name ?? e.nome ?? e.name ?? '—',
                cpf: e.cpf ?? null
            },
            grade: e.grade ?? e.nota ?? null,
            enrollmentId: e.id ?? null,
            raw: e
        }))
    }
    // list of students only
    if (data.length > 0 && (data[0].nome || data[0].name)) {
        return data.map(s => ({
            student: { id: s.id ?? null, nome: s.nome ?? s.name, cpf: s.cpf ?? null },
            grade: s.grade ?? s.nota ?? null,
            enrollmentId: null,
            raw: s
        }))
    }
    // fallback
    return data.map((item, idx) => ({
        student: { id: item.student?.id ?? item.id ?? null, nome: item.student?.nome ?? item.nome ?? item.name ?? '—', cpf: item.student?.cpf ?? item.cpf ?? null },
        grade: item.grade ?? item.nota ?? null,
        enrollmentId: item.id ?? null,
        raw: item
    }))
}

export default function SubjectStudentsModal({ open, onClose, subject, mode = 'approved', auth }) {
    const [list, setList] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)
    const [debugTries, setDebugTries] = useState([])

    useEffect(() => {
        if (!open || !subject) { setList([]); setError(null); setDebugTries([]); return }
        let mounted = true
        setLoading(true)
        setError(null)
        setDebugTries([])

        async function fetchList() {
            const tries = []
            // 1) rota direta do seu controller (tem no código que você enviou)
            const path1 = `/enrollments/subject/${subject.id}/${mode === 'approved' ? 'approved' : 'failed'}`
            tries.push({ path: path1, status: 'trying' })
            setDebugTries([...tries])
            try {
                const res = await get(path1, auth) // fetcher usa base /api
                tries[tries.length - 1].status = 'ok'
                tries[tries.length - 1].resultCount = Array.isArray(res) ? res.length : 0
                setDebugTries([...tries])

                // Controller returns List<Student> for these endpoints (conforme seu controller).
                // Normalizamos: students => grade=null (pois controlador devolve students only).
                const normalized = Array.isArray(res)
                    ? res.map(s => ({ student: { id: s.id ?? null, nome: s.nome ?? s.name, cpf: s.cpf ?? null }, grade: s.grade ?? s.nota ?? null, enrollmentId: null, raw: s }))
                    : []

                // If mode is approved/failed and items have no grade, we still show them (backend decided)
                if (mounted) {
                    setList(normalized)
                    setLoading(false)
                }
                return
            } catch (err) {
                tries[tries.length - 1].status = 'error'
                tries[tries.length - 1].error = err.message ?? String(err)
                setDebugTries([...tries])
                // continue to fallback
            }

            // 2) fallback: get ALL enrollments and filter client-side by subject id
            tries.push({ path: '/enrollments', status: 'trying' })
            setDebugTries([...tries])
            try {
                const all = await get('/enrollments', auth)
                tries[tries.length - 1].status = 'ok'
                tries[tries.length - 1].resultCount = Array.isArray(all) ? all.length : 0
                setDebugTries([...tries])

                const normalizedAll = normalizeEnrollments(all)
                // filter by subject id (supports different shapes)
                const filtered = normalizedAll.filter(item => {
                    const raw = item.raw || {}
                    const subjId = raw.subject?.id ?? raw.subjectId ?? raw.subject_id ?? raw.subject?.subjectId ?? null
                    if (subjId != null) return Number(subjId) === Number(subject.id)
                    // fallback by comparing subject.codigo or subject.nome if present
                    const subjCodigo = raw.subject?.codigo ?? raw.subjectCodigo ?? raw.subject_code ?? null
                    if (subjCodigo && subject.codigo && String(subjCodigo) === String(subject.codigo)) return true
                    const subjNome = raw.subject?.nome ?? raw.subjectNome ?? raw.subject_name ?? raw.subjectName
                    if (subjNome && subject.nome && String(subjNome) === String(subject.nome)) return true
                    return false
                })

                // filter by grade if grades exist
                const haveGrades = filtered.some(i => i.grade != null)
                let final = filtered
                if (haveGrades) {
                    if (mode === 'approved') final = filtered.filter(i => Number(i.grade) >= 7)
                    if (mode === 'failed') final = filtered.filter(i => Number(i.grade) < 7)
                }

                if (mounted) {
                    setList(final)
                    setLoading(false)
                }
                return
            } catch (err) {
                tries[tries.length - 1].status = 'error'
                tries[tries.length - 1].error = err.message ?? String(err)
                setDebugTries([...tries])
            }

            // nenhuma rota útil
            if (mounted) {
                setError('Nenhuma rota retornou dados. Verifique /api/enrollments ou cole aqui a resposta do server.')
                setLoading(false)
            }
        }

        fetchList()
        return () => { mounted = false }
    }, [open, subject, mode, auth])

    return (
        <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
            <DialogTitle>{subject ? `${subject.codigo} — ${subject.nome}` : 'Disciplina'} — {mode === 'approved' ? 'Aprovados' : 'Reprovados'}</DialogTitle>
            <DialogContent>
                <Box>
                    {loading && <Typography>Carregando...</Typography>}
                    {error && <Alert severity="warning" sx={{ mb: 2 }}>{error}</Alert>}
                    {!loading && !error && list.length === 0 && <Typography variant="body2" color="text.secondary">Nenhum aluno encontrado.</Typography>}
                    {!loading && list.length > 0 && (
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Nome</TableCell>
                                    <TableCell>CPF</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {list.map((item, idx) => (
                                    <TableRow key={item.student?.id ?? idx}>
                                        <TableCell>{item.student?.nome ?? '—'}</TableCell>
                                        <TableCell>{item.student?.cpf ?? '—'}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </Box>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Fechar</Button>
            </DialogActions>
        </Dialog>
    )
}
