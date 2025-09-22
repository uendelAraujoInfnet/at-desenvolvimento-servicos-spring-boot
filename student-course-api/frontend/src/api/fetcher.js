const API_BASE = import.meta.env.VITE_API_BASE || '/api'

function authHeader(auth){
    if(!auth) return {}
    const token = btoa(`${auth.user}:${auth.pass}`)
    return { 'Authorization': 'Basic ' + token }
}

async function handleResponse(res){
    const text = await res.text()
    const contentType = res.headers.get('content-type') || ''
    const body = contentType.includes('application/json') && text ? JSON.parse(text) : text
    if(!res.ok){
        const msg = (body && body.error) ? body.error : (typeof body === 'string' ? body : JSON.stringify(body))
        const err = new Error(msg || `HTTP ${res.status}`)
        err.status = res.status
        err.body = body
        throw err
    }
    return body
}

function requireAuth(auth){
    if(!auth) throw new Error('Not authenticated')
}

export async function get(endpoint, auth) {
    requireAuth(auth)
    const res = await fetch(`${API_BASE}${endpoint}`, {
        headers: { ...authHeader(auth) }
    })
    return handleResponse(res)
}

export async function post(endpoint, body, auth){
    requireAuth(auth)
    const res = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeader(auth) },
        body: JSON.stringify(body)
    })
    return handleResponse(res)
}

export async function put(endpoint, body, auth){
    requireAuth(auth)
    const res = await fetch(`${API_BASE}${endpoint}`, {
        method: 'PUT',
        headers: { 'Content-Type':'application/json', ...authHeader(auth) },
        body: JSON.stringify(body)
    })
    return handleResponse(res)
}

export async function del(endpoint, auth){
    requireAuth(auth)
    const res = await fetch(`${API_BASE}${endpoint}`, {
        method: 'DELETE',
        headers: { ...authHeader(auth) }
    })
    return handleResponse(res)
}