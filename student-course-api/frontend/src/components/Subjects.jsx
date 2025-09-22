import React, { useEffect, useState } from 'react'
import { get, post, put, del } from '../api/fetcher'
import Notification from './Notification'

export default function Subjects({ auth }) {
    const [list, setList] = useState([])
    const [form, setForm] = useState({ nome:'', codigo:'' })
    const [editingId, setEditingId] = useState(null)
    const [note, setNote] = useState(null)

    useEffect(()=> {
        if(!auth) return
        load()
    }, [auth])

    async function load(){
        try {
            const data = await get('/subjects', auth)
            setList(data)
        } catch (err) {
            setNote({ message: 'Erro ao carregar disciplinas: '+err.message, type: 'error' })
        }
    }

    async function submit(){
        try {
            if(editingId){
                await put(`/subjects/${editingId}`, form, auth)
                setNote({ message: 'Disciplina atualizada', type: 'success' })
            } else {
                await post('/subjects', form, auth)
                setNote({ message: 'Disciplina criada', type: 'success' })
            }
            setForm({ nome:'', codigo:'' })
            setEditingId(null)
            await load()
            window.dispatchEvent(new CustomEvent('data:changed', { detail: { resource: 'subjects' }}))
        } catch (err) {
            setNote({ message: err.message, type: 'error' })
        }
    }

    async function remove(id){
        if(!confirm('Deseja remover essa disciplina?')) return
        try {
            await del(`/subjects/${id}`, auth)
            setNote({ message: 'Disciplina removida', type: 'success' })
            await load()
            window.dispatchEvent(new CustomEvent('data:changed', { detail: { resource: 'subjects' }}))
        } catch(err){
            setNote({ message: err.message, type: 'error' })
        }
    }

    function startEdit(s){
        setEditingId(s.id)
        setForm({ nome: s.nome, codigo: s.codigo })
    }

    return (
        <div style={{border:'1px solid #ddd', padding:10, marginTop:10}}>
            <h2>Subjects</h2>
            <Notification message={note?.message} type={note?.type} onClose={() => setNote(null)} />
            <div style={{display:'flex', gap:8, marginBottom:8}}>
                <input placeholder="Nome" value={form.nome} onChange={e=>setForm({...form, nome:e.target.value})}/>
                <input placeholder="Codigo" value={form.codigo} onChange={e=>setForm({...form, codigo:e.target.value})}/>
                <button onClick={submit}>{editingId ? 'Salvar' : 'Criar'}</button>
                {editingId && <button onClick={() => { setEditingId(null); setForm({ nome:'', codigo:'' })}}>Cancelar</button>}
            </div>

            <ul>
                {list.map(s => <li key={s.id}>{s.nome} — {s.codigo}
                    <span style={{marginLeft:12}}>
            <button onClick={() => startEdit(s)}>Editar</button>
            <button onClick={() => remove(s.id)} style={{marginLeft:6}}>Excluir</button>
          </span>
                </li>)}
            </ul>
        </div>
    )
}