package com.attafitamim.navigation.router.compose.utils

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(isEnabled: Boolean, onBack: ()-> Unit)
