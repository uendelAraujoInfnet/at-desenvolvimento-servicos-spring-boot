import React, { useEffect, useState } from 'react'
import { get, post, put, del } from '../api/fetcher'
import Notification from './Notification'

export default function Students({ auth }) {
    const [list, setList] = useState([])
    const [form, setForm] = useState({ nome:'', cpf:'', email:'', telefone:'', endereco:'' })
    const [editingId, setEditingId] = useState(null)
    const [note, setNote] = useState(null)

    useEffect(() => {
        if(!auth) return
        load()
    }, [auth])

    async function load(){
        try {
            const data = await get('/students', auth)
            setList(data)
        } catch (err) {
            console.error(err)
            setNote({ message: 'Erro ao carregar alunos: ' + err.message, type: 'error' })
        }
    }

    async function submit(){
        try {
            if(editingId){
                await put(`/students/${editingId}`, form, auth)
                setNote({ message: 'Aluno atualizado', type: 'success' })
            } else {
                await post('/students', form, auth)
                setNote({ message: 'Aluno criado', type: 'success' })
            }
            setForm({ nome:'', cpf:'', email:'', telefone:'', endereco:'' })
            setEditingId(null)
            await load()
            // notifica outros componentes (ex: Enrollments) que os dados mudaram
            window.dispatchEvent(new CustomEvent('data:changed', { detail: { resource: 'students' }}))
        } catch(err){
            setNote({ message: err.message, type: 'error' })
        }
    }

    function startEdit(s){
        setEditingId(s.id)
        setForm({ nome:s.nome, cpf:s.cpf, email:s.email, telefone:s.telefone, endereco:s.endereco })
    }

    async function remove(id){
        if(!confirm('Deseja remover esse aluno?')) return
        try {
            await del(`/students/${id}`, auth)
            setNote({ message: 'Aluno removido', type: 'success' })
            await load()
            window.dispatchEvent(new CustomEvent('data:changed', { detail: { resource: 'students' }}))
        } catch(err){
            setNote({ message: err.message, type: 'error' })
        }
    }

    return (
        <div style={{border:'1px solid #ddd', padding:10, marginTop:10}}>
            <h2>Alunos</h2>
            <Notification message={note?.message} type={note?.type} onClose={() => setNote(null)} />
            <div style={{display:'flex', gap:8, flexWrap:'wrap', marginBottom:8}}>
                <input placeholder="Nome" value={form.nome} onChange={e=>setForm({...form, nome:e.target.value})}/>
                <input placeholder="CPF" value={form.cpf} onChange={e=>setForm({...form, cpf:e.target.value})}/>
                <input placeholder="Email" value={form.email} onChange={e=>setForm({...form, email:e.target.value})}/>
                <input placeholder="Telefone" value={form.telefone} onChange={e=>setForm({...form, telefone:e.target.value})}/>
                <input placeholder="Endereco" value={form.endereco} onChange={e=>setForm({...form, endereco:e.target.value})}/>
                <button onClick={submit}>{editingId ? 'Salvar' : 'Criar'}</button>
                {editingId && <button onClick={() => { setEditingId(null); setForm({ nome:'', cpf:'', email:'', telefone:'', endereco:'' })}}>Cancelar</button>}
            </div>

            <ul>
                {list.map(s =>
                    <li key={s.id} style={{ marginBottom: 6 }}>
                        <strong>{s.nome}</strong> — {s.cpf} — {s.email} — {s.telefone} — {s.endereco}
                        <div style={{display:'inline-block', marginLeft:12}}>
                            <button onClick={() => startEdit(s)}>Editar</button>
                            <button onClick={() => remove(s.id)} style={{marginLeft:6}}>Excluir</button>
                        </div>
                    </li>
                )}
            </ul>
        </div>
    )
}