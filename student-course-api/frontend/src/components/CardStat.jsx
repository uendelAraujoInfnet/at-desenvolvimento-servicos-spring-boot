
import React from 'react'
import { Card, Typography, Box } from '@mui/material'

export default function CardStat({ title, value, subtitle }) {
    return (
        <Card variant="outlined" sx={{ p:2 }}>
            <Box>
                <Typography variant="caption" color="text.secondary">{title}</Typography>
                <Typography variant="h5">{value}</Typography>
                {subtitle && <Typography variant="body2" color="text.secondary">{subtitle}</Typography>}
            </Box>
        </Card>
    )
}
