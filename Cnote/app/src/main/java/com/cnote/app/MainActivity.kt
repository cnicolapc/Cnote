package com.cnote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cnote.app.data.NoteType
import com.cnote.app.ui.NoteViewModel
import com.cnote.app.ui.screens.NoteEditScreen
import com.cnote.app.ui.screens.NoteListScreen
import com.cnote.app.ui.screens.WebClipViewScreen
import com.cnote.app.ui.theme.CnoteTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Se arriviamo da ShareReceiverActivity dopo un web clip, apriamo subito quella nota
        val openNoteId = intent?.getLongExtra("open_note_id", -1L)?.takeIf { it != -1L }

        setContent {
            CnoteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CnoteApp(viewModel, startNoteId = openNoteId)
                }
            }
        }
    }
}

@Composable
fun CnoteApp(viewModel: NoteViewModel, startNoteId: Long? = null) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (startNoteId != null) "edit/$startNoteId/${NoteType.TEXT.name}" else "list"
    ) {
        composable("list") {
            NoteListScreen(
                viewModel = viewModel,
                onOpenNote = { noteId, type ->
                    val idArg = noteId ?: -1L
                    navController.navigate("edit/$idArg/${type.name}")
                }
            )
        }
        composable(
            route = "edit/{noteId}/{type}",
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val noteIdArg = backStackEntry.arguments?.getLong("noteId") ?: -1L
            val typeArg = backStackEntry.arguments?.getString("type") ?: NoteType.TEXT.name
            NoteEditScreen(
                viewModel = viewModel,
                noteId = if (noteIdArg == -1L) null else noteIdArg,
                initialType = NoteType.valueOf(typeArg),
                onBack = { navController.popBackStack() },
                onOpenWebClip = { id -> navController.navigate("webview/$id") }
            )
        }
        composable(
            route = "webview/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteIdArg = backStackEntry.arguments?.getLong("noteId") ?: 0L
            WebClipViewScreen(
                viewModel = viewModel,
                noteId = noteIdArg,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
