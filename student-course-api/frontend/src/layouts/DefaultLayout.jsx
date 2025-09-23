
import React from 'react'
import { AppBar, Toolbar, Typography, Button, Drawer, List, ListItemButton, Box, Container, Toolbar as MuiToolbar } from '@mui/material'
import DashboardIcon from '@mui/icons-material/Dashboard'
import PeopleIcon from '@mui/icons-material/People'
import BookIcon from '@mui/icons-material/Book'

export default function DefaultLayout({ children, onLogout, user }) {
    return (
        <Box sx={{ display: 'flex' }}>
            <AppBar position="fixed">
                <Toolbar>
                    <Typography variant="h6">Student Course</Typography>
                    <Box sx={{ flexGrow: 1 }} />
                    <Typography variant="body2" sx={{ mr:2 }}>Olá, {user}</Typography>
                    <Button color="inherit" onClick={onLogout}>Logout</Button>
                </Toolbar>
            </AppBar>

            <Drawer variant="permanent" sx={{ '& .MuiDrawer-paper': { width:220, top: 64 } }}>
                <MuiToolbar />
                <List>
                    <ListItemButton><DashboardIcon sx={{ mr:1 }} />Dashboard</ListItemButton>
                    <ListItemButton><PeopleIcon sx={{ mr:1 }} />Alunos</ListItemButton>
                    <ListItemButton><BookIcon sx={{ mr:1 }} />Disciplinas</ListItemButton>
                </List>
            </Drawer>

            <Box component="main" sx={{ flexGrow:1, p:3, ml:'220px', mt:'64px' }}>
                <Container maxWidth="xl">
                    {children}
                </Container>
            </Box>
        </Box>
    )
}
