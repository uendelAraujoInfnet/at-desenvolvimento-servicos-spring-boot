import React, { useEffect, useState, useCallback } from 'react'
import { get, post, put } from '../api/fetcher'
import Notification from './Notification'

export default function Enrollments({ auth }){
    const [students, setStudents] = useState([])
    const [subjects, setSubjects] = useState([])
    const [enrolls, setEnrolls] = useState([])
    const [form, setForm] = useState({ studentId:'', subjectId:'' })
    const [grade, setGrade] = useState({ enrollId:'', value:'' })
    const [note, setNote] = useState(null)
    const [approvedList, setApprovedList] = useState([])
    const [failedList, setFailedList] = useState([])

    const fetchAll = useCallback(async () => {
        if(!auth) return
        try {
            const [s, su, e] = await Promise.all([
                get('/students', auth),
                get('/subjects', auth),
                get('/enrollments', auth)
            ])
            setStudents(s); setSubjects(su); setEnrolls(e)
        } catch(err){
            setNote({ message: 'Erro ao carregar dados: '+err.message, type:'error' })
        }
    }, [auth])

    useEffect(() => {
        if(!auth) return
        fetchAll()

        // handler para eventos globais de mudança
        const handler = () => {
            // pequena proteção: espera 200ms para permitir que backend finalize transação
            setTimeout(() => {
                fetchAll()
            }, 200)
        }

        window.addEventListener('data:changed', handler)
        return () => window.removeEventListener('data:changed', handler)
    }, [auth, fetchAll])

    async function enroll(){
        if(!form.studentId || !form.subjectId) return setNote({ message:'Selecione aluno e disciplina', type:'error' })
        try {
            await post('/enrollments', { studentId: Number(form.studentId), subjectId: Number(form.subjectId) }, auth)
            setNote({ message:'Aluno matriculado', type:'success' })
            setForm({ studentId:'', subjectId:'' })
            await fetchAll()
        } catch(err){
            setNote({ message: err.message, type:'error' })
        }
    }

    async function assignGrade(){
        if(!grade.enrollId || grade.value === '') return setNote({ message:'Informe matrícula e nota', type:'error' })
        try {
            await put(`/enrollments/${grade.enrollId}/grade`, { grade: Number(grade.value) }, auth)
            setNote({ message:'Nota atribuída', type:'success' })
            setGrade({ enrollId:'', value:'' })
            await fetchAll()
        } catch(err){
            setNote({ message: err.message, type:'error' })
        }
    }

    async function listBySubject(subjectId){
        try {
            const approved = await get(`/enrollments/subject/${subjectId}/approved`, auth)
            const failed = await get(`/enrollments/subject/${subjectId}/failed`, auth)
            setApprovedList(approved)
            setFailedList(failed)
        } catch(err){
            setNote({ message: 'Erro ao buscar listas: '+err.message, type:'error' })
        }
    }

    return (
        <div style={{border:'1px solid #ddd', padding:10, marginTop:10}}>
            <h2>Matrículas</h2>
            <Notification message={note?.message} type={note?.type} onClose={() => setNote(null)} />

            <div style={{display:'flex', gap:8, alignItems:'center'}}>
                <select value={form.studentId} onChange={e=>setForm({...form, studentId: e.target.value})}>
                    <option value=''>Aluno</option>
                    {students.map(s => <option key={s.id} value={s.id}>{s.nome}</option>)}
                </select>

                <select value={form.subjectId} onChange={e=>setForm({...form, subjectId: e.target.value})}>
                    <option value=''>Disciplina</option>
                    {subjects.map(s => <option key={s.id} value={s.id}>{s.nome}</option>)}
                </select>

                <button onClick={enroll}>Matricular</button>
            </div>

            <div style={{marginTop:10}}>
                <input placeholder="Enrollment ID" value={grade.enrollId} onChange={e=>setGrade({...grade, enrollId: e.target.value})}/>
                <input placeholder="Grade" value={grade.value} onChange={e=>setGrade({...grade, value: e.target.value})}/>
                <button onClick={assignGrade}>Atribuir nota</button>
            </div>

            <h3>Matrículas</h3>
            <ul>
                {enrolls.length === 0 ? <li>Nenhuma matrícula</li> : enrolls.map(en => (
                    <li key={en.id}>#{en.id} - {en.student?.nome} / {en.subject?.nome} - nota: {en.grade ?? '—'}</li>
                ))}
            </ul>

            <div style={{marginTop:10}}>
                <h4>Listar aprovados/reprovados por disciplina</h4>
                <select onChange={e => { if(e.target.value) listBySubject(e.target.value) }}>
                    <option value=''>Escolha uma disciplina</option>
                    {subjects.map(s => <option key={s.id} value={s.id}>{s.nome}</option>)}
                </select>

                <div style={{display:'flex', gap:20, marginTop:10}}>
                    <div>
                        <h5>Aprovados</h5>
                        <ul>{approvedList.length === 0 ? <li>—</li> : approvedList.map(a => <li key={a.id}>{a.nome}</li>)}</ul>
                    </div>

                    <div>
                        <h5>Reprovados</h5>
                        <ul>{failedList.length === 0 ? <li>—</li> : failedList.map(a => <li key={a.id}>{a.nome}</li>)}</ul>
                    </div>
                </div>
            </div>
        </div>
    )
}