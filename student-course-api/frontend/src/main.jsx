import React from 'react'
import { createRoot } from 'react-dom/client'
import CssBaseline from '@mui/material/CssBaseline'
import { ThemeProvider, createTheme } from '@mui/material/styles'
import App from './App'

const theme = createTheme({
    palette: { mode: 'light', primary: { main: '#1976d2' } },
    typography: { fontFamily: ['Inter', 'Roboto', 'Arial', 'sans-serif'].join(',') }
})

const root = createRoot(document.getElementById('root'))
root.render(
    <ThemeProvider theme={theme}>
        <CssBaseline />
        <App />
    </ThemeProvider>
)