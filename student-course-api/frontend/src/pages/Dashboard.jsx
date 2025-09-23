
import React, { useEffect, useState } from 'react'
import { Grid, Box, Button, Typography } from '@mui/material'
import CardStat from '../components/CardStat'
import SubjectsTable from '../components/SubjectsTable'
import StudentsTable from '../components/StudentsTable'
import EnrollmentsTable from '../components/EnrollmentsTable'
import SubjectForm from '../components/SubjectForm'
import { get } from '../api/fetcher'

export default function Dashboard({ auth }) {
    const [metrics, setMetrics] = useState({ students: 0, subjects: 0, enrollments: 0 })
    const [openSubject, setOpenSubject] = React.useState(false)
    const [reloadKey, setReloadKey] = React.useState(0)

    useEffect(() => {
        if (!auth) return
        let mounted = true

        async function loadMetrics() {
            // tentativa 1: endpoint /api/metrics
            const tries = ['/api/metrics', '/api/subjects/metrics']
            for (const path of tries) {
                try {
                    const res = await get(path, auth)
                    // aceita {students, subjects, enrollments} ou {students: N, ...}
                    if (res && (res.students != null || res.subjects != null || res.enrollments != null)) {
                        if (!mounted) return
                        setMetrics({
                            students: res.students ?? 0,
                            subjects: res.subjects ?? 0,
                            enrollments: res.enrollments ?? 0
                        })
                        return
                    }
                } catch (e) {

                }
            }

            // fallback: buscar listas inteiras e contar (mais pesado)
            try {
                const [students, subjects, enrollments] = await Promise.all([
                    get('/students', auth).catch(() => []),
                    get('/subjects', auth).catch(() => []),
                    get('/enrollments', auth).catch(() => [])
                ])
                if (!mounted) return
                setMetrics({
                    students: Array.isArray(students) ? students.length : 0,
                    subjects: Array.isArray(subjects) ? subjects.length : 0,
                    enrollments: Array.isArray(enrollments) ? enrollments.length : 0
                })
            } catch (err) {
                console.error('Failed to compute metrics fallback', err)
            }
        }

        loadMetrics()
        return () => { mounted = false }
    }, [auth, reloadKey])

    // passar onRefresh para tables que geram mudanças
    return (
        <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h5">Dashboard</Typography>
                <Box>
                    <Button variant="outlined" onClick={() => setOpenSubject(true)} sx={{ mr: 1 }}>Nova Disciplina</Button>
                </Box>
            </Box>

            <Grid container spacing={2}>
                <Grid item xs={12} sm={4}><CardStat title="Disciplinas" value={metrics.subjects} subtitle="Total de disciplinas cadastradas" /></Grid>
                <Grid item xs={12} sm={4}><CardStat title="Alunos" value={metrics.students} subtitle="Total de alunos" /></Grid>
                <Grid item xs={12} sm={4}><CardStat title="Matrículas" value={metrics.enrollments} subtitle="Total de matrículas" /></Grid>
            </Grid>

            <Box sx={{ mt: 3 }}>
                <SubjectsTable auth={auth} onRefresh={() => setReloadKey(k => k + 1)} />
                <StudentsTable auth={auth} onRefresh={() => setReloadKey(k => k + 1)} />
                <EnrollmentsTable auth={auth} onRefresh={() => setReloadKey(k => k + 1)} />
            </Box>

            <SubjectForm open={openSubject} onClose={() => setOpenSubject(false)} auth={auth} onCreated={() => setReloadKey(k => k + 1)} />
        </Box>
    )
}
