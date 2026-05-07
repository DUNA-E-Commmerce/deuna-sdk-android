package com.deuna.explore.presentation.screens.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.deuna.explore.domain.ExplorePresentationMode
import com.deuna.explore.presentation.ExploreViewModel
import com.deuna.explore.presentation.screens.drawer.ConfigurationDrawer

@Composable
fun MainScreen(viewModel: ExploreViewModel) {
    val state by viewModel.uiState.collectAsState()
    var isDrawerOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = state.appliedConfig.merchantName.ifEmpty { "SDK Tester" },
                showRefresh = state.appliedConfig.presentationMode == ExplorePresentationMode.EMBEDDED
                    && state.isShowingEmbeddedScreen,
                onOpenDrawer = {
                    viewModel.openDrawer()
                    isDrawerOpen = true
                },
                onRefresh = { viewModel.refreshEmbedded() },
            )
            HorizontalDivider()
            ModeContent(viewModel = viewModel)
        }

        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        }

        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it },
        ) {
            ConfigurationDrawer(
                viewModel = viewModel,
                onClose = { isDrawerOpen = false },
            )
        }
    }
}
