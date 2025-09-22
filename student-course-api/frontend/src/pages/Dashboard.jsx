import React from 'react'
import Students from '../components/Students'
import Subjects from '../components/Subjects'
import Enrollments from '../components/Enrollments'

export default function Dashboard({ auth, onLogout }){
    if(!auth) {
        return (
            <div style={{padding:20}}>
                <p>Autenticação necessária. Faça login.</p>
            </div>
        )
    }

    return (
        <div>
            <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
                <div>Logado como <strong>{auth?.user}</strong></div>
                <button onClick={onLogout}>Logout</button>
            </div>

            <Students auth={auth} />
            <Subjects auth={auth} />
            <Enrollments auth={auth} />
        </div>
    )
}
